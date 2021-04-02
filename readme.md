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

Ingest style 2 - mapped ingest, partition key via http header

```
curl localhost:8080/mappedIngest/s1 -X POST -d '{"k1":{"o1":"xxx"}, "k2":"v2"}' -H 'Content-Type: application/json' -H 'S1KEY: v2'
```

Ingest style 2 - mapped ingest, partition key json path

curl localhost:8080/mappedIngest/s2 -X POST -d '{"foo":{"foopart1":1, "foopart2":2}, "bar":"barval", "baz":"bazval"}' -H 'Content-Type: application/json'

AWS Config

```
export AWS_PROFILE=xxxx
export AWS_REGION=xxxx
```

Create a Stream

```
aws kinesis create-stream --stream-name s1 --shard-count 1
aws kinesis create-stream --stream-name s2 --shard-count 1
```

Clean Up

```
aws kinesis delete-stream --stream-name s1
aws kinesis delete-stream --stream-name s2
```