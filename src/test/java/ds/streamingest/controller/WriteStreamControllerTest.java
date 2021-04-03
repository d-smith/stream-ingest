package ds.streamingest.controller;

import ds.streamingest.model.WriteStreamRequest;
import ds.streamingest.service.StreamWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    StreamWriter streamWriter;

    @Test
    public void shouldReturnGotIt() throws Exception {

        this.mockMvc.perform(post("/writeToStream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"streamName\":\"larry\", \"key\":\"k\",\"data\":\"v\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));

    }

    @Test
    public void missingStreamNameShouldReturnError() throws Exception {

        this.mockMvc.perform(post("/writeToStream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"streamName\":\"\", \"key\":\"k\",\"data\":\"v\"}"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("stream name not specified"));

    }

    @Test
    public void missingPartitionKeyShouldReturnError() throws Exception {

        this.mockMvc.perform(post("/writeToStream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"streamName\":\"larry\", \"key\":\"\",\"data\":\"v\"}"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("partition key not specified"));

    }
}
