FROM navikt/java:12

COPY build/libs/*.jar ./

USER root

RUN mkdir /tmp/brreg

ADD https://data.brreg.no/enhetsregisteret/api/underenheter/lastned /tmp/brreg/underenheter_alle.json.gz
RUN gunzip /tmp/brreg/underenheter_alle.json.gz
RUN touch /tmp/brreg/underenheter_alle.json

ADD https://data.brreg.no/enhetsregisteret/api/enheter/lastned /tmp/brreg/enheter_alle.json.gz
RUN gunzip /tmp/brreg/enheter_alle.json.gz
RUN touch /tmp/brreg/enheter_alle.json

RUN chown -R apprunner /tmp/brreg

USER apprunner

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh
