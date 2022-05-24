package ds.streamingest.controller;

import ds.streamingest.model.WriteStreamRequest;
import ds.streamingest.service.LambdaCaller;
import ds.streamingest.service.StreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


@RestController
public class WriteStreamController {
    private final static Logger logger = LoggerFactory.getLogger(WriteStreamController.class);

    @Autowired
    private StreamWriter streamWriter;

    @Autowired
    private LambdaCaller lambdaCaller;

    @PostMapping("/writeToStream")
    public ResponseEntity<String> ingest(@RequestBody WriteStreamRequest writeStreamRequest) {
        logger.info("key is " + writeStreamRequest.getKey());

        String streamName = writeStreamRequest.getStreamName();
        if(streamName == null || streamName.equals("")) {
            return new ResponseEntity<>("stream name not specified", HttpStatus.BAD_REQUEST);
        }

        String partitionKey = writeStreamRequest.getKey();
        if(partitionKey == null || partitionKey.equals("")) {
            return new ResponseEntity<>("partition key not specified", HttpStatus.BAD_REQUEST);
        }

        var inputData = writeStreamRequest.getData();
        var xformed = lambdaCaller.invokeLambda(inputData);
        streamWriter.writeToStream(streamName, partitionKey, xformed.getBytes(StandardCharsets.UTF_8));

        return new ResponseEntity<>("got it", HttpStatus.OK);
    }
}