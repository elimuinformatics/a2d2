FROM maven:3.6.0-jdk-8-slim as maven

WORKDIR /usr/src

COPY ./services /usr/src/services
COPY a2d2-settings.xml /usr/src

ARG NEXUS_PASSWORD
ARG NEXUS_USERNAME

ENV NEXUS_PASSWORD $NEXUS_PASSWORD
ENV NEXUS_USERNAME $NEXUS_USERNAME

RUN for file in /usr/src/services/*; do mvn clean install -f "$file" --settings=a2d2-settings.xml -Dmaven.repo.local=client_repo;   done 

FROM openjdk:8 as final

WORKDIR /app

ENV HEALTHCHECKURL http://localhost:8080/actuator/health
ARG JAVA_OPTS

COPY ./a2d2-api/target/a2d2-api.war /app/a2d2-api.war
COPY --from=maven /usr/src/client_repo /root/.m2/repository
COPY --from=maven /usr/src/services /app/services

HEALTHCHECK --interval=60s --timeout=30s --start-period=30s --retries=3 CMD curl -f $HEALTHCHECKURL 2>&1 | grep UP || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar a2d2-api.war"]
