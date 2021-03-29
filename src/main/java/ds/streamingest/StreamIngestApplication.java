package ds.streamingest;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreamIngestApplication {

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
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			//dumpBeans(ctx);
			System.out.println("initialize the repo here");
		};
	}

}
