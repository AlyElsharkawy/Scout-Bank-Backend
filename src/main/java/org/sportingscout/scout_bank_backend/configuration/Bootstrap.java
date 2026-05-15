package org.sportingscout.scout_bank_backend.configuration;

import org.apache.catalina.User;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.sportingscout.scout_bank_backend.repositories.OrganizationsRepository;
import org.sportingscout.scout_bank_backend.security.Permissions;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;
import java.util.Optional;
import java.util.Locale;

import net.datafaker.Faker;

@Component
public class Bootstrap implements CommandLineRunner {
  private final UsersRepository usersRepo;
  private final OrganizationsRepository organizationsRepo;
  private final PasswordEncoder passwordEncoder;
  private static final Faker egyptianFaker = new Faker(Locale.of("ar", "EG"));
  private static final Faker faker = new Faker();

  @Value("${bootstrap.root-user:false}")
  private boolean createRoot;

  @Value("${bootstrap.organizations:false}")
  private boolean createOrganizations;

  @Value("${bootstrap.root-password}")
  private String tempPassword;

  public Bootstrap(UsersRepository usersRepo, OrganizationsRepository organizationsRepo,
      PasswordEncoder passwordEncoder) {
    this.usersRepo = usersRepo;
    this.organizationsRepo = organizationsRepo;
    this.passwordEncoder = passwordEncoder;

    if (tempPassword == null) {
      tempPassword = egyptianFaker.internet().password(8, 8, true, false, true);
      System.out.println("Temp password is empty. Using this password instead: " + tempPassword);
    }
  }

  @Override
  public void run(String... args) throws Exception {
    if (createOrganizations) {
      Organization sportingScout = new Organization("Sporting Scouts",
          "The Sporting Scouts are an Alexandria-based scouting organization headquartered at Sporting Club, Alexandria");
      organizationsRepo.save(sportingScout);
    }

    if (createRoot) {
      Optional<Organization> temp = organizationsRepo.findByName("Sporting Scouts");
      Organization rootOrganization = temp.isPresent() ? temp.get() : null;
      ApplicationUser rootUser = ApplicationUser.builder()
          .name("Root User")
          .email("root@example.com")
          .phoneNumber("01245678910")
          .authorities(Set.of(
              Permissions.ARTICLE_VIEW, Permissions.ARTICLE_WRITE, Permissions.ARTICLE_EDIT,
              Permissions.ARTICLE_DELETE, Permissions.ARTICLE_REVIEW,
              Permissions.ORGANIZATION_VIEW, Permissions.ORGANIZATION_CREATE, Permissions.ORGANIZATION_EDIT,
              Permissions.MEDIA_UPLOAD, Permissions.MEDIA_DELETE,
              Permissions.USER_VIEW, Permissions.USER_ROLE_EDIT, Permissions.USER_DELETE))
          .organization(rootOrganization)
          .password(this.passwordEncoder.encode(tempPassword))
          .build();
      usersRepo.save(rootUser);
      for (int i = 0; i < 5; i++) {
        tempPassword = egyptianFaker.internet().password(8, 8, true, false, true);
        ApplicationUser tempUser = ApplicationUser.builder()
            .name(faker.name().name())
            .email(faker.internet().emailAddress())
            .phoneNumber(egyptianFaker.phoneNumber().cellPhone().replace(" ", ""))
            .organization(rootOrganization)
            .authorities(Set.of(
                Permissions.ARTICLE_VIEW, Permissions.ORGANIZATION_VIEW))
            .password(passwordEncoder.encode(tempPassword))
            .build();
        ApplicationUser newUser = usersRepo.save(tempUser);
        System.out.println("Created temp user with ID " + newUser.getId() + " and password " + tempPassword);
      }
    }
  }
}
