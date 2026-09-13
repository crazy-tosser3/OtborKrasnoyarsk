FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY . .

RUN chmod +x mvnw && ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/balloon-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

CMD ["java", "-jar", "app.jar"]
