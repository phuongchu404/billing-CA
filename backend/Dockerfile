# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY thirdparty-api/pom.xml thirdparty-api/
COPY subscription-provider/pom.xml subscription-provider/
COPY plan-mng-web/pom.xml plan-mng-web/

RUN mvn dependency:go-offline -B --no-transfer-progress

COPY thirdparty-api/src thirdparty-api/src
COPY subscription-provider/src subscription-provider/src
COPY plan-mng-web/src plan-mng-web/src

RUN mvn clean package -DskipTests -B --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

ENV APP_HOME=/app
ENV LOG_HOME=/app/logs
ENV JAVA_OPTS=""

WORKDIR ${APP_HOME}

RUN groupadd --system spring && useradd --system --gid spring --create-home spring

COPY --from=builder /build/plan-mng-web/target/plan-mng-web-*.jar app.jar

RUN mkdir -p ${LOG_HOME} \
    && chown -R spring:spring ${APP_HOME}

USER spring

EXPOSE 9292

VOLUME ["/app/logs"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
