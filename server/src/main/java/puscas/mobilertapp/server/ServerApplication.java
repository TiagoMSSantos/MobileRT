package puscas.mobilertapp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class ServerApplication implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

	public static void main(String[] args) {
		logger.info("Starting server");
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		logger.info("Server running");
	}

	@PreDestroy
	public void onExit() {
		logger.info("###STOPPING###");
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			logger.error("Error when tried to sleep.", e);;
		}
		logger.info("###STOP FROM THE LIFECYCLE###");
	}
}
