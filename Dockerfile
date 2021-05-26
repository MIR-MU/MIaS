ARG VERSION_OPENJDK=8

FROM openjdk:${VERSION_OPENJDK}

ARG LIB_DIRNAME
ARG JAR_FILENAME

ENV JAVA_OPTS=""

COPY ${LIB_DIRNAME} /opt/lib
COPY ${JAR_FILENAME} /opt/MIaS.jar

ENTRYPOINT ["java", "-classpath", "/opt/lib/*.jar", "-jar", "/opt/MIaS.jar"]
CMD ["-stats", "-overwrite", "/dataset", "/dataset"]
