package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
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

    public AuthService(ContaAcessoRepository contaRepository, CredencialRepository credencialRepository, IdentificadorAcessoRepository identRepository, TutorRepository tutorRepository, VeterinarioRepository vetRepository, ClinicaRepository clinicaRepository, SessaoRepository sessaoRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
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
        if (contaRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email ja em uso");
        }

        // 1. Criar ContaAcesso
        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(request.getEmail());
        conta.setTelefone(request.getPhone());
        conta.setTipoConta(request.getRole());
        conta.setStatusConta("ATIVA");
        conta = contaRepository.save(conta);

        // 2. Criar Credencial
        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(request.getPassword()));
        credencialRepository.save(cred);

        // 3. Criar Identificador (Ex: CPF/CNPJ)
        if (request.getDocument() != null && !request.getDocument().isEmpty()) {
            IdentificadorAcesso ident = new IdentificadorAcesso();
            ident.setContaAcesso(conta);
            ident.setTipoIdentificador(request.getRole().equals("CLINICA") ? "CNPJ" : "CPF");
            ident.setValorHash(request.getDocument()); // Em producao seria um hash real
            identRepository.save(ident);
        }

        // 4. Criar a Especializacao
        if ("TUTOR".equals(request.getRole())) {
            Tutor tutor = new Tutor();
            tutor.setContaAcesso(conta);
            tutor.setNome(request.getName());
            tutorRepository.save(tutor);
        } else if ("VETERINARIO".equals(request.getRole())) {
            Veterinario vet = new Veterinario();
            vet.setContaAcesso(conta);
            vet.setNome(request.getName());
            vetRepository.save(vet);
            // RegistroVeterinario seria salvo aqui com request.getCrmv()
        } else if ("CLINICA".equals(request.getRole())) {
            Clinica clinica = new Clinica();
            clinica.setContaAcesso(conta);
            clinica.setRazaoSocial(request.getName());
            clinica.setNomeFantasia(request.getName());
            clinicaRepository.save(clinica);
        }

        return createSession(conta);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        ContaAcesso conta = contaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        Credencial cred = credencialRepository.findByContaAcessoIdConta(conta.getIdConta())
                .orElseThrow(() -> new RuntimeException("Credencial invalida"));

        if (!passwordEncoder.matches(request.getPassword(), cred.getSenhaHash())) {
            throw new RuntimeException("Credenciais invalidas");
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
