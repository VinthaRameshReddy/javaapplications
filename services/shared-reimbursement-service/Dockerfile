# ================================
# Stage 1: Build & Extract Layers
# ================================
FROM azul/zulu-openjdk:21 as builder

# Set working directory
WORKDIR /application

# Copy built JAR (from Gradle output)
ARG JAR_FILE=libs
COPY build/libs/*.jar application.jar

# Extract Spring Boot layered jar for efficient layering
RUN java -Djarmode=layertools -jar application.jar extract


# ================================
# Stage 2: Runtime Image
# ================================
FROM azul/zulu-openjdk:21-jre

# Set timezone and working directory
ENV TZ=Asia/Kolkata
WORKDIR /application

# ✅ Define log directory environment variable for Logback
ENV LOG_DIR=/var/log/app/

# ✅ Create the log directory and set safe permissions
RUN mkdir -p ${LOG_DIR} && chmod -R 777 /var/log/app

# Copy extracted layers from builder
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

# Expose the application port
EXPOSE 10092


# ✅ Entry point for Spring Boot layered jar
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
