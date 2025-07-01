FROM sbtscala/scala-sbt:openjdk-21_1.10.3_3.4.3 AS builder
WORKDIR /app
COPY . .
RUN sbt package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/scala-3.4.3/*.jar .
COPY --from=builder /app/src/main/scala .
ENTRYPOINT ["java", "-cp", "document-matrix_3.4.3-1.0.0.jar", "com.example.Cli"]
EXPOSE 8080
