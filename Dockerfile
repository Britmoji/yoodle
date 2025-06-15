FROM gradle:8-alpine AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:21-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/yoodle-*-all.jar bot.jar

RUN adduser -D -g '' bot && \
    chown -R bot:bot /app && \
    chmod 755 /app

USER bot
ENTRYPOINT ["java", "-jar", "bot.jar"]
