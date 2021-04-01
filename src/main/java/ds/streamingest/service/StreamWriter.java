package ds.streamingest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class StreamWriter {
    private final static Logger logger = LoggerFactory.getLogger(StreamWriter.class);

    public void writeToStream(String streamName, String partitionKey, byte[] data) {
        logger.info("write to the damn stream already");
    }
}
