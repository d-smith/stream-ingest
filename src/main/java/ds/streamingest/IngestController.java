package ds.streamingest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class IngestController {
    @RequestMapping("/ingest")
    public String ingest() {
        return "yo";
    }
}