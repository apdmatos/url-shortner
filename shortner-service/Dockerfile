FROM scottyengineering/java11

ADD src/main/resources/schema /schema

ADD build/libs/shortner-service-0.0.1-SNAPSHOT.jar /shortner-service.jar
ADD application.yml /

EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar", "/shortner-service.jar"]

