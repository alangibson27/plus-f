FROM openjdk:8-jre
RUN mkdir plusf
RUN groupadd -r plusf && useradd --no-log-init -r -g plusf plusf && chown plusf:plusf plusf
USER plusf
COPY build/distributions/discovery.zip .
RUN unzip discovery.zip -d plusf

EXPOSE 7070/udp
CMD ["plusf/discovery/bin/discovery", "7070"]