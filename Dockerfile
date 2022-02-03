FROM registry-docker.riachuelo.net:5000/corporativo/openjdk:11

MAINTAINER Riachuelo Developer Team

VOLUME /config

ADD target/rchlo-microservices.jar rchlo-microservices.jar

COPY target/classes/logback.xml /config/

COPY target/classes/config/* /config/

ENV JAVA_OPTS="-Xmx256m -Xms256m -XX:MetaspaceSize=48m -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dlogging.config=file:/config/logback.xml -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar rchlo-microservices.jar" ]