FROM registry-docker-k8s.riachuelo.net:5000/corporativo/openjdk:14
MAINTAINER Riachuelo Developer Team

ARG APPLICATION_NAME
ENV APPLICATION_NAME=$APPLICATION_NAME

ARG APPLICATION_PATH
ENV APPLICATION_PATH=$APPLICATION_PATH

ADD $APPLICATION_PATH/$APPLICATION_NAME $APPLICATION_NAME

VOLUME /config

#VOLUME /ssl
#COPY target/classes/ssl/* /ssl/

ENV JAVA_OPTS_MEMORY_MIN="128m"
ENV JAVA_OPTS_MEMORY_MAX="256m"

ENV JAVA_OPTS_METASPACE_MIN="48m"
ENV JAVA_OPTS_METASPACE_MAX="48m"


ENV JAVA_OPTS_MEMORY='-Xms$JAVA_OPTS_MEMORY_MIN -Xmx$JAVA_OPTS_MEMORY_MAX -XX:MetaspaceSize=$JAVA_OPTS_METASPACE_MIN -XX:MaxMetaspaceSize=$JAVA_OPTS_METASPACE_MAX'

ENV JAVA_OPTS="$JAVA_OPTS_MEMORY  -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+ShowCodeDetailsInExceptionMessages $JAVA_OPTS_CONFIG -Djava.security.egd=file:/dev/./urandom"

RUN echo $'#!/bin/sh \n\
java '$JAVA_OPTS' --enable-preview -jar '$APPLICATION_NAME'' > ./entrypoint.sh && \
    chmod +x ./entrypoint.sh

EXPOSE 8082
ENTRYPOINT ["./entrypoint.sh"]