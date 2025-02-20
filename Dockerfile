FROM eclipse-temurin:17 as builder

WORKDIR /app
COPY build/libs/*.jar /app/

RUN mkdir /brreg

ADD https://data.brreg.no/enhetsregisteret/api/underenheter/lastned /brreg/underenheter_alle.json.gz
RUN gunzip /brreg/underenheter_alle.json.gz
RUN touch /brreg/underenheter_alle.json

ADD https://data.brreg.no/enhetsregisteret/api/enheter/lastned /brreg/enheter_alle.json.gz
RUN gunzip /brreg/enheter_alle.json.gz
RUN touch /brreg/enheter_alle.json

FROM gcr.io/distroless/java17-debian12
COPY --from=builder /brreg/ /brreg/
COPY --from=builder /app/ /app/
ENV TZ="Europe/Oslo"
EXPOSE 8080
WORKDIR /app
CMD ["app.jar", "-XX:MaxRAMPercentage=75", "-Dlogback.configurationFile=logback.xml"]
