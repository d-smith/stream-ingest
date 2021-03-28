package ds.streamingest.model;

public class PartitionKeyExtractorDescription {
    
    private String streamName;
    private PartitionKeyExtractorTypes locationType;
    private String extractionContext;
    
    public PartitionKeyExtractorDescription(String streamName, PartitionKeyExtractorTypes locationType, String extractionContext) {
        this.streamName = streamName;
        this.locationType = locationType;
        this.extractionContext = extractionContext;
    }
    
    public String getStreamName() {
        return streamName;
    }
    
    public PartitionKeyExtractorTypes getLocationType() {
        return locationType;
    }
    
    public String getExtractionContext() {
        return extractionContext;
    }
    
}