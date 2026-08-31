package com.clyvo.veterinary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Clyvo Veterinary API.
 *
 * <p>Esta classe inicializa o contexto do Spring Boot, que fará:
 * <ul>
 *   <li>Auto-configuração de todos os beans registrados</li>
 *   <li>Inicialização do servidor embarcado (Tomcat)</li>
 *   <li>Execução das migrations Flyway</li>
 *   <li>Configuração do Spring Security</li>
 * </ul>
 *
 * @author Clyvo Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ClyvoVeterinaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClyvoVeterinaryApplication.class, args);
    }
}
