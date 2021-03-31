package ds.streamingest.controller;

import ds.streamingest.model.PartitionKeyExtractorDescription;
import ds.streamingest.model.PartitionKeyExtractorTypes;
import ds.streamingest.model.WriteStreamRequest;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
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


}
