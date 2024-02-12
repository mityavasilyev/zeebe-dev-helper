# Use the Gradle image to create a build artifact
FROM --platform=linux/amd64 gradle:jdk17-alpine as build
WORKDIR /home/gradle
ADD ./ ./
RUN gradle clean assemble

# Base image for the Zeebe broker
FROM --platform=linux/amd64 camunda/zeebe:latest as zeebe

FROM --platform=linux/amd64 gradle:jdk21-alpine
COPY --from=build home/gradle/build/libs/*.jar service.jar
COPY --from=build home/gradle/build/resources/main/*.bpmn process_template.bpmn
COPY --from=zeebe /usr/local/zeebe /usr/local/zeebe

# Switch to root user
USER root

# Expose port  8080 for your application and port  26500 for Zeebe broker
EXPOSE  8080  26500

RUN apk add --no-cache curl tar nodejs npm

# Install zbctl globally
RUN npm install -g zbctl

ENTRYPOINT ["sh", "-c", "/usr/local/zeebe/bin/broker & java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar service.jar"]
