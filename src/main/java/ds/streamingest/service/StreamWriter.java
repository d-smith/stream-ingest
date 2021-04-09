package ds.streamingest.service;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordResult;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class StreamWriter {
    private final static Logger logger = LoggerFactory.getLogger(StreamWriter.class);

    private KinesisProducer kinesisProducer;

    final ExecutorService callbackThreadPool = Executors.newCachedThreadPool();
    final long outstandingLimit = 300;

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
                logger.warn("callback indicates failure - {}", t);
            };
            @Override public void onSuccess(UserRecordResult result) {
                logger.info("callback indicates success - {}", result);
            };
        };

        //logger.info("write to the stream with bytes {}", data);

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
    }
}
