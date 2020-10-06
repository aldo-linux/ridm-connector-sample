## Implement Jackson JsonNode columns using JPA

1. This [article](https://vladmihalcea.com/how-to-store-schema-less-eav-entity-attribute-value-data-using-json-and-hibernate/) explains how to achieve JsonNode as columns in any SQL database

## Run

1.  Run Kafka docker
```
docker-compose -f ./src/main/docker/kafka.yml up -d
```

2. Run the Spring Boot
```
./mvnw spring-boot:run
```
