FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=build/libs/\*.jar
COPY ${JAR_FILE} chat-delete-links-bot.jar
ENTRYPOINT ["java", "-jar", "chat-delete-links-bot.jar"]