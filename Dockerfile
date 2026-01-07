
FROM gcr.io/distroless/base-debian12:nonroot

WORKDIR /app

COPY target/chat-api /app/chat-api

EXPOSE 8080

USER nonroot:nonroot

ENTRYPOINT ["/app/chat-api"]