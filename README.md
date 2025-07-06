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
docker compose -f docker-manual/compose-local.yaml up -d
```

- Stop all
```bash 
docker compose -f docker-manual/compose-local.yaml stop
```

- Stop and Remove all
```bash 
docker compose -f docker-manual/compose-local.yaml down
```

- Check what is running
```bash 
docker ps
```

- Rebuild filebeat

Remark: is using the directory under filebeat. there is a Dockerfile and yml file for configuration.
```bash 
 docker-compose -f docker-manual/compose-local.yaml build filebeat
```  

After installation you can access the kibana web gui and check the log. first you need a little configuration described below

Open elastic search/kibana:
with browser open url: http://localhost:5601/app/home#/

Initially go to discover -> create index pattern: filebeat* -> next -> add @timestamp -> create index pattern
Go back to discover: there you will see log statement from different services

## Kubernetes

[Kubernetes Documentation](k8s-manual/KubeCommands.md)

The approach having all kubernetes files of the other projects here should be reworked. the kubernetes files should go into the
appropriate projects, templating with helm and deployment into a kubernetes environment should be considered.

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

To run maven filtering for destination target/helm
```bash
mvn clean install -DskipTests 
```

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter kbe-brewery-gateway-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace kbe-brewery-gateway --create-namespace --wait --timeout 5m --debug --render-subchart-notes
```

show logs
```powershell
kubectl get pods -l app.kubernetes.io/name=$APPLICATION_NAME -n kbe-brewery-gateway
```
replace $POD with pods from the command above
```powershell
kubectl logs $POD -n kbe-brewery-gateway --all-containers
```

test
```powershell
helm test $APPLICATION_NAME --namespace kbe-brewery-gateway --logs
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME --namespace kbe-brewery-gateway
```

delete all
```powershell
kubectl delete all --all -n kbe-brewery-gateway
```


You can use the actuator rest call to verify via port 30090

