package ds.streamingest.controller;

import ds.streamingest.model.PartitionKeyExtractorDescription;
import ds.streamingest.model.PartitionKeyExtractorTypes;
import ds.streamingest.model.WriteStreamRequest;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
import ds.streamingest.service.StreamWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class MappedIngestControllerTest {

    @MockBean
   PartitionKeyExtractorDescRepo repository;

    @MockBean
    StreamWriter streamWriter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MappedIngestController mappedIngestController;

    @Test
    public void shouldMapKey() throws Exception {

        when(repository.retrieve("mappedStream1")).thenReturn(new PartitionKeyExtractorDescription("mappingStream1", PartitionKeyExtractorTypes.HEADER, "pkey"));

        this.mockMvc.perform(post("/mappedIngest/mappedStream1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("pkey","k2")
                .content("{\"k1\":{\"o1\":\"xxx\"}, \"k2\":\"v2\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));

    }

    //TODO - write tests for missing mapping spec, no header as per mapping

    @Test
    public void shouldMapStringAttribute() throws Exception {
        when(repository.retrieve("mappedStream2")).thenReturn(new PartitionKeyExtractorDescription("mappedStream2", PartitionKeyExtractorTypes.BODY, "/foo"));

        this.mockMvc.perform(post("/mappedIngest/mappedStream2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foo\":\"fooval\", \"bar\":\"barval\", \"baz\":\"bazval\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));
    }

    @Test
    public void shouldMapIntegerAttribute() throws Exception {
        when(repository.retrieve("mappedStream3")).thenReturn(new PartitionKeyExtractorDescription("mappedStream3", PartitionKeyExtractorTypes.BODY, "/foo"));

        this.mockMvc.perform(post("/mappedIngest/mappedStream3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foo\":123, \"bar\":\"barval\", \"baz\":\"bazval\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));
    }

    @Test
    public void shouldMapPath() throws Exception {
        when(repository.retrieve("mappedStream4")).thenReturn(new PartitionKeyExtractorDescription("mappedStream4", PartitionKeyExtractorTypes.BODY, "/foo/foopart1"));

        this.mockMvc.perform(post("/mappedIngest/mappedStream4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foo\":{\"foopart1\":1, \"foopart2\":2}, \"bar\":\"barval\", \"baz\":\"bazval\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("got it"));
    }

}
