# spring ingest

```
mvn clean spring-boot:run
```

Actuator endpoint:

```
curl localhost:8080/actuator/health
curl localhost:8080/actuator/info
```


Ingest style 1 - api consumer understands downstream mapping

```
curl localhost:8080/writeToStream -X POST -d '{"streamName":"larry", "key":"k","data":"v"}' -H 'Content-Type: application/json'
```