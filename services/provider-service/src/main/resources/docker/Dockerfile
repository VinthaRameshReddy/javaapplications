FROM eclipse-temurin:21-jre
WORKDIR /app
COPY libs /app/libs
COPY ../../../../gradle/libs.versions.toml /app/libs.versions.toml
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 10110
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/libs/*.jar"]


