# spring ingest

## Overview

This project illustrates a couple ways of providing a simple data ingest API 
that uses Kinesis as the underlying stream provider.

Two styles of interface are provided: a simple wrapper on top of kinesis, 
and a mapping API that uses the resource context, and some metadata to determine
which stream to write to, and how to extract the partition key value from the 
API call.

This uses KPL under the hood. Properties for KPL config documented [here](https://github.com/awslabs/amazon-kinesis-producer/blob/master/java/amazon-kinesis-producer-sample/default_config.properties)

To build and run:

```
mvn clean spring-boot:run
```

Unit tests:

```
mvn clean test
```

Actuator endpoint:

```
curl localhost:8080/actuator/health
curl localhost:8080/actuator/info
```


Ingest style 1 - wrapper style

```
curl localhost:8080/writeToStream -X POST -d '{"streamName":"s1", "key":"k","data":"v"}' -H 'Content-Type: application/json'
```

Ingest style 2 - mapped ingest, partition key via http header

```
curl localhost:8080/mappedIngest/s1 -X POST -d @ce.json -H 'Content-Type: application/json' -H 'S1KEY: v2'
```

Ingest style 2 - mapped ingest, partition key json path

curl localhost:8080/mappedIngest/s2 -X POST -d '{"foo":{"foopart1":1, "foopart2":2}, "bar":"barval", "baz":"bazval"}' -H 'Content-Type: application/json'

AWS Config

```
export AWS_PROFILE=xxxx
export AWS_REGION=xxxx
```

### Back Pressure

The [KPL project readme](https://github.com/awslabs/amazon-kinesis-producer) discusses backpressure. 
This implementation embeds a simple backpressure implementation
that hasn't been tuned or anything, but is a placeholder to illustrate how this works.

You can experiment with the value of outstandingLimit in StreamWriter.java to see back pressure in action, 
or just hammer the endpoint with enough load...

## Misc AWS Concerns

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

Minimal KPL Producer IAM Policy

```console
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "kinesis:PutRecord",
                "kinesis:SubscribeToShard",
                "cloudwatch:PutMetricData",
                "kinesis:DescribeStreamSummary",
                "kinesis:ListShards",
                "kinesis:PutRecords",
                "kinesis:DescribeStreamConsumer",
                "kinesis:DescribeStream",
                "kinesis:RegisterStreamConsumer"
            ],
            "Resource": "*"
        }
    ]
}
```
## CloudWatch

```
aws cloudwatch list-metrics --namespace "AWS/Kinesis" --dimensions Name=StreamName,Value=s1


aws cloudwatch get-metric-statistics --namespace "AWS/Kinesis" \
--metric-name "PutRecords.Bytes" --dimensions Name=StreamName,Value=s1 \
--start-time "`date -v -10M`" --end-time "`date`" --period 60 --statistics Sum

```

## Firehose

Use [this project](https://github.com/d-smith/kinesis-firehose-s3) to set up a firehose.

```
aws cloudformation create-stack --stack-name archiveFirehose \
--template-body file://streamback.yml \
--parameters ParameterKey=FirehoseName,ParameterValue=s1FH ParameterKey=KinesisStreamName,ParameterValue=s1 ParameterKey=ArchiveBucket,ParameterValue=97068-firehose-sink \
--capabilities CAPABILITY_NAMED_IAM
```

Notes - was able to confirm that [aggregated records are de-aggregated](https://docs.aws.amazon.com/streams/latest/dev/kpl-with-firehose.html) before being written to s3

To process events when they hit the bucket, add the xform lambda in the above project, then install the ce2ddb project

## Kinesis Data Analytics

Java apps - will need to deal with aggregated records

Streaming SQL - will need to use a [preprocessing lambda](https://docs.aws.amazon.com/kinesisanalytics/latest/dev/lambda-preprocessing.html) to deaggregate records

## Notes on Metrics

KPI programming interface allows access to metrics it collects, in addition to sending them to cloud watch. For
details see [here](https://github.com/awslabs/amazon-kinesis-producer/blob/master/metrics.md).

Example `getMetrics` dump:

```
07:43:13.489 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter - available metrics
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=RetriesPerRecord, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=0.0, mean=0.0, sampleCount=100.0, min=0.0, max=0.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=RetriesPerRecord, duration=26, dimensions={StreamName=s2}, sum=0.0, mean=0.0, sampleCount=100.0, min=0.0, max=0.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=RetriesPerRecord, duration=26, dimensions={}, sum=0.0, mean=0.0, sampleCount=100.0, min=0.0, max=0.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=AllErrors, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=0.0, mean=0.0, sampleCount=14.0, min=0.0, max=0.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=BufferingTime, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=7233.0, mean=72.33, sampleCount=100.0, min=7.0, max=125.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=BufferingTime, duration=26, dimensions={}, sum=7233.0, mean=72.33, sampleCount=100.0, min=7.0, max=125.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPerKinesisRecord, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=70.0, mean=5.0, sampleCount=14.0, min=4.0, max=8.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=BufferingTime, duration=26, dimensions={StreamName=s2}, sum=7233.0, mean=72.33, sampleCount=100.0, min=7.0, max=125.0]
07:43:13.508 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsDataPut, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=161200.0, mean=1612.0, sampleCount=100.0, min=1612.0, max=1612.0]
07:43:13.510 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsDataPut, duration=26, dimensions={}, sum=161200.0, mean=1612.0, sampleCount=100.0, min=1612.0, max=1612.0]
07:43:13.510 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsDataPut, duration=26, dimensions={StreamName=s2}, sum=161200.0, mean=1612.0, sampleCount=100.0, min=1612.0, max=1612.0]
07:43:13.510 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPut, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=100.0, mean=1.0, sampleCount=100.0, min=1.0, max=1.0]
07:43:13.510 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPerPutRecordsRequest, duration=26, dimensions={}, sum=100.0, mean=5.0, sampleCount=20.0, min=3.0, max=8.0]
07:43:13.511 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=AllErrors, duration=26, dimensions={StreamName=s2}, sum=0.0, mean=0.0, sampleCount=44.0, min=0.0, max=0.0]
07:43:13.511 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPut, duration=26, dimensions={StreamName=s2}, sum=100.0, mean=1.0, sampleCount=100.0, min=1.0, max=1.0]
07:43:13.511 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsDataPut, duration=26, dimensions={StreamName=s2}, sum=162082.0, mean=3683.681818181818, sampleCount=44.0, min=1612.0, max=12983.0]
07:43:13.511 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsDataPut, duration=26, dimensions={}, sum=162082.0, mean=3683.681818181818, sampleCount=44.0, min=1612.0, max=12983.0]
07:43:13.511 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsReceived, duration=27, dimensions={}, sum=101.0, mean=1.0, sampleCount=101.0, min=1.0, max=1.0]
07:43:13.513 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=RequestTime, duration=26, dimensions={StreamName=s2}, sum=7260.0, mean=363.0, sampleCount=20.0, min=193.0, max=770.0]
07:43:13.513 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPut, duration=26, dimensions={}, sum=100.0, mean=1.0, sampleCount=100.0, min=1.0, max=1.0]
07:43:13.513 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsPut, duration=26, dimensions={StreamName=s2}, sum=44.0, mean=1.0, sampleCount=44.0, min=1.0, max=1.0]
07:43:13.513 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsDataPut, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=113722.0, mean=8123.0, sampleCount=14.0, min=6503.0, max=12983.0]
07:43:13.513 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsPut, duration=26, dimensions={}, sum=44.0, mean=1.0, sampleCount=44.0, min=1.0, max=1.0]
07:43:13.514 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPerPutRecordsRequest, duration=26, dimensions={StreamName=s2}, sum=100.0, mean=5.0, sampleCount=20.0, min=3.0, max=8.0]
07:43:13.514 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=RequestTime, duration=26, dimensions={}, sum=7260.0, mean=363.0, sampleCount=20.0, min=193.0, max=770.0]
07:43:13.514 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsPerPutRecordsRequest, duration=26, dimensions={}, sum=44.0, mean=2.2, sampleCount=20.0, min=1.0, max=7.0]
07:43:13.514 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=AllErrors, duration=26, dimensions={}, sum=0.0, mean=0.0, sampleCount=44.0, min=0.0, max=0.0]
07:43:13.514 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsPut, duration=26, dimensions={StreamName=s2, ShardId=shardId-000000000000}, sum=14.0, mean=1.0, sampleCount=14.0, min=1.0, max=1.0]
07:43:13.515 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPerKinesisRecord, duration=26, dimensions={StreamName=s2}, sum=100.0, mean=2.272727272727273, sampleCount=44.0, min=1.0, max=8.0]
07:43:13.515 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPending, duration=27, dimensions={StreamName=s2}, sum=235.0, mean=1.7407407407407407, sampleCount=135.0, min=0.0, max=43.0]
07:43:13.515 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPending, duration=27, dimensions={}, sum=235.0, mean=1.7407407407407407, sampleCount=135.0, min=0.0, max=43.0]
07:43:13.515 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsReceived, duration=27, dimensions={StreamName=s2}, sum=101.0, mean=1.0, sampleCount=101.0, min=1.0, max=1.0]
07:43:13.516 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=UserRecordsPerKinesisRecord, duration=26, dimensions={}, sum=100.0, mean=2.272727272727273, sampleCount=44.0, min=1.0, max=8.0]
07:43:13.516 [http-nio-8080-exec-8] INFO  ds.streamingest.service.StreamWriter -   Metric [name=KinesisRecordsPerPutRecordsRequest, duration=26, dimensions={StreamName=s2}, sum=44.0, mean=2.2, sampleCount=20.0, min=1.0, max=7.0]
```

