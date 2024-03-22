# Overview

This document provides step-by-step instructions for installing and configuring a Java Spring Boot application with a PostgreSQL database on an Ubuntu Server. This guide assumes you have a basic understanding of Linux and have access to a clean Ubuntu Server instance.
Prerequisites

Before you begin, ensure you have the following:

    Ubuntu Server installed and accessible.
    Java Development Kit (JDK) installed.
    PostgreSQL database server installed and configured.

## Steps

### Step 1: Install Java Development Kit (JDK)

#### Update the package index
`sudo apt update`

#### Install the default JDK (OpenJDK 17 or higher)
`sudo apt install default-jdk`

#### Verify the installation
`java -version`

### Step 2: Install PostgreSQL Database Server (13 or higher)

#### Update the package index
`sudo apt update`

#### Install PostgreSQL
`sudo apt install postgresql postgresql-contrib`

#### Start and enable the PostgreSQL service
`sudo systemctl start postgresql`

`sudo systemctl enable postgresql`

### Step 3: Create a PostgreSQL Database and User

#### Access the PostgreSQL interactive terminal
`sudo -u postgres psql`

#### Create a new database
`CREATE DATABASE hysteryale;`

#### Create a new database user
`CREATE USER hysteryale WITH PASSWORD 'your_password';`

#### Grant privileges to the user on the database
`GRANT ALL PRIVILEGES ON DATABASE hysteryale TO hysteryale;`

#### add extension to search  
```angular2html
\c hysteryale;

CREATE EXTENSION pg_trgm;
```

## Step 4: Clone the Spring Boot Application

### Install Git
`sudo apt install git`

#### Navigate to the folder /opt
`cd /opt`

### create root folder for project

```
mkdir hysteryale
cd hysteryale
```
### create root folder for upload files
```
mkdir -p /opt/hysteryale/uploadFiles/forecast_pricing
mkdir -p /opt/hysteryale/uploadFiles/booked
mkdir -p /opt/hysteryale/uploadFiles/shipment
mkdir -p /opt/hysteryale/uploadFiles/macro
mkdir -p /opt/hysteryale/uploadFiles/part
mkdir -p /opt/hysteryale/uploadFiles/novo
mkdir -p /opt/hysteryale/uploadFiles/competitor
mkdir -p /opt/hysteryale/uploadFiles/product
mkdir -p /opt/hysteryale/uploadFiles/exchange_rate
mkdir -p /opt/hysteryale/uploadFiles/booking_fpa


mkdir -p /opt/hysteryale/images/product/
mkdir -p /opt/hysteryale/images/part/
```

### set permission

```angular2html
sudo chown -R 777 /opt/hysteryale/images
sudo chown -R 777 /opt/hysteryale/uploadFiles
```

### Update Github Deployment Key

#### Clone your Spring Boot application repository
`git clone https://github.com/Phoenix-Software-Development/hyster-yale-backend.git`

`cd hyster-yale-backend`

## Step 5: Configure the Spring Boot Application

Edit the `application.properties` file in your Spring Boot application to configure the database connection:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/hysteryale`
spring.datasource.username=your_username`
spring.datasource.password=your_password`
```

## Step 6: Build and Package the Spring Boot Application

#### install maven
`sudo apt install maven`

#### Build the application
`mvn clean install`

## Step 7: Run the Spring Boot Application

#### Run the application
```
nohup java -Xmx12000m -Dspring.profiles.active=dev -jar hysteryale.war > log.txt 2>&1 & disown
```

## Step 8: Access the Application

If the frontend has been deployed successfully, try to open a web browser and navigate to http://localhost:8080 to access your Spring Boot application.
