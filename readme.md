# spring ingest

## Overview

This project illustrates a couple ways of providing a simple data ingest API 
that uses Kinesis as the underlying stream provider.

Two styles of interface are provided: a simple wrapper on top of kinesis, 
and a mapping API that uses the resource context, and some metadata to determine
which stream to write to, and how to extract the partition key value from the 
API call.

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
curl localhost:8080/mappedIngest/s1 -X POST -d '{"k1":{"o1":"xxx"}, "k2":"v2"}' -H 'Content-Type: application/json' -H 'S1KEY: v2'
```

Ingest style 2 - mapped ingest, partition key json path

curl localhost:8080/mappedIngest/s2 -X POST -d '{"foo":{"foopart1":1, "foopart2":2}, "bar":"barval", "baz":"bazval"}' -H 'Content-Type: application/json'

AWS Config

```
export AWS_PROFILE=xxxx
export AWS_REGION=xxxx
```

### Back Pressure

The [KPL project readme](https://github.com/awslabs/amazon-kinesis-producer) discusses backpressure. This implementation embeds a simple backpressure implementation
that hasn't been tuned or anything, but is a placeholder to illustrate how this works.

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

## Fire Hose

Use [this project](https://github.com/d-smith/kinesis-firehose-s3) to set up a firehose.

```
aws cloudformation create-stack --stack-name archiveFirehose \
--template-body file://streamback.yml \
--parameters ParameterKey=FirehoseName,ParameterValue=s1FH ParameterKey=KinesisStreamName,ParameterValue=s1 ParameterKey=ArchiveBucket,ParameterValue=97068-firehose-sink \
--capabilities CAPABILITY_NAMED_IAM
```

Notes - was able to confirm that [aggregated records are de-aggregated](https://docs.aws.amazon.com/streams/latest/dev/kpl-with-firehose.html) before being written to s3

## Kinesis Data Analytics

Java apps - will need to deal with aggregated records

Streaming SQL - will need to use a [preprocessing lambda](https://docs.aws.amazon.com/kinesisanalytics/latest/dev/lambda-preprocessing.html) to deaggregate records