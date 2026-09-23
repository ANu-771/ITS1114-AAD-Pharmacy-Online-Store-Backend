package lk.ijse.pharmacy_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PharmacyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmacyBackendApplication.class, args);
	}

}
