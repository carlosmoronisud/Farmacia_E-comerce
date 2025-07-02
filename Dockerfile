
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod -R 777 ./mvnw

RUN ./mvnw install -DskipTests

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21-jdk

VOLUME /tmp# Estágio de Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/app

# Copia os arquivos de configuração do Maven e o código fonte
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Garante que o script mvnw seja executável
RUN chmod +x ./mvnw

# Compila o projeto e pula os testes
RUN ./mvnw install -DskipTests

# Cria o diretório de dependências e extrai as dependências do JAR
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Estágio Final (Runtime)
FROM eclipse-temurin:21-jdk

# Define um volume para arquivos temporários (útil para logs, etc.)
VOLUME /tmp

# Argumento para o diretório de dependências
ARG DEPENDENCY=/workspace/app/target/dependency

# Copia os arquivos necessários do estágio de build para a imagem final
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Define o ponto de entrada da aplicação
# A classe principal foi corrigida para com.generation.farmacia.FarmaciaApplication
ENTRYPOINT ["java","-cp","app:app/lib/*","com.generation.farmacia.FarmaciaApplication"]

ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.generation.farmacia.FarmaciaApplication"]