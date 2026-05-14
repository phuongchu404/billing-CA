FROM eclipse-temurin:17-jre-jammy

ENV APP_HOME=/app
ENV JAVA_OPTS=""
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/app/config/

WORKDIR ${APP_HOME}

RUN groupadd --system spring \
    && useradd --system --gid spring --create-home spring \
    && mkdir -p /app/config /app/logs \
    && chown -R spring:spring /app

COPY backend/*.jar /app/app.jar

USER spring

EXPOSE 9292

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
