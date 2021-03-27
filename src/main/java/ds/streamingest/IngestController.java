package ds.streamingest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class IngestController {
    @PostMapping("/writeToStream")
    public String ingest(@RequestBody WriteStreamRequest writeStreamRequest) {
        System.out.println(writeStreamRequest);
        System.out.println("key is " + writeStreamRequest.getKey());
        return "got it";
    }
}