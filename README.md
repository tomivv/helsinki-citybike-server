# Helsinki city bike app

## Description
This project displays individual journeys taken with city bikes in Helsinki metropolitan area. Project is currently running frontend at [Vercel](https://helsinki-citybike.vercel.app/) and backend in DigitalOcean. Client is a [T3 Stack](https://create.t3.gg/) bootstrapped with `create-t3-app`.

## Prerequisites
* Java jdk 17

## How to get started
This application consists of client and backend. Client code and instructions are located at [here!](https://github.com/tomivv/helsinki-citybike)

1. Clone repository
```bash
git clone https://github.com/tomivv/helsinki-citybike-server.git
```
2. Install dependencies

```bash
cd helsinki-citybike-server
./mvnw dependency:resolve
```

3. Edit env files
```
rename src/main/resources/application.properties.example to application.properties and fill the missing values
```
4. Running app

```bash
./mvnw spring-boot:run
```

6. Running docker container

To build docker container you first need to do steps 1 and 3
  
Build docker image locally
```bash
docker build -t helsinki-citybike-server .
```
And then run the container
```bash
docker run -d --name helsinki-citybike-server helsinki-citybike-server
```

## Technologies used

* Docker
* PostgreSQL
* Typescript
* Next.js + React
* Java (backend)
