package ds.streamingest;

import java.util.Arrays;

import ds.streamingest.config.MappingConfig;
import ds.streamingest.controller.MappedIngestController;
import ds.streamingest.model.PartitionKeyExtractorDescription;
import ds.streamingest.model.PartitionKeyExtractorTypes;
import ds.streamingest.repository.PartitionKeyExtractorDescRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreamIngestApplication {

	private final static Logger logger = LoggerFactory.getLogger(StreamIngestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(StreamIngestApplication.class, args);
	}
	
	private void dumpBeans(ApplicationContext ctx) {
		System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
	}

	@Autowired
	private PartitionKeyExtractorDescRepo repository;

	@Autowired
	private MappingConfig mappingConfig;
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			//dumpBeans(ctx);
			logger.info(mappingConfig.toString());
			if(repository != null) {
				logger.info("Initializing in memory repository.");
				repository.store(new PartitionKeyExtractorDescription("s1", PartitionKeyExtractorTypes.HEADER, "s1key"));
				repository.store(new PartitionKeyExtractorDescription("s2", PartitionKeyExtractorTypes.BODY, "/foo/foopart1"));

			}
		};
	}

}
