FROM eclipse-temurin:17.0.18_8-jre-alpine AS builder

RUN adduser --system bank-user && addgroup --system bank-group && adduser \
    bank-user bank-group
USER bank-user

WORKDIR extracted
ADD target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17.0.18_8-jre-alpine
WORKDIR application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
EXPOSE 8080

#use it for after springboot 3.2
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
