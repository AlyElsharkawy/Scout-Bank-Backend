package org.sportingscout.scout_bank_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScoutBankBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScoutBankBackendApplication.class, args);
  }

}
