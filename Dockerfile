#step 1: BUILD
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

#copy source code
COPY import_files/ import_files/
COPY src/ src/
COPY pom.xml .
COPY .env .

# create folder to save file when import file excel
RUN mkdir -p /tmp/UploadFiles/forecast_pricing

# build file .war
RUN mvn clean install

# step 2: RUN
FROM eclipse-temurin:17-jdk-alpine
VOLUME /app

COPY --from=build /app/target/ target/
EXPOSE 8080

# create folder to save file when import file excel
RUN mkdir -p /tmp/UploadFiles/forecast_pricing
RUN mkdir -p /opt/hysteryale/images/product/
RUN mkdir -p /opt/hysteryale/images/part/
# run
ENTRYPOINT ["java","-jar","target/hysteryale.war"]