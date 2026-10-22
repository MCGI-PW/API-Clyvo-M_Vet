package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.exceptions.AccountBlockedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.StatusConta;
import com.clyvo.veterinary.models.enums.TipoConta;
import com.clyvo.veterinary.repositories.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha incorretos.";
    private static final String ESPECIALIDADE_PADRAO = "Clínica Geral";

    private final ContaAcessoRepository contaRepository;
    private final CredencialRepository credencialRepository;
    private final IdentificadorAcessoRepository identRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository vetRepository;
    private final ClinicaRepository clinicaRepository;
    private final SessaoRepository sessaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(ContaAcessoRepository contaRepository,
                       CredencialRepository credencialRepository,
                       IdentificadorAcessoRepository identRepository,
                       TutorRepository tutorRepository,
                       VeterinarioRepository vetRepository,
                       ClinicaRepository clinicaRepository,
                       SessaoRepository sessaoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.contaRepository = contaRepository;
        this.credencialRepository = credencialRepository;
        this.identRepository = identRepository;
        this.tutorRepository = tutorRepository;
        this.vetRepository = vetRepository;
        this.clinicaRepository = clinicaRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return createSession(criarConta(request));
    }

    /**
     * Cria a conta completa (acesso, credencial, identificadores e perfil) sem abrir sessão.
     * Reutilizado pelo cadastro público e pela carga de dados de demonstração.
     */
    @Transactional
    public ContaAcesso criarConta(RegisterRequest request) {
        TipoConta tipo = TipoConta.from(request.getRole());
        if (tipo == null) {
            throw new BusinessException("Perfil de usuário inválido: " + request.getRole() + ". Valores aceitos: TUTOR, VETERINARIO, CLINICA.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (contaRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }

        String documento = isBlank(request.getDocument()) && tipo == TipoConta.CLINICA
                ? request.getCnpj()
                : request.getDocument();
        String tipoDocumento = tipo == TipoConta.CLINICA ? "CNPJ" : "CPF";
        validarIdentificadorLivre(tipoDocumento, documento);
        if (tipo == TipoConta.VETERINARIO) {
            validarIdentificadorLivre("CRMV", request.getCrmv());
        }

        ContaAcesso conta = criarContaComCredencial(email, request.getPhone(), tipo, request.getPassword());

        salvarIdentificador(conta, tipoDocumento, documento);
        if (tipo == TipoConta.VETERINARIO) {
            salvarIdentificador(conta, "CRMV", request.getCrmv());
        }

        criarPerfil(conta, tipo, request.getName());
        return conta;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = (request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : "";

        ContaAcesso conta = contaRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

        if (!StatusConta.ATIVA.matches(conta.getStatusConta())) {
            throw new AccountBlockedException("Esta conta está inativa ou bloqueada. Entre em contato com o suporte.");
        }

        Credencial cred = credencialRepository.findByContaAcessoIdConta(conta.getIdConta())
                .orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

        if (cred.getBloqueadoAte() != null && cred.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            throw new AccountBlockedException("Conta temporariamente bloqueada devido a excesso de tentativas. Tente novamente mais tarde.");
        }

        if (!passwordEncoder.matches(request.getPassword(), cred.getSenhaHash())) {
            registrarTentativaFalha(cred);
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }

        // Sucesso: zera contador de falhas e bloqueio
        if ((cred.getTentativasFalhas() != null && cred.getTentativasFalhas() > 0) || cred.getBloqueadoAte() != null) {
            cred.setTentativasFalhas(0);
            cred.setBloqueadoAte(null);
            credencialRepository.save(cred);
        }

        return createSession(conta);
    }

    /** Login social: autentica a conta existente ou provisiona automaticamente um novo Tutor. */
    @Transactional
    public AuthResponse loginOrRegisterOAuth2(String email, String name) {
        String emailClean = isBlank(email)
                ? "oauth2-" + UUID.randomUUID() + "@clyvovet.com"
                : email.trim().toLowerCase();
        String nameClean = isBlank(name) ? "Usuário Social OAuth2" : name.trim();

        ContaAcesso conta = contaRepository.findByEmail(emailClean).orElseGet(() -> {
            // Senha aleatória: a conta social só autentica via provedor OAuth2
            ContaAcesso nova = criarContaComCredencial(emailClean, null, TipoConta.TUTOR, UUID.randomUUID().toString());
            criarPerfil(nova, TipoConta.TUTOR, nameClean);
            return nova;
        });

        return createSession(conta);
    }

    /** Revoga a sessão do token no banco, invalidando o JWT mesmo antes da expiração. */
    @Transactional
    public void logout(String token) {
        if (isBlank(token)) {
            return;
        }
        sessaoRepository.findByTokenHash(jwtUtil.hashToken(token)).ifPresent(sessao -> {
            sessao.setDataRevogacao(LocalDateTime.now());
            sessaoRepository.save(sessao);
        });
    }

    private ContaAcesso criarContaComCredencial(String email, String telefone, TipoConta tipo, String senha) {
        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(email);
        conta.setTelefone(telefone);
        conta.setTipoConta(tipo.name());
        conta.setStatusConta(StatusConta.ATIVA.name());
        conta = contaRepository.save(conta);

        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(senha));
        cred.setTentativasFalhas(0);
        cred.setTrocaSenhaObrigatoria(false);
        credencialRepository.save(cred);
        return conta;
    }

    /** CPF, CNPJ e CRMV são únicos (uk_tipo_valor): avisa o usuário em vez de estourar a constraint. */
    private void validarIdentificadorLivre(String tipo, String valor) {
        if (!isBlank(valor) && identRepository.findByTipoIdentificadorAndValorHash(tipo, valor.trim()).isPresent()) {
            throw new BusinessException(tipo + " já cadastrado no sistema.");
        }
    }

    private void salvarIdentificador(ContaAcesso conta, String tipo, String valor) {
        if (isBlank(valor)) {
            return;
        }
        IdentificadorAcesso ident = new IdentificadorAcesso();
        ident.setContaAcesso(conta);
        ident.setTipoIdentificador(tipo);
        ident.setValorHash(valor.trim());
        identRepository.save(ident);
    }

    private void criarPerfil(ContaAcesso conta, TipoConta tipo, String nome) {
        switch (tipo) {
            case TUTOR -> {
                Tutor tutor = new Tutor();
                tutor.setContaAcesso(conta);
                tutor.setNome(nome);
                tutorRepository.save(tutor);
            }
            case VETERINARIO -> {
                Veterinario vet = new Veterinario();
                vet.setContaAcesso(conta);
                vet.setNome(nome);
                vet.setEspecialidade(ESPECIALIDADE_PADRAO);
                vet.setSituacaoProfissional("REGULAR");
                vetRepository.save(vet);
            }
            case CLINICA -> {
                Clinica clinica = new Clinica();
                clinica.setContaAcesso(conta);
                clinica.setRazaoSocial(nome);
                clinica.setNomeFantasia(nome);
                clinica.setAtiva(true);
                clinicaRepository.save(clinica);
            }
        }
    }

    private void registrarTentativaFalha(Credencial cred) {
        int tentativas = (cred.getTentativasFalhas() != null ? cred.getTentativasFalhas() : 0) + 1;
        cred.setTentativasFalhas(tentativas);
        if (tentativas >= MAX_TENTATIVAS) {
            cred.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
        }
        credencialRepository.save(cred);
    }

    private AuthResponse createSession(ContaAcesso conta) {
        String token = jwtUtil.generateToken(conta.getIdConta(), conta.getTipoConta());

        Sessao sessao = new Sessao();
        sessao.setContaAcesso(conta);
        sessao.setTokenHash(jwtUtil.hashToken(token));
        sessao.setDataExpiracao(jwtUtil.extractExpiration(token).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        sessaoRepository.save(sessao);

        return new AuthResponse(token, conta.getTipoConta());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
