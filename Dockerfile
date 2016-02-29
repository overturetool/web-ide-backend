#FROM centos
FROM java:8-jre
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

WORKDIR /app
ADD . /app
COPY workspace/ target/universal/stage/workspace/

#ENV JAVA_HOME JAVA_ENV_ENV_JAVA_HOME \
#    JAVA_VERSION JAVA_ENV_ENV_JAVA_VERSION \
#    JAVA_DEBIAN_VERSION JAVA_ENV_ENV_JAVA_DEBIAN_VERSION \

#RUN sbt clean compile stage
CMD ["target/universal/stage/bin/overture_webide"]