# SFG Beer Works - Brewery Microservices

This project has a services of microservices for deployment via Docker Compose and Kubernetes.

You can access the API documentation [here](https://sfg-beer-works.github.io/brewery-api/#tag/Beer-Service)
Official Website: https://kubebyexample.com

## Overview

This project depends on several other projects and components which are all in the compose-local.yaml

projects:
Each projects is a microservice with its own repository. you can build those with mvn install. this will push
an image to your local docker image repos with the name: local/.....
for simplicity we use the images create by springframeworkguru, so the prefix is: springframeworkguru/

- inventory
- beer-service
- order-service
- inventory-failover
components:
- API Gateway
- Service Discovery (Eureka)
- Config Server
- Circuit Breaker (Resilience4j)
- Distributed Tracing (Zipkin)
- Message Broker (RabbitMQ)
- Database (MySQL)
- Caching (Redis)
- Monitoring (Prometheus & Grafana)

## Commands
- Start everything
```bash 
docker compose -f compose-local.yaml up -d
```

- Stop all
```bash 
docker compose -f compose-local.yaml stop
```

- Stop and Remove all
```bash 
docker compose -f compose-local.yaml down
```

- Check what is running
```bash 
docker ps
```

- Rebuild filebeat

Remark: is using the directory under filebeat. there is a Dockerfile and yml file for configuration.
```bash 
 docker-compose -f compose-local.yaml build filebeat
```  



After installation you can access the kibana web gui and check the log. first you need a little configuration described below

Open elastic search/kibana:
with browser open url: http://localhost:5601/app/home#/

Initially go to discover -> create index pattern: filebeet* -> next -> add @timestamp -> create index pattern
Go back to discover: there you will see log statement from different services

## Kubernetes

[Kubernetes Documentation](k8s/KubeCommands.md)

The approach having all kubernetes files of the other projects here should be reworked. the kubernetes files should go into the
appropriate projects, templating with helm and deployment into a kubernetes environment should be considered.

