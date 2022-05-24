package ds.streamingest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import ds.streamingest.model.PartitionKeyExtractorDescription;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
import ds.streamingest.service.LambdaCaller;
import ds.streamingest.service.StreamWriter;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@Log4j2
public class MappedIngestController {

    @Autowired
    private PartitionKeyExtractorDescRepo repository;

    @Autowired
    private StreamWriter streamWriter;

    @Autowired
    private LambdaCaller lambdaCaller;

    @PostMapping("/mappedIngest/{streamName}")
    public ResponseEntity<String> ingest(@PathVariable String streamName,
                                         @RequestHeader Map<String, String> headers,
                                         @RequestBody JsonNode jsonData) throws Exception {
        //logger.info("lookup repo context for stream {}", streamName);
        PartitionKeyExtractorDescription keyExtractorDesc = repository.retrieve(streamName);
        if(keyExtractorDesc == null) {
            return new ResponseEntity<>(String.format("no mapping found for stream %s",streamName), HttpStatus.BAD_REQUEST);
        }

        //logger.info("mapping read for {}",streamName);

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
        log.info(headers.toString());
        log.info("looking for header value for {}", key);

        String keyValue = headers.get(key);
        if(keyValue == null) {
            log.error("No header value for {} found in request headers",key);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        log.info("write to stream {} with partition key {} and payload {}",
                streamName, keyValue, jsonData.toString());

        //TODO - what's the best way to extract the bytes for the payload?
        var inputData = jsonData.toString();
        var xformed = lambdaCaller.invokeLambda(inputData);
        log.info("lambda returns {}", xformed);
        streamWriter.writeToStream(streamName, keyValue, xformed.getBytes(StandardCharsets.UTF_8));

        return new ResponseEntity<>("got it", HttpStatus.OK);
    }

    private ResponseEntity<String> processBodyExtraction(PartitionKeyExtractorDescription keyExtractorDesc,
                                                           String streamName,
                                                           Map<String, String> headers,
                                                           JsonNode jsonData) {

        //TODO - can we mask all the values in a json object to avoid logging sensitive data?
        //logger.info("extract value at {} from {}", keyExtractorDesc.getExtractionContext(), jsonData);

        JsonNode keyNode = jsonData.at(keyExtractorDesc.getExtractionContext());
        if(keyNode == null) {
            log.error("Unable to extract value from body to use as partition key");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //logger.info("extracted {}", keyNode.asText());

        streamWriter.writeToStream(streamName, keyNode.asText(), jsonData.toString().getBytes(StandardCharsets.UTF_8));

        return new ResponseEntity<>("got it", HttpStatus.OK);
    }
}
