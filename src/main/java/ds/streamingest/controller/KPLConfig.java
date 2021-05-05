package ds.streamingest.controller;

import ds.streamingest.service.KPLConfiguration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "kpl")
public class KPLConfig {

    @ReadOperation
    public Map<String,String> dumpKPLConfig() {
        ConcurrentHashMap<String,String> config = new ConcurrentHashMap<>();
        config.put("FailIfThrottled", ""  + KPLConfiguration.getFailIfThrottled());
        config.put("BackPressureBufferThreshold", ""  + KPLConfiguration.getBackPressureBufferThreshold());
        config.put("RateLimit", ""  + KPLConfiguration.getRateLimit());
        config.put("RecordMaxBufferedTime", ""  + KPLConfiguration.getRecordMaxBufferedTime());
        config.put("RecordTtl", ""  + KPLConfiguration.getRecordTtl());

        return config;
    }
}
