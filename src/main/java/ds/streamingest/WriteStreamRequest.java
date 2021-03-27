package ds.streamingest;

public class WriteStreamRequest {
    private final String data;
    private final String streamName;
    private final String key;


    public WriteStreamRequest(String streamName, String key, String data) {
        this.streamName = streamName;
        this.key = key;
        this.data = data;
    }
    
    public String getStreamName() {
        return streamName;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getData() {
        return data;
    }
}