# Java chat server

This project is a java server for a chat application.

## Prerequisites
You need to have the following installed:
1. Postgres
2. Java runtime environment
3. Maven
4. Jenkins
5. Docker

## Before starting

### Environment variables
To use the application you need to create a Postgres database following the SQL provided in `db/init.sql`.
Then you need to create a `.env` file following the `.env-sample` model.

### Install dependencies
`mvn clean install -DskipTests`

## Starting the java server

`java -cp target/chatserver-1.0-SNAPSHOT-jar-with-dependencies.jar chatapp.JavaServer`

## Testing
`mvn test`
A jacoco coverage report is generated in `target/site/jacoco/index.html`

## Jenkins
run `systemctl status jenkins.service` to see if jenkins server is running</br>
It should be running on `localhost:8080` by default.

## Grafana
1. Create docker network (only once)</br>
`docker network create chat-app-monitoring`
2. Run Prometheus in a container to collect and store metrics</br>
```bash
docker run \
  --rm \
  --name prometheus \
  --network chat-app-monitoring \
  --add-host=host.docker.internal:host-gateway \
  -p 9090:9090 \
  -v ./prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```
3. Run Grafana in a container to visualize metrics</br>
```bash
docker run \
  --rm \
  --name grafana \
  --network chat-app-monitoring \
  -p 3001:3000 \
  grafana/grafana
```
4. Prometheus: <a>http://localhost:9090/targets</a></br>
<a>http://localhost:9090/query</a>
5. Grafana: <a>http://localhost:3001</a>
6. Default Grafana login: `admin` `admin`


