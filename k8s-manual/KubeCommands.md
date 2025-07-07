# Kubernetes

https://kubebyexample.com

## Table of Contents
- [Commands](#commands)
    - [Display resources](#display-resources)
    - [View logs](#View logs)
    - [Delete all services and deployments](#delete-all-services-and-deployments)
- [Create Applications](#Create Applications)
  - [Mysql](#Mysql)
  - [JMS/ActiveMQ](#JMS/ActiveMQ)
  - [Inventory Service](#Inventory Service)
  - [Inventory Failover Service](#Inventory Failover Service)
  - [Beer Service](#Beer Service)
  - [Order Service](#Order Service)
  - [gateway](#gateway)
- [Expose port](#Expose port)
- [Consolidated Logging](#Consolidated Logging)
  - [elasticsearch](#elasticsearch)
  - [kibana](#kibana)
  - [filebeat](#filebeat)

## Overview

For the images we are not using our own image. We are using the original springframeworkguru images instead
of the local images.

You can access the API documentation [here](https://sfg-beer-works.github.io/brewery-api/#tag/Beer-Service)


![alt text](../guru.png "Overview")

## Commands

### Display resources

Display all K8s resources in the default namespace
```bash
kubectl get all
```

Display all K8s resources in the default namespace with more details
```bash 
kubectl get all -o wide
```

### View logs 

Use parameter -f to follow the logs

```bash 
kubectl logs jms-<pod-id>
```
```bash 
kubectl logs mysql-<pod-id>
```
```bash 
kubectl logs beer-service-<pod-id>
```
```bash 
kubectl logs inventory-failover-<pod-id>
```
```bash 
kubectl logs inventory-service-<pod-id>
```
```bash 
kubectl logs order-service-<pod-id>
```
```bash 
kubectl logs gateway-<pod-id>
```
```bash 
kubectl logs elasticsearch-<pod-id>
```
```bash 
kubectl logs kibana-<pod-id>
```
```bash 
kubectl logs filebeat-<pod-id>
```

### Delete all services and deployments
```bash
kubectl delete service mysql
kubectl delete deployment mysql

kubectl delete service activemq-artemis
kubectl delete deployment activemq-artemis

kubectl delete service beer-service
kubectl delete deployment beer-service

kubectl delete service inventory-failover
kubectl delete deployment inventory-failover

kubectl delete service inventory-service
kubectl delete deployment inventory-service

kubectl delete service order-service
kubectl delete deployment order-service

kubectl delete service gateway
kubectl delete deployment gateway

kubectl delete service elasticsearch
kubectl delete deployment elasticsearch

kubectl delete service kibana
kubectl delete deployment kibana

kubectl delete service -f filebeat-kubernetes.yaml
```
If you have a complex installation use the yaml file for deletion. for example:
```bash
kubectl delete -f gateway-deployment.yaml
```

## Create Applications

### Mysql

Create Deployment
```bash
kubectl create deployment mysql --image=mysql:5.7 --dry-run=client -o yaml > mysql-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f mysql-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip mysql --tcp=3306:3306 --dry-run=client -o yaml > mysql-service.yaml
```

Apply Service
```bash
kubectl apply -f mysql-service.yaml
```

### JMS/ActiveMQ

Create Deployment
```bash
kubectl create deployment activemq-artemis --image=vromero/activemq-artemis --dry-run=client -o yaml > jms-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f jms-deployment.yaml
```

Create Service for
```bash
kubectl create service clusterip activemq-artemis --tcp=8161:8161 --tcp=61616:61616 --dry-run=client -o yaml > jms-service.yaml
```

Apply Service for
```bash
kubectl apply -f jms-service.yaml
```

### Inventory Service

Create Deployment 
```bash
kubectl create deployment inventory-service --image=springframeworkguru/kbe-brewery-inventory-service --dry-run=client -o yaml > inventory-service-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f inventory-service-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip inventory-service --tcp=8082:8082 --dry-run=client -o yaml > inventory-service-service.yaml
```

Apply Service
```bash
kubectl apply -f inventory-service-service.yaml
```

### Inventory Failover Service

We just copied/pasted the filebeat yaml file from the original springframeworkguru site

Create Deployment
```bash
kubectl create deployment inventory-failover --image=springframeworkguru/kbe-brewery-inventory-failover --dry-run=client -o yaml > inventory-failover-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f inventory-failover-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip inventory-failover --tcp=8083:8083 --dry-run=client -o yaml > inventory-failover-service.yaml
```

Apply Service
```bash
kubectl apply -f inventory-failover-service.yaml
```

### Beer Service

Create Deployment
```bash
kubectl create deployment beer-service --image=springframeworkguru/kbe-brewery-beer-service --dry-run=client -o yaml > beer-service-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f beer-service-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip beer-service --tcp=8080:8080 --dry-run=client -o yaml > beer-service-service.yaml
```

Apply Service
```bash
kubectl apply -f beer-service-service.yaml
```

### Order Service

Create Deployment
```bash
kubectl create deployment order-service --image=springframeworkguru/kbe-brewery-order-service --dry-run=client -o yaml > order-service-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f order-service-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip order-service --tcp=8081:8081 --dry-run=client -o yaml > order-service-service.yaml
```

Apply Service
```bash
kubectl apply -f order-service-service.yaml
```

### gateway

Create Deployment
```bash
kubectl create deployment gateway --image=springframeworkguru/kbe-brewery-gateway --dry-run=client -o yaml > gateway-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f gateway-deployment.yaml
```

Create Service (using nodeport)
```bash
kubectl create service nodeport gateway --tcp=9090:9090 --dry-run=client -o yaml > gateway-service.yaml
```

Apply Service
```bash
kubectl apply -f gateway-service.yaml
```

## Expose port 

Below are some alternative from the nodeport approach. The Gateway with our installation acts as a ingress server on a random port, 
you can find out with the ```bash kubectl get all ``` command.
```bash
NAME                         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)              AGE
service/activemq-artemis     ClusterIP   10.98.35.99      <none>        8161/TCP,61616/TCP   14h
service/beer-service         ClusterIP   10.96.118.77     <none>        8080/TCP             13h
service/gateway              NodePort    10.98.23.105     <none>        9090:31975/TCP       7s
service/inventory-failover   ClusterIP   10.109.235.24    <none>        8083/TCP             14h
service/inventory-service    ClusterIP   10.103.214.107   <none>        8082/TCP             14h
service/kubernetes           ClusterIP   10.96.0.1        <none>        443/TCP              15d
service/mysql                ClusterIP   10.110.132.110   <none>        3306/TCP             14h
service/order-service        ClusterIP   10.111.189.101   <none>        8081/TCP             13h
```

This is achieved with the nodeport type when we create the service (see above).

### Port forward to Gateway
This is exposing the gateway to the outside world on the port 8080
TODO: This is just a workaround
```bash
kubectl port-forward service/gateway 8080:8080
```

Instead of starting port-forwarding we can do the same with ingress

### Ingress

Install Ingress Controller
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
```

And wait for installation
```bash
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s
```

The Ingress Controller listen on port 80 and 443 by default
Check:
```bash
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx 
```

Now we change the port 80 to 8080
```bash
kubectl edit svc ingress-nginx-controller -n ingress-nginx
```
Now we can create a new file 'ingress-nginx-controller.yaml' and paste the content there
In the file we change the port 80 to 8080 and save the file.

Deploy changed Ingress Controller
```bash
kubectl apply -f ingress-nginx-controller.yaml
```

Check again if ingress controller is listening on port 8080:
```bash
kubectl get svc -n ingress-nginx
```

Finally we create an ingress for our gateway
```bash
kubectl create ingress gateway-ingress --rule="/*=gateway:8080" --class=nginx --annotation="kubernetes.io/ingress.class=nginx" --dry-run=client -o yaml > gateway-ingress.yaml
```  

Apply Service for gateway
```bash
kubectl apply -f gateway-ingress.yaml
``` 

Check ingress
```bash
kubectl get ingress
``` 

## Consolidated Logging

### elasticsearch

will hold the log data

Create Deployment
```bash
kubectl create deployment elasticsearch --image=docker.elastic.co/elasticsearch/elasticsearch:7.12.1 --dry-run=client -o yaml > elasticsearch-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f elasticsearch-deployment.yaml
```

Create Service
```bash
kubectl create service clusterip elasticsearch --tcp=9200:9200 --dry-run=client -o yaml > elasticsearch-service.yaml
```

Apply Service
```bash
kubectl apply -f elasticsearch-service.yaml
```

### kibana

will enable search in the log database on elastic search. we expose kibana to a random port with nodeport, so we can access the webgui
e.g. http://localhost:31312/app/home#/

Create Deployment
```bash
kubectl create deployment kibana --image=docker.elastic.co/kibana/kibana:7.12.1 --dry-run=client -o yaml > kibana-deployment.yaml
```

Apply Deployment
```bash
kubectl apply -f kibana-deployment.yaml
```

Create Service (using nodeport)
```bash
kubectl create service nodeport kibana --tcp=5601:5601 --dry-run=client -o yaml > kibana-service.yaml
```

Apply Service
```bash
kubectl apply -f kibana-service.yaml
```

### filebeat

retrieves the logs from all services.

Apply 
```bash
kubectl apply -f filebeat-kubernetes.yaml
```
Output
```bash
configmap/filebeat-config created
daemonset.apps/filebeat created
clusterrolebinding.rbac.authorization.k8s.io/filebeat created
clusterrole.rbac.authorization.k8s.io/filebeat created
serviceaccount/filebeat created
```
After installation you can open the kibana console via the web browser and access the logs:
Open elastic search/kibana (get the randport from kibana with the ```kubectl get service/kibana``` :
with browser open url: http://localhost:31312/app/home#/

Initially go to discover -> create index pattern: filebeat* -> next -> add @timestamp -> create index pattern

![Create Index Pattern](images/create%20index%20pattern.png)

Go back to discover:

