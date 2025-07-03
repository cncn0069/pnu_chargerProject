package charger.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChargerProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargerProjectApplication.class, args);
	}

}
