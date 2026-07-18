FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
COPY start.sh start.sh
RUN chmod +x start.sh
EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]
