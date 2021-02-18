ARG VERSION_OPENJDK=8

FROM openjdk:${VERSION_OPENJDK}
ARG JAR_FILENAME
COPY ${JAR_FILENAME} /MIaS.jar
ENTRYPOINT ["java", "-jar", "/MIaS.jar"]
CMD ["-stats", "-overwrite", "/dataset", "/dataset"]
