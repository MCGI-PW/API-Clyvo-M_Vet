# ============================================================================
# DOCKERFILE - CLYVO VETERINARY API (JAVA 21 LTS)
# Requisito 8.2: Execução estritamente NON-ROOT (sem privilégios administrativos)
# Padrão: Multi-stage Build otimizado para produção
# ============================================================================

# ----------------------------------------------------------------------------
# ESTÁGIO 1: Build da Aplicação com Maven e Eclipse Temurin 21
# ----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copia os descritores do Maven para cache eficiente de dependências
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Concede permissão de execução ao wrapper e baixa dependências em cache
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copia o código-fonte da aplicação
COPY src ./src

# Compila o JAR da aplicação pulando os testes unitários no build da imagem
RUN ./mvnw clean package -DskipTests -B

# ----------------------------------------------------------------------------
# ESTÁGIO 2: Imagem Final de Execução (Runtime Minimalista e Seguro)
# ----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runner

WORKDIR /app

# [SEGURANÇA - REQUISITO 8.2] Criação de usuário e grupo não-privilegiados (NON-ROOT)
# UID/GID 10001 garante isolamento contra escalada de privilégios no host
RUN addgroup --system --gid 10001 appgroup && \
    adduser --system --uid 10001 --ingroup appgroup --no-create-home appuser

# Copia o JAR compilado a partir do estágio de build
COPY --from=builder /build/target/*.jar app.jar

# Define a propriedade do arquivo para o usuário não-root
RUN chown -R appuser:appgroup /app

# Alterna para o usuário não-root (Obrigatório para conformidade de segurança Azure ACI)
USER 10001:10001

# Porta exposta pela aplicação
EXPOSE 8080

# Variáveis de ambiente padrão para a JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Comando de inicialização seguro
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
