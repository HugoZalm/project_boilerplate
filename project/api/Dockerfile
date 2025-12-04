ARG MAVEN_VERSION TOMCAT_VERSION
FROM maven:${MAVEN_VERSION} AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:${TOMCAT_VERSION}
# FROM tomcat:10-jdk21-temurin
COPY --from=build /app/target/api.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
