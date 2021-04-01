package ds.streamingest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import ds.streamingest.model.PartitionKeyExtractorDescription;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
import ds.streamingest.service.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class MappedIngestController {
    private final static Logger logger = LoggerFactory.getLogger(MappedIngestController.class);

    @Autowired
    private PartitionKeyExtractorDescRepo repository;

    @Autowired
    private StreamWriter streamWriter;

    @PostMapping("/mappedIngest/{streamName}")
    public ResponseEntity<String> ingest(@PathVariable String streamName,
                                         @RequestHeader Map<String, String> headers,
                                         @RequestBody JsonNode jsonData) throws Exception {
        logger.info("lookup repo context for stream {}", streamName);
        PartitionKeyExtractorDescription keyExtractorDesc = repository.retrieve(streamName);
        if(keyExtractorDesc == null) {
            return new ResponseEntity<>(String.format("no mapping found for stream %s",streamName), HttpStatus.BAD_REQUEST);
        }

        logger.info("mapping read for {}",streamName);

        switch(keyExtractorDesc.getLocationType()) {
            case HEADER:
                return processHeaderExtraction(keyExtractorDesc, streamName, headers, jsonData);
            case BODY:
                return processBodyExtraction(keyExtractorDesc, streamName, headers, jsonData);
            default:
                return new ResponseEntity<>("Invalid mapping type in mapping configuration", HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> processHeaderExtraction(PartitionKeyExtractorDescription keyExtractorDesc,
                                                           String streamName,
                                                           Map<String, String> headers,
                                                           JsonNode jsonData) throws IOException {
        String key = keyExtractorDesc.getExtractionContext();
        logger.info(headers.toString());
        logger.info("looking for header value for {}", key);

        String keyValue = headers.get(key);
        if(keyValue == null) {
            logger.error("No header value for {} found in request headers",key);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        logger.info("write to stream {} with partition key {} and payload {}",
                streamName, key, jsonData.toString());

        streamWriter.writeToStream(streamName, key, jsonData.binaryValue());

        return new ResponseEntity<>("got it", HttpStatus.OK);
    }

    private ResponseEntity<String> processBodyExtraction(PartitionKeyExtractorDescription keyExtractorDesc,
                                                           String streamName,
                                                           Map<String, String> headers,
                                                           JsonNode jsonData) {
        return new ResponseEntity<>("not implemented", HttpStatus.NOT_IMPLEMENTED);
    }
}
