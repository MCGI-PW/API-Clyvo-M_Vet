package com.clyvo.veterinary.config;

import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.ModalidadeConsulta;
import com.clyvo.veterinary.models.enums.StatusAutorizacao;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.models.enums.StatusVinculo;
import com.clyvo.veterinary.models.enums.TipoConta;
import com.clyvo.veterinary.repositories.*;
import com.clyvo.veterinary.services.AuthService;
import com.clyvo.veterinary.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Carga de dados de demonstração (somente perfis local/dev/demo).
 * As contas são criadas pelo próprio {@link AuthService}, garantindo as mesmas regras do cadastro real.
 */
@Component
@Profile({"local", "dev", "demo"})
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;
    private final EspecieRepository especieRepository;
    private final RacaRepository racaRepository;
    private final ContaAcessoRepository contaRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository vetRepository;
    private final ClinicaRepository clinicaRepository;
    private final VeterinarioClinicaRepository vetClinicaRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final NotificacaoService notificacaoService;
    private final String demoPassword;

    public DataInitializer(AuthService authService,
                           EspecieRepository especieRepository,
                           RacaRepository racaRepository,
                           ContaAcessoRepository contaRepository,
                           TutorRepository tutorRepository,
                           VeterinarioRepository vetRepository,
                           ClinicaRepository clinicaRepository,
                           VeterinarioClinicaRepository vetClinicaRepository,
                           PetRepository petRepository,
                           ConsultaRepository consultaRepository,
                           ProntuarioRepository prontuarioRepository,
                           AutorizacaoAcessoPetRepository autorizacaoRepository,
                           NotificacaoService notificacaoService,
                           @Value("${app.demo.password:senha123}") String demoPassword) {
        this.authService = authService;
        this.especieRepository = especieRepository;
        this.racaRepository = racaRepository;
        this.contaRepository = contaRepository;
        this.tutorRepository = tutorRepository;
        this.vetRepository = vetRepository;
        this.clinicaRepository = clinicaRepository;
        this.vetClinicaRepository = vetClinicaRepository;
        this.petRepository = petRepository;
        this.consultaRepository = consultaRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.notificacaoService = notificacaoService;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Raca golden = seedRaca("Golden Retriever", seedEspecie("Cão"));

        Tutor ana = seedConta("Ana Silva", "ana.silva@email.com", "11999991111", TipoConta.TUTOR, "12345678901",
                tutorRepository::findByContaAcessoIdConta);
        Clinica central = seedConta("Clínica Clyvo Central", "contato@matriz.com", "1133334444", TipoConta.CLINICA, "12345678000199",
                clinicaRepository::findByContaAcessoIdConta);
        Veterinario drCarlos = seedConta("Dr. Carlos Mendes", "dr.carlos@veterinaria.com", "11988882222", TipoConta.VETERINARIO, "98765432100",
                vetRepository::findByContaAcessoIdConta);

        // Veterinária ainda sem vínculo: permite demonstrar "vincular à equipe" e "transferir atendimento"
        seedConta("Dra. Camila Rocha", "dra.camila@veterinaria.com", "11977773333", TipoConta.VETERINARIO, "11122233344",
                vetRepository::findByContaAcessoIdConta);

        seedVinculo(drCarlos, central);
        Pet thor = seedPet("Thor", ana, golden);

        // Uma consulta futura (fila do veterinário) e uma concluída com prontuário (histórico do tutor)
        if (consultaRepository.findByPetTutorIdTutor(ana.getIdTutor()).isEmpty()) {
            seedConsulta(thor, drCarlos, central, LocalDateTime.now().plusDays(2).withHour(14).withMinute(0), StatusConsulta.AGENDADO);
            Consulta passada = seedConsulta(thor, drCarlos, central, LocalDateTime.now().minusDays(20).withHour(10).withMinute(0), StatusConsulta.CONCLUIDA);
            seedProntuario(passada, "Diagnóstico: Check-up anual sem alterações.\nPrescrição: Vermífugo dose única.\nObservações: Retorno em 12 meses.");
            seedAutorizacao(thor, drCarlos, central, ana);

            // Caixa de entrada com os mesmos avisos que a aplicação gera nesses fluxos
            notificacaoService.notificar(ana.getContaAcesso(),
                    "A consulta do pet Thor foi finalizada. O prontuário já está disponível no seu painel.");
            notificacaoService.notificar(ana.getContaAcesso(),
                    "Sua consulta com " + drCarlos.getNome() + " na unidade " + central.getNomeFantasia() + " foi agendada com sucesso!");
            notificacaoService.notificar(drCarlos.getContaAcesso(), "Nova consulta agendada para o paciente Thor.");
        }
    }

    /** Cria a conta via AuthService (se ainda não existir) e devolve o perfil correspondente. */
    private <T> T seedConta(String nome, String email, String telefone, TipoConta tipo, String documento,
                            Function<UUID, Optional<T>> perfilPorConta) {
        ContaAcesso conta = contaRepository.findByEmail(email).orElseGet(() -> {
            RegisterRequest req = new RegisterRequest();
            req.setName(nome);
            req.setEmail(email);
            req.setPhone(telefone);
            req.setPassword(demoPassword);
            req.setRole(tipo.name());
            req.setDocument(documento);
            return authService.criarConta(req);
        });
        return perfilPorConta.apply(conta.getIdConta())
                .orElseThrow(() -> new IllegalStateException("Perfil " + tipo + " não encontrado para " + email));
    }

    private Especie seedEspecie(String nome) {
        return especieRepository.findByNome(nome).orElseGet(() -> {
            Especie esp = new Especie();
            esp.setNome(nome);
            return especieRepository.save(esp);
        });
    }

    private Raca seedRaca(String nome, Especie especie) {
        return racaRepository.findByNome(nome).orElseGet(() -> {
            Raca r = new Raca();
            r.setNome(nome);
            r.setEspecie(especie);
            return racaRepository.save(r);
        });
    }

    private void seedVinculo(Veterinario vet, Clinica clinica) {
        if (vetClinicaRepository.findByVeterinarioIdVeterinarioAndStatusVinculo(vet.getIdVeterinario(), StatusVinculo.ATIVO.name()).isEmpty()) {
            VeterinarioClinica vinculo = new VeterinarioClinica();
            vinculo.setVeterinario(vet);
            vinculo.setClinica(clinica);
            vinculo.setDataInicio(LocalDate.now());
            vinculo.setStatusVinculo(StatusVinculo.ATIVO.name());
            vetClinicaRepository.save(vinculo);
        }
    }

    private Pet seedPet(String nome, Tutor tutor, Raca raca) {
        return petRepository.findByTutorIdTutor(tutor.getIdTutor()).stream().findFirst().orElseGet(() -> {
            Pet pet = new Pet();
            pet.setNome(nome);
            pet.setTutor(tutor);
            pet.setRaca(raca);
            pet.setDataNascimento(LocalDate.of(2022, 5, 10));
            pet.setSexo("MACHO");
            pet.setAtivo(true);
            return petRepository.save(pet);
        });
    }

    private Consulta seedConsulta(Pet pet, Veterinario vet, Clinica clinica, LocalDateTime dataHora, StatusConsulta status) {
        Consulta c = new Consulta();
        c.setPet(pet);
        c.setVeterinario(vet);
        c.setClinica(clinica);
        c.setDataHora(dataHora);
        c.setModalidade(ModalidadeConsulta.PRESENCIAL.name());
        c.setStatus(status.name());
        return consultaRepository.save(c);
    }

    /** Consentimento LGPD do tutor, como o gerado automaticamente no agendamento pela aplicação. */
    private void seedAutorizacao(Pet pet, Veterinario vet, Clinica clinica, Tutor tutor) {
        AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
        auth.setPet(pet);
        auth.setVeterinario(vet);
        auth.setClinica(clinica);
        auth.setContaAutorizador(tutor.getContaAcesso());
        auth.setDataAutorizacao(LocalDateTime.now().minusDays(20));
        auth.setStatus(StatusAutorizacao.ATIVA.name());
        autorizacaoRepository.save(auth);
    }

    private void seedProntuario(Consulta consulta, String notas) {
        Prontuario p = new Prontuario();
        p.setConsulta(consulta);
        p.setVeterinario(consulta.getVeterinario());
        p.setNotasClinicas(notas);
        p.setDataRegistro(consulta.getDataHora().plusMinutes(40));
        prontuarioRepository.save(p);
    }
}
