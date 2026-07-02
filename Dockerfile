FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
# This copies your local 'wallet' folder into the container
COPY wallet /app/wallet
EXPOSE 7000
ENTRYPOINT ["java","-jar","/app/app.jar"]