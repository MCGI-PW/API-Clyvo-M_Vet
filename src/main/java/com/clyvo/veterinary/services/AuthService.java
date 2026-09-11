package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.exceptions.AccountBlockedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.TipoConta;
import com.clyvo.veterinary.repositories.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthService {
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
        // Validação estrita do papel (role)
        if (request.getRole() == null || !TipoConta.isValid(request.getRole())) {
            throw new BusinessException("Perfil de usuário inválido: " + request.getRole() + ". Valores aceitos: TUTOR, VETERINARIO, CLINICA.");
        }
        String roleNormalized = request.getRole().trim().toUpperCase();

        if (contaRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }

        // 1. Criar ContaAcesso
        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(request.getEmail().trim().toLowerCase());
        conta.setTelefone(request.getPhone());
        conta.setTipoConta(roleNormalized);
        conta.setStatusConta("ATIVA");
        conta = contaRepository.save(conta);

        // 2. Criar Credencial
        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(request.getPassword()));
        cred.setTentativasFalhas(0);
        cred.setTrocaSenhaObrigatoria(false);
        credencialRepository.save(cred);

        // 3. Criar Identificador (CPF / CNPJ / CRMV)
        String doc = request.getDocument();
        if (doc == null || doc.isBlank()) {
            if ("CLINICA".equals(roleNormalized) && request.getCnpj() != null && !request.getCnpj().isBlank()) {
                doc = request.getCnpj();
            }
        }

        if (doc != null && !doc.isBlank()) {
            IdentificadorAcesso ident = new IdentificadorAcesso();
            ident.setContaAcesso(conta);
            ident.setTipoIdentificador("CLINICA".equals(roleNormalized) ? "CNPJ" : "CPF");
            ident.setValorHash(doc.trim());
            identRepository.save(ident);
        }

        // Salvar CRMV se for veterinário
        if ("VETERINARIO".equals(roleNormalized) && request.getCrmv() != null && !request.getCrmv().isBlank()) {
            IdentificadorAcesso identCrmv = new IdentificadorAcesso();
            identCrmv.setContaAcesso(conta);
            identCrmv.setTipoIdentificador("CRMV");
            identCrmv.setValorHash(request.getCrmv().trim());
            identRepository.save(identCrmv);
        }

        // 4. Criar a Especialização
        if ("TUTOR".equals(roleNormalized)) {
            Tutor tutor = new Tutor();
            tutor.setContaAcesso(conta);
            tutor.setNome(request.getName());
            tutorRepository.save(tutor);
        } else if ("VETERINARIO".equals(roleNormalized)) {
            Veterinario vet = new Veterinario();
            vet.setContaAcesso(conta);
            vet.setNome(request.getName());
            vet.setEspecialidade("Clínica Geral");
            vet.setSituacaoProfissional("REGULAR");
            vetRepository.save(vet);
        } else if ("CLINICA".equals(roleNormalized)) {
            Clinica clinica = new Clinica();
            clinica.setContaAcesso(conta);
            clinica.setRazaoSocial(request.getName());
            clinica.setNomeFantasia(request.getName());
            clinica.setAtiva(true);
            clinicaRepository.save(clinica);
        }

        return createSession(conta);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String emailClean = (request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : "";

        ContaAcesso conta = contaRepository.findByEmail(emailClean)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha incorretos."));

        // Verificar status da conta
        if (!"ATIVA".equalsIgnoreCase(conta.getStatusConta())) {
            throw new AccountBlockedException("Esta conta está inativa ou bloqueada. Entre em contato com o suporte.");
        }

        Credencial cred = credencialRepository.findByContaAcessoIdConta(conta.getIdConta())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha incorretos."));

        // Verificar bloqueio temporário por tentativas excessivas
        if (cred.getBloqueadoAte() != null && cred.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            throw new AccountBlockedException("Conta temporariamente bloqueada devido a excesso de tentativas. Tente novamente mais tarde.");
        }

        // Validar senha
        if (!passwordEncoder.matches(request.getPassword(), cred.getSenhaHash())) {
            int tentativas = (cred.getTentativasFalhas() != null ? cred.getTentativasFalhas() : 0) + 1;
            cred.setTentativasFalhas(tentativas);
            if (tentativas >= 5) {
                cred.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
            }
            credencialRepository.save(cred);
            throw new BadCredentialsException("E-mail ou senha incorretos.");
        }

        // Sucesso: resetar contador de falhas e bloqueio
        if ((cred.getTentativasFalhas() != null && cred.getTentativasFalhas() > 0) || cred.getBloqueadoAte() != null) {
            cred.setTentativasFalhas(0);
            cred.setBloqueadoAte(null);
            credencialRepository.save(cred);
        }

        return createSession(conta);
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
}
