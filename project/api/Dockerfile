FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# FROM eclipse-temurin:21-jre
# WORKDIR /app
# COPY --from=build /app/target/wateralmanak-api.war /app/wateralmanak-api.war
# EXPOSE 8080
# CMD ["java", "-jar", "/app/wateralmanak-api.war"]

FROM tomcat:10-jdk21-temurin
COPY --from=build /app/target/wateralmanak-api.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080