# Use the official OpenJDK image as the base image
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/memebot-0.0.1-SNAPSHOT.jar /app/your-app.jar

# Expose the application port (replace 8080 with your actual application port)
EXPOSE 8080

# Set the entrypoint command to run your application
ENTRYPOINT ["java", "-jar", "/app/your-app.jar"]
