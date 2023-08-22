FROM eclipse-temurin:17-jre-alpine
COPY /target/myjoboffers.jar /myjoboffers.jar
ENTRYPOINT ["java","-jar","/myjoboffers.jar"]