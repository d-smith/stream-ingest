package ds.streamingest.controller;

import ds.streamingest.model.WriteStreamRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
public class WriteStreamController {
    private final static Logger logger = LoggerFactory.getLogger(WriteStreamController.class);


    @PostMapping("/writeToStream")
    public String ingest(@RequestBody WriteStreamRequest writeStreamRequest) {
        logger.info("key is " + writeStreamRequest.getKey());
        return "got it";
    }
}