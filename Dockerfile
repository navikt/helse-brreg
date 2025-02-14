FROM eclipse-temurin:17-ubi9-minimal

WORKDIR /app
COPY build/libs/*.jar /app/

USER root
RUN mkdir /brreg

ADD https://data.brreg.no/enhetsregisteret/api/underenheter/lastned /brreg/underenheter_alle.json.gz
RUN gunzip /brreg/underenheter_alle.json.gz
RUN touch /brreg/underenheter_alle.json

ADD https://data.brreg.no/enhetsregisteret/api/enheter/lastned /brreg/enheter_alle.json.gz
RUN gunzip /brreg/enheter_alle.json.gz
RUN touch /brreg/enheter_alle.json

RUN groupadd --system --gid 1069 apprunner
RUN useradd --system --home-dir "/app/" --uid 1069 --gid apprunner apprunner

RUN chown -R 1069 /brreg
RUN chmod 775 /brreg

ENV TZ="Europe/Oslo"
EXPOSE 8080
USER apprunner
CMD ["java", "-jar", "/app/app.jar", "-XX:MaxRAMPercentage=75", "-Dlogback.configurationFile=logback.xml"]
