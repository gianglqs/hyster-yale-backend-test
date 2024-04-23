# Overview

This document provides step-by-step instructions for installing and configuring a Java Spring Boot application with a PostgreSQL database on an Ubuntu Server. This guide assumes you have a basic understanding of Linux and have access to a clean Ubuntu Server instance.
Prerequisites

Before you begin, ensure you have the following:

    Ubuntu Server installed and accessible.
    Java Development Kit (JDK) installed.
    PostgreSQL database server installed and configured.

## I. Configure environment

### 1. Install Java Development Kit (JDK)

- Update the package index
```
sudo apt update
```

- Install the default JDK (OpenJDK 17 or higher)
```
sudo apt install default-jdk 
 ```

- Verify the installation
```
java -version
```

### 2. Install PostgreSQL Database Server (13 or higher)

#### Update the package index
```
sudo apt update
```

#### Install PostgreSQL
```
sudo apt install postgresql postgresql-contrib
```

#### Start and enable the PostgreSQL service
```
sudo systemctl start postgresql

sudo systemctl enable postgresql
```

### 3. Create a PostgreSQL Database and User

- Access the PostgreSQL interactive terminal
```
sudo -u postgres psql
```

- Create a new database
```
CREATE DATABASE hysteryale
```
- Create a new database user and Grant privileges on the database
```
CREATE USER hysteryale WITH PASSWORD 'your_password'

GRANT ALL PRIVILEGES ON DATABASE hysteryale TO hysteryale
```

- add extension to search  
```
\c hysteryale;

CREATE EXTENSION pg_trgm;
```
### 4. Install Git
- Install git
```
sudo apt install git
```
- Config SSH: <a href="https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent">See details here</a>
## II. Configure Spring Boot Application

- Create root folder for project and change ownership for current user
```
sudo mkdir /opt/hysteryale/

sudo chown -R oem /opt/hysteryale/
```

- Clone repository
```
git clone git@github.com:Phoenix-Software-Development/hyster-yale-backend.git
```

- Create folder for upload files by executing script /scripts/init_folders.sh

- Copy folder `/opt/hysteryale/hyster-yale-backend/locale` to folder `/opt/hysteryale/`
```
cp -r /opt/hysteryale/hyster-yale-backend/locale /opt/hysteryale/
```

- Create file `.env` with template in `.env-template` and populate it
```
cp /opt/hysteryale/hyster-yale-backend/.env-template /opt/hysteryale/hyster-yale-backend/.env
```

## III. Build and Run Spring Boot Application

- install maven
```
sudo apt install maven
```

- Build the application
```
mvn clean install -DfileName=hysteryale
```

- Run the application
```
nohup java -Xmx12000m -Dspring.profiles.active=dev -jar hysteryale.war > log.txt 2>&1 & disown
```

### Access the Application

Open a web browser and navigate to http://localhost:8080 to access your Spring Boot application.
