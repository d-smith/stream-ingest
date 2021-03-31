package ds.streamingest.controller;

import ds.streamingest.model.WriteStreamRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class WriteStreamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WriteStreamController writeStreamController;

    @Test
    public void shouldReturnGotIt() throws Exception {

        WriteStreamRequest request = new WriteStreamRequest("name", "key", "data");
        this.mockMvc.perform(post("/writeToStream", request)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"streamName\":\"larry\", \"key\":\"k\",\"data\":\"v\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));

    }
}
