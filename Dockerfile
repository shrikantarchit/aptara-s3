FROM openjdk:8
EXPOSE 7070
ADD target/filestorage-0.0.1-SNAPSHOT.jar filestorage-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","filestorage-0.0.1-SNAPSHOT.jar"]