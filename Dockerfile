#step 1: BUILD
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
ARG RELEASE_TAG

#copy source code
COPY import_files/ import_files/
COPY src/ src/
COPY pom.xml .
COPY .env .

# create folder to save file when import file excel
RUN mkdir -p /tmp/UploadFiles/forecast_pricing

# build file .war
RUN mvn clean install -DskipTests -DfileName=$RELEASE_TAG

# step 2: RUN
FROM eclipse-temurin:17-jdk-alpine
VOLUME /app

COPY --from=build /app/target/$RELEASE_TAG.war .
COPY locale/ /locale/


EXPOSE 8080

# create folder to save file when import file excel
RUN mkdir -p /opt/hysteryale/uploadFiles/forecast_pricing
RUN mkdir -p /opt/hysteryale/uploadFiles/booked
RUN mkdir -p /opt/hysteryale/uploadFiles/shipment
RUN mkdir -p /opt/hysteryale/uploadFiles/macro
RUN mkdir -p /opt/hysteryale/uploadFiles/part
RUN mkdir -p /opt/hysteryale/uploadFiles/novo
RUN mkdir -p /opt/hysteryale/uploadFiles/competitor
RUN mkdir -p /opt/hysteryale/uploadFiles/product
RUN mkdir -p /opt/hysteryale/uploadFiles/exchange_rate
RUN mkdir -p /opt/hysteryale/uploadFiles/residual_value
RUN mkdir -p /opt/hysteryale/uploadFiles/dealer_product
RUN mkdir -p /opt/hysteryale/uploadFiles/dealer

RUN mkdir -p /opt/hysteryale/images/product/
RUN mkdir -p /opt/hysteryale/images/part/
# run
CMD java -jar $RELEASE_TAG.war
