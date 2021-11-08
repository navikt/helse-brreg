FROM navikt/java:12

COPY build/libs/*.jar ./

USER root
RUN mkdir /brreg

ADD https://data.brreg.no/enhetsregisteret/api/underenheter/lastned /brreg/underenheter_alle.json.gz
RUN gunzip /brreg/underenheter_alle.json.gz
RUN touch /brreg/underenheter_alle.json

ADD https://data.brreg.no/enhetsregisteret/api/enheter/lastned /brreg/enheter_alle.json.gz
RUN gunzip /brreg/enheter_alle.json.gz
RUN touch /brreg/enheter_alle.json

RUN chown -R 1069 /brreg
RUN chmod 775 /brreg

USER apprunner

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh
