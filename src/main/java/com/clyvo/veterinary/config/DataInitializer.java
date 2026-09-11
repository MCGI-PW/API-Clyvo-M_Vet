package com.clyvo.veterinary.config;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Profile;

@Component
@Profile({"local", "dev"})
public class DataInitializer implements CommandLineRunner {

    private final EspecieRepository especieRepository;
    private final RacaRepository racaRepository;
    private final ContaAcessoRepository contaRepository;
    private final CredencialRepository credencialRepository;
    private final IdentificadorAcessoRepository identRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository vetRepository;
    private final ClinicaRepository clinicaRepository;
    private final VeterinarioClinicaRepository vetClinicaRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EspecieRepository especieRepository,
                           RacaRepository racaRepository,
                           ContaAcessoRepository contaRepository,
                           CredencialRepository credencialRepository,
                           IdentificadorAcessoRepository identRepository,
                           TutorRepository tutorRepository,
                           VeterinarioRepository vetRepository,
                           ClinicaRepository clinicaRepository,
                           VeterinarioClinicaRepository vetClinicaRepository,
                           PetRepository petRepository,
                           ConsultaRepository consultaRepository,
                           PasswordEncoder passwordEncoder) {
        this.especieRepository = especieRepository;
        this.racaRepository = racaRepository;
        this.contaRepository = contaRepository;
        this.credencialRepository = credencialRepository;
        this.identRepository = identRepository;
        this.tutorRepository = tutorRepository;
        this.vetRepository = vetRepository;
        this.clinicaRepository = clinicaRepository;
        this.vetClinicaRepository = vetClinicaRepository;
        this.petRepository = petRepository;
        this.consultaRepository = consultaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Popular Espécies e Raças se não existirem
        Especie cao = seedEspecie("Cão");
        Especie gato = seedEspecie("Gato");
        Especie ave = seedEspecie("Ave");

        Raca golden = seedRaca("Golden Retriever", cao);
        seedRaca("Poodle", cao);
        seedRaca("Bulldog Francês", cao);
        seedRaca("Vira-lata (SRD)", cao);

        seedRaca("Siamês", gato);
        seedRaca("Persa", gato);
        seedRaca("Maine Coon", gato);

        seedRaca("Calopsita", ave);
        seedRaca("Canário", ave);

        // 2. Criar Tutor Padrão: ana.silva@email.com / senha123
        Tutor tutorAna = seedTutor("Ana Silva", "ana.silva@email.com", "senha123", "11999991111", "12345678901");

        // 3. Criar Clínica Padrão: contato@matriz.com / senha123
        Clinica clinicaCentral = seedClinica("Clínica Clyvo Central", "contato@matriz.com", "senha123", "1133334444", "12345678000199");

        // 4. Criar Veterinário Padrão: dr.carlos@veterinaria.com / senha123
        Veterinario drCarlos = seedVeterinario("Dr. Carlos Mendes", "dr.carlos@veterinaria.com", "senha123", "11988882222", "98765432100", "CRMV-SP 12345", "Clínica Geral");

        // 5. Vincular Dr. Carlos à Clínica Central
        seedVinculo(drCarlos, clinicaCentral);

        // 6. Cadastrar Pet para Ana: Thor (Golden Retriever)
        Pet thor = seedPet("Thor", tutorAna, golden, LocalDate.of(2022, 5, 10), "MACHO");

        // 7. Criar Consulta de Exemplo para demonstração na web
        seedConsulta(thor, drCarlos, clinicaCentral, LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
    }

    private Especie seedEspecie(String nome) {
        return especieRepository.findByNome(nome).orElseGet(() -> {
            Especie esp = new Especie();
            esp.setNome(nome);
            return especieRepository.save(esp);
        });
    }

    private Raca seedRaca(String nome, Especie especie) {
        Optional<Raca> existente = racaRepository.findByNome(nome);
        if (existente.isPresent()) {
            return existente.get();
        }
        Raca r = new Raca();
        r.setNome(nome);
        r.setEspecie(especie);
        return racaRepository.save(r);
    }

    private Tutor seedTutor(String nome, String email, String senha, String telefone, String cpf) {
        Optional<ContaAcesso> contaOpt = contaRepository.findByEmail(email);
        if (contaOpt.isPresent()) {
            return tutorRepository.findByContaAcessoIdConta(contaOpt.get().getIdConta()).orElse(null);
        }

        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(email);
        conta.setTelefone(telefone);
        conta.setTipoConta("TUTOR");
        conta.setStatusConta("ATIVA");
        conta = contaRepository.save(conta);

        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(senha));
        credencialRepository.save(cred);

        IdentificadorAcesso ident = new IdentificadorAcesso();
        ident.setContaAcesso(conta);
        ident.setTipoIdentificador("CPF");
        ident.setValorHash(cpf);
        identRepository.save(ident);

        Tutor tutor = new Tutor();
        tutor.setContaAcesso(conta);
        tutor.setNome(nome);
        return tutorRepository.save(tutor);
    }

    private Clinica seedClinica(String nome, String email, String senha, String telefone, String cnpj) {
        Optional<ContaAcesso> contaOpt = contaRepository.findByEmail(email);
        if (contaOpt.isPresent()) {
            return clinicaRepository.findByContaAcessoIdConta(contaOpt.get().getIdConta()).orElse(null);
        }

        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(email);
        conta.setTelefone(telefone);
        conta.setTipoConta("CLINICA");
        conta.setStatusConta("ATIVA");
        conta = contaRepository.save(conta);

        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(senha));
        credencialRepository.save(cred);

        IdentificadorAcesso ident = new IdentificadorAcesso();
        ident.setContaAcesso(conta);
        ident.setTipoIdentificador("CNPJ");
        ident.setValorHash(cnpj);
        identRepository.save(ident);

        Clinica clinica = new Clinica();
        clinica.setContaAcesso(conta);
        clinica.setRazaoSocial(nome + " LTDA");
        clinica.setNomeFantasia(nome);
        return clinicaRepository.save(clinica);
    }

    private Veterinario seedVeterinario(String nome, String email, String senha, String telefone, String cpf, String crmv, String especialidade) {
        Optional<ContaAcesso> contaOpt = contaRepository.findByEmail(email);
        if (contaOpt.isPresent()) {
            return vetRepository.findByContaAcessoIdConta(contaOpt.get().getIdConta()).orElse(null);
        }

        ContaAcesso conta = new ContaAcesso();
        conta.setEmail(email);
        conta.setTelefone(telefone);
        conta.setTipoConta("VETERINARIO");
        conta.setStatusConta("ATIVA");
        conta = contaRepository.save(conta);

        Credencial cred = new Credencial();
        cred.setContaAcesso(conta);
        cred.setSenhaHash(passwordEncoder.encode(senha));
        credencialRepository.save(cred);

        IdentificadorAcesso ident = new IdentificadorAcesso();
        ident.setContaAcesso(conta);
        ident.setTipoIdentificador("CPF");
        ident.setValorHash(cpf);
        identRepository.save(ident);

        Veterinario vet = new Veterinario();
        vet.setContaAcesso(conta);
        vet.setNome(nome);
        vet.setEspecialidade(especialidade);
        vet.setSituacaoProfissional("REGULAR");
        return vetRepository.save(vet);
    }

    private void seedVinculo(Veterinario vet, Clinica clinica) {
        if (vet == null || clinica == null) return;
        if (vetClinicaRepository.findByVeterinarioIdVeterinarioAndStatusVinculo(vet.getIdVeterinario(), "ATIVO").isEmpty()) {
            VeterinarioClinica vinculo = new VeterinarioClinica();
            vinculo.setVeterinario(vet);
            vinculo.setClinica(clinica);
            vinculo.setStatusVinculo("ATIVO");
            vetClinicaRepository.save(vinculo);
        }
    }

    private Pet seedPet(String nome, Tutor tutor, Raca raca, LocalDate dataNasc, String sexo) {
        if (tutor == null) return null;
        if (petRepository.findByTutorIdTutor(tutor.getIdTutor()).isEmpty()) {
            Pet pet = new Pet();
            pet.setNome(nome);
            pet.setTutor(tutor);
            pet.setRaca(raca);
            pet.setDataNascimento(dataNasc);
            pet.setSexo(sexo);
            pet.setAtivo(true);
            return petRepository.save(pet);
        }
        return petRepository.findByTutorIdTutor(tutor.getIdTutor()).get(0);
    }

    private void seedConsulta(Pet pet, Veterinario vet, Clinica clinica, LocalDateTime dataHora) {
        if (pet == null || vet == null) return;
        if (consultaRepository.findByPetTutorIdTutor(pet.getTutor().getIdTutor()).isEmpty()) {
            Consulta c = new Consulta();
            c.setPet(pet);
            c.setVeterinario(vet);
            c.setClinica(clinica);
            c.setDataHora(dataHora);
            c.setModalidade("PRESENCIAL");
            c.setStatus("AGENDADO");
            consultaRepository.save(c);
        }
    }
}
