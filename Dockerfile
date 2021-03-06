FROM ubuntu:15.10

MAINTAINER tanmay ambre

RUN apt-get update && apt-get install -y --no-install-recommends openjdk-8-jdk

RUN java -version
RUN which java

RUN groupadd -r mongodb && useradd -r -g mongodb mongodb

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.2 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.2.list
RUN apt-get update && apt-get install -y --force-yes --no-install-recommends mongodb-org

RUN mkdir -p /data/db /data/configdb /logs \
	&& chown -R mongodb:mongodb /data/db /data/configdb 

VOLUME /data/db /data/configdb /logs

RUN mkdir -p /usr/local/psd2

COPY oauth2server/target/oauth2server-0.0.1-SNAPSHOT.jar /usr/local/psd2/
COPY psd2api/target/psd2api-0.0.1-SNAPSHOT.jar /usr/local/psd2/
COPY psd2demoapp/target/psd2demoapp-0.0.1-SNAPSHOT.jar /usr/local/psd2/

COPY startup.sh /usr/local/psd2

RUN ls -al /usr/local/psd2/

RUN adduser mongodb root

RUN which java
RUN which mongod

EXPOSE 8081
EXPOSE 8082
EXPOSE 8084
EXPOSE 27017

WORKDIR /usr/local/psd2

ENTRYPOINT ["/usr/local/psd2/startup.sh"]

