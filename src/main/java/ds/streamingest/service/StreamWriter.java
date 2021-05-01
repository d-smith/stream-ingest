package ds.streamingest.service;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordFailedException;
import com.amazonaws.services.kinesis.producer.UserRecordResult;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
public class StreamWriter {

    private static final Random RANDOM = new Random();
    private final static Logger logger = LoggerFactory.getLogger(StreamWriter.class);

    private KinesisProducer kinesisProducer;

    final ExecutorService callbackThreadPool = Executors.newCachedThreadPool();
    final long outstandingLimit = 5000;
    private long errorCount;

    public StreamWriter() {

        //TODO - load config from properties
        logger.info("initializing KPL");
        KinesisProducerConfiguration config = new KinesisProducerConfiguration()
                .setRecordMaxBufferedTime(3000)
                .setMaxConnections(10)
                .setRegion(System.getenv("AWS_REGION"))
                .setRequestTimeout(60000);

        kinesisProducer = new KinesisProducer(config);
    }

    public void writeToStream(String streamName, String partitionKey, byte[] data) {


        final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
            @Override public void onFailure(Throwable t) {
                //errorCount++;
                //if(errorCount % 10 == 0) {
                //    logger.warn("callback error count is {}", errorCount);
                //}

            };
            @Override public void onSuccess(UserRecordResult result) {
                //logger.info("callback indicates success - {}", result);
            };
        };

        //logger.info("write to the stream with bytes {}", data);

/*
        if (kinesisProducer.getOutstandingRecordsCount() < outstandingLimit) {

            ByteBuffer buffer = ByteBuffer.wrap(data);

            // doesn't block
            ListenableFuture<UserRecordResult> f = kinesisProducer.addUserRecord(streamName, partitionKey, buffer);
            Futures.addCallback(f, callback, callbackThreadPool);
        } else {
            logger.info("apply backpressure");
            try {
                Thread.sleep(1);
            } catch(Throwable t) {
                logger.error("interrupted exception thrown while attempting to apply backpressure");
            }
        }
*/

        ByteBuffer buffer = ByteBuffer.wrap(data);

        // doesn't block
        ListenableFuture<UserRecordResult> f = kinesisProducer.addUserRecord(streamName, partitionKey, buffer);
        Futures.addCallback(f, new FutureCallback<UserRecordResult>() {
            @Override
            public void onSuccess(UserRecordResult result) {
                long totalTime = result.getAttempts().stream()
                        .mapToLong(a -> a.getDelay() + a.getDuration())
                        .sum();
                // Only log with a small probability, otherwise it'll be very
                // spammy
                if (RANDOM.nextDouble() < 1e-5) {
                    logger.info(String.format(
                            "Succesfully put record, partitionKey=%s, "
                                    + "payload=%s, sequenceNumber=%s, "
                                    + "shardId=%s, took %d attempts, "
                                    + "totalling %s ms",
                            partitionKey, data, result.getSequenceNumber(),
                            result.getShardId(), result.getAttempts().size(),
                            totalTime));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof UserRecordFailedException) {
                    UserRecordFailedException e =
                            (UserRecordFailedException) t;
                    UserRecordResult result = e.getResult();

                    String errorList =
                            StringUtils.join(result.getAttempts().stream()
                                    .map(a -> String.format(
                                            "Delay after prev attempt: %d ms, "
                                                    + "Duration: %d ms, Code: %s, "
                                                    + "Message: %s",
                                            a.getDelay(), a.getDuration(),
                                            a.getErrorCode(),
                                            a.getErrorMessage()))
                                    .collect(Collectors.toList()), "\n");

                    logger.error(String.format(
                            "Record failed to put, partitionKey=%s, "
                                    + "payload=%s, attempts:\n%s",
                            partitionKey, data, errorList));
                }
            };
        }, callbackThreadPool);


    }
}
