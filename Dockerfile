FROM ingensi/play-framework:latest
MAINTAINER Kasper Saaby <kdsaaby@gmail.com>

ADD . /app

RUN yum install -y attr
RUN setfattr -n user.pax.flags -v "mr" /usr/bin/java
