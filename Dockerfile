FROM eclipse-temurin:17 as builder

WORKDIR /app
COPY build/libs/*.jar /app/

RUN mkdir /tmp/brreg

#ADD ./brreg_test/underenheter_alle.json.gz /tmp/brreg/underenheter_alle.json.gz
ADD https://data.brreg.no/enhetsregisteret/api/underenheter/lastned /tmp/brreg/underenheter_alle.json.gz
RUN gunzip /tmp/brreg/underenheter_alle.json.gz
RUN touch /tmp/brreg/underenheter_alle.json

#ADD ./brreg_test/enheter_alle.json.gz /tmp/brreg/enheter_alle.json.gz
ADD https://data.brreg.no/enhetsregisteret/api/enheter/lastned /tmp/brreg/enheter_alle.json.gz
RUN gunzip /tmp/brreg/enheter_alle.json.gz
RUN touch /tmp/brreg/enheter_alle.json

FROM gcr.io/distroless/java17-debian12
COPY --from=builder /tmp/brreg/ /tmp/brreg/
COPY --from=builder /app/ /app/
ENV TZ="Europe/Oslo"
EXPOSE 8080
WORKDIR /app
CMD ["app.jar", "-XX:MaxRAMPercentage=75", "-Dlogback.configurationFile=logback.xml"]
