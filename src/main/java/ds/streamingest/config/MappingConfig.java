package ds.streamingest.config;

import ds.streamingest.model.ApiMapping;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("mapping")
public class MappingConfig {
    private List<ApiMapping> streams = new ArrayList<>();

    public List<ApiMapping> getStreams() {
        return streams;
    }

    public void setStreams(List<ApiMapping> streams) {
        this.streams = streams;
    }

    @Override
    public String toString() {
        return "MappingConfig{" +
                "streams=" + streams +
                '}';
    }
}
