FROM openjdk:17

ARG JAR_FILE=target/*.jar

ADD ${JAR_FILE} iOpportunity-service.jar

ENTRYPOINT ["java","-jar","iOpportunity-service.jar"]

EXPOSE 8085