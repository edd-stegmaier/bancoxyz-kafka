FROM maven:3.9.9-eclipse-temurin-21 AS buildstage
WORKDIR /app
COPY pom.xml ./
COPY src/ src/
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=buildstage /app/target/auth-server-1.0.0.jar /app/app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
