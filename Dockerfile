FROM maven:3.8.1-jdk-11-slim as maven

WORKDIR /usr/src

COPY . /usr/src

ARG NEXUS_PASSWORD
ARG NEXUS_USERNAME

ENV NEXUS_PASSWORD $NEXUS_PASSWORD
ENV NEXUS_USERNAME $NEXUS_USERNAME

RUN mvn clean install -DskipTests --settings=./a2d2-settings.xml
RUN for file in /usr/src/services/*; do mvn clean install -f "$file" --settings=a2d2-settings.xml -Dmaven.repo.local=client_repo;   done 

FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as final

WORKDIR /app

ENV HEALTHCHECKURL http://localhost:8080/actuator/health
ARG JAVA_OPTS

COPY --from=maven /usr/src/a2d2-api/target/a2d2-api.war /app/a2d2-api.war
COPY --from=maven /usr/src/client_repo /root/.m2/repository
COPY --from=maven /usr/src/services /app/services

RUN apk --no-cache add curl
HEALTHCHECK --interval=60s --timeout=30s --start-period=30s --retries=3 CMD curl -f $HEALTHCHECKURL 2>&1 | grep UP || exit 1

EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar a2d2-api.war"]
