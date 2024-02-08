# Use the Gradle image to create a build artifact
FROM --platform=linux/amd64 gradle:jdk17-alpine as build
WORKDIR /home/gradle
ADD ./ ./
RUN gradle build

FROM --platform=linux/amd64 gradle:jdk21-alpine
COPY --from=build home/gradle/build/libs/*.jar service.jar

# Switch to root user
USER root

# Expose port  8080
EXPOSE  8080

RUN apk add --no-cache curl tar nodejs npm

# Install zbctl globally
RUN npm install -g zbctl

# Set the entrypoint to run the JAR file
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar service.jar"]
