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

Ingest style 2 - mapped ingest

curl localhost:8080/mappedIngest/s1 -X POST -d '{"k1":{"o1":"xxx"}, "k2":"v2"}' -H 'Content-Type: application/json' -H 'S1KEY: v2'

AWS Config

```
export AWS_PROFILE=xxxx
export AWS_REGION=xxxx
```

Create a Stream

```
aws kinesis create-stream --stream-name s1 --shard-count 1
```

Clean Up

```
aws kinesis delete-stream --stream-name s1
```