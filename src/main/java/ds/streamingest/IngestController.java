package ds.streamingest;

import ds.streamingest.model.WriteStreamRequest;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
public class IngestController {
    private final static Logger logger = LoggerFactory.getLogger(IngestController.class);

    @Autowired
    private PartitionKeyExtractorDescRepo repository;
    
    @PostMapping("/writeToStream")
    public String ingest(@RequestBody WriteStreamRequest writeStreamRequest) {
        logger.info("key is " + writeStreamRequest.getKey());
        return "got it";
    }
}