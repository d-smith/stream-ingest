package ds.streamingest;

import ds.streamingest.controller.WriteStreamController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
class StreamIngestApplicationTests {


	@Autowired
	private WriteStreamController writeStreamController;

	@Test
	void contextLoads() {
		assertThat(writeStreamController).isNotNull();
	}



}
