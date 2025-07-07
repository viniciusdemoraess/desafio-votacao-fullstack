FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["./mvnw", "spring-boot:run"]
