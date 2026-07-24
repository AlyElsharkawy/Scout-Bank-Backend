package org.sportingscout.scout_bank_backend.configuration;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.entities.UserRank;
import org.sportingscout.scout_bank_backend.entities.UserRole;
import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.sportingscout.scout_bank_backend.entities.ArticleType;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.sportingscout.scout_bank_backend.repositories.OrganizationsRepository;
import org.sportingscout.scout_bank_backend.repositories.UserRanksRepository;
import org.sportingscout.scout_bank_backend.repositories.UserRolesRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTagsRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTypeRepository;
import org.sportingscout.scout_bank_backend.security.Permissions;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.time.LocalDate;

import net.datafaker.Faker;

@Component
public class Bootstrap implements CommandLineRunner {
  private final UsersRepository usersRepo;
  private final OrganizationsRepository organizationsRepo;
  private final UserRanksRepository userRanksRepo;
  private final UserRolesRepository userRolesRepo;
  private final ArticleTagsRepository articleTagsRepo;
  private final ArticleTypeRepository articleTypeRepo;
  private final PasswordEncoder passwordEncoder;
  private static final Faker egyptianFaker = new Faker(Locale.of("ar", "EG"));
  private static final Faker faker = new Faker();

  @Value("${bootstrap.root-user:false}")
  private boolean createRoot;

  @Value("${bootstrap.users:false}")
  private boolean createUsers;

  @Value("${bootstrap.organizations:false}")
  private boolean createOrganizations;

  @Value("${bootstrap.ranks:false}")
  private boolean createRanks;

  @Value("${bootstrap.roles:false}")
  private boolean createRoles;

  @Value("${bootstrap.tags:false}")
  private boolean createTags;

  @Value("${bootstrap.types:false}")
  private boolean createTypes;

  @Value("${bootstrap.root-password}")
  private String tempPassword;

  public Bootstrap(UsersRepository usersRepo, OrganizationsRepository organizationsRepo,
      UserRanksRepository userRanksRepo, UserRolesRepository userRolesRepo,
      PasswordEncoder passwordEncoder, ArticleTagsRepository articleTagsRepo,
      ArticleTypeRepository articleTypeRepo) {
    this.usersRepo = usersRepo;
    this.organizationsRepo = organizationsRepo;
    this.passwordEncoder = passwordEncoder;
    this.userRanksRepo = userRanksRepo;
    this.userRolesRepo = userRolesRepo;
    this.articleTagsRepo = articleTagsRepo;
    this.articleTypeRepo = articleTypeRepo;

    if (tempPassword == null) {
      tempPassword = egyptianFaker.internet().password(8, 8, true, false, true);
      System.out.println("Temp password is empty. Using this password instead: " + tempPassword);
    }
  }

  @Override
  public void run(String... args) throws Exception {
    ApplicationUser rootUser = null;
    if (createRanks) {
      UserRank smurfsRank = new UserRank("Smurfs",
          "Our youngest members who start their scouting journey through play, exploration, and basic outdoor skills.",
          7, 9);

      UserRank cubsRank = new UserRank("Cubs",
          "Cubs develop teamwork, outdoor skills and self-confidence through games, activities and adventures.", 9, 12);

      UserRank scoutsRank = new UserRank("Scouts",
          "Scouts gain independence through camping, hiking, and community service while earning badges.", 12, 15);

      UserRank seniorScoutsRank = new UserRank("Senior Scouts",
          "Senior scouts build leadership skills through challenging outdoor adventures and community projects.", 15,
          17);

      UserRank roversRank = new UserRank("Rovers",
          "Our young adult members who focus on leadership, service, and advanced outdoor skills.", 18);

      UserRank leaderRank = new UserRank("Leaders",
          "A Scout leader is an individual responsible for guiding and supporting scouts, organizing committees, planning activities and events, and making sure everything runs smoothly to create a successful and meaningful scouting experience for everyone involved.");

      userRanksRepo.save(smurfsRank);
      userRanksRepo.save(cubsRank);
      userRanksRepo.save(scoutsRank);
      userRanksRepo.save(seniorScoutsRank);
      userRanksRepo.save(roversRank);
      userRanksRepo.save(leaderRank);
    }

    if (createOrganizations) {
      Organization sportingScout = new Organization("Alexandria Sporting Scouts",
          "The Sporting Scouts are an Alexandria-based scouting organization headquartered at Sporting Club, Alexandria");
      organizationsRepo.save(sportingScout);
    }

    if (createRoles) {
      UserRole viewerRole = new UserRole("Viewer",
          "Access is restricted to read-only capabilities. Members assigned to this role are permitted to browse and read all publicly available platform content but do not possess permissions to create, modify, or delete data.",
          Set.of());

      UserRole editorRole = new UserRole("Editor",
          "Users in this role are authorized to update and edit existing articles across the knowledge bank to ensure accuracy and formatting compliance, but they cannot author new articles or manage media assets.",
          Set.of(Permissions.ARTICLE_EDIT));

      UserRole writerRole = new UserRole("Writer",
          "Beyond the editing privileges of an Editor, a Writer has the authorization to create and publish brand-new articles and upload related media files, such as images or videos, to support their text.",
          Set.of(Permissions.ARTICLE_EDIT, Permissions.ARTICLE_WRITE,
              Permissions.MEDIA_UPLOAD));

      UserRole reviewerRole = new UserRole("Reviewer",
          "Reviewers inherit all the article generation and media upload capabilities of a Writer, with the addition of the authority to evaluate, approve, or reject newly drafted submissions before they become public.",
          Set.of(
              Permissions.ARTICLE_EDIT, Permissions.ARTICLE_WRITE, Permissions.ARTICLE_REVIEW,
              Permissions.MEDIA_UPLOAD, Permissions.TAG_EDIT, Permissions.TAG_CREATE));

      UserRole supervisorRole = new UserRole("Supervisor",
          "Supervisors manage daily operations and organizational structures. This role has the authority to oversee content life cycles (including article deletion), create and manage organizational units, and modify user role assignments within their scope.",
          Set.of(
              Permissions.ARTICLE_EDIT, Permissions.ARTICLE_WRITE, Permissions.ARTICLE_REVIEW,
              Permissions.ARTICLE_DELETE, Permissions.MEDIA_UPLOAD, Permissions.USER_VIEW,
              Permissions.USER_ROLE_EDIT, Permissions.ORGANIZATION_CREATE,
              Permissions.ORGANIZATION_EDIT, Permissions.TAG_CREATE, Permissions.TAG_EDIT,
              Permissions.TAG_DELETE));

      UserRole adminRole = new UserRole("Admin",
          "Administrators hold global administrative control over the platform. Administrators possess full operational rights, including system-wide configuration management, user rank adjustments, and the authority to execute destructive actions such as deleting user profiles or entire organizations.",
          Set.of(
              Permissions.ARTICLE_EDIT, Permissions.ARTICLE_WRITE, Permissions.ARTICLE_REVIEW,
              Permissions.ARTICLE_DELETE, Permissions.MEDIA_UPLOAD, Permissions.MEDIA_DELETE,
              Permissions.USER_VIEW, Permissions.USER_DELETE,
              Permissions.ORGANIZATION_CREATE, Permissions.ORGANIZATION_EDIT,
              Permissions.ORGANIZATION_DELETE, Permissions.USER_ROLE_EDIT, Permissions.USER_RANK_EDIT,
              Permissions.ROLE_CREATE, Permissions.ROLE_EDIT, Permissions.ROLE_DELETE,
              Permissions.RANK_CREATE, Permissions.RANK_EDIT, Permissions.RANK_DELETE,
              Permissions.TAG_DELETE, Permissions.TAG_EDIT, Permissions.TAG_CREATE,
              Permissions.TYPE_CREATE));

      UserRole rootRole = new UserRole("Root",
          "Root users have as much power as admin users. However, they can also access the site's documentation. They are intended to the first line of defence and work closely with developers when a bug is discovered",
          Set.of(
              Permissions.ARTICLE_EDIT, Permissions.ARTICLE_WRITE, Permissions.ARTICLE_REVIEW,
              Permissions.ARTICLE_DELETE,
              Permissions.MEDIA_UPLOAD, Permissions.MEDIA_DELETE,
              Permissions.USER_VIEW, Permissions.USER_DELETE, Permissions.ORGANIZATION_CREATE,
              Permissions.ORGANIZATION_EDIT,
              Permissions.ORGANIZATION_DELETE, Permissions.USER_ROLE_EDIT, Permissions.USER_RANK_EDIT,
              Permissions.ROLE_CREATE, Permissions.ROLE_EDIT, Permissions.ROLE_DELETE,
              Permissions.RANK_CREATE, Permissions.RANK_EDIT, Permissions.RANK_DELETE,
              Permissions.TAG_DELETE, Permissions.TAG_EDIT, Permissions.TAG_CREATE,
              Permissions.DOCUMENTATION_ACCESS,
              Permissions.TYPE_EDIT, Permissions.TYPE_CREATE, Permissions.TYPE_DELETE));

      userRolesRepo.save(viewerRole);
      userRolesRepo.save(editorRole);
      userRolesRepo.save(writerRole);
      userRolesRepo.save(reviewerRole);
      userRolesRepo.save(supervisorRole);
      userRolesRepo.save(adminRole);
      userRolesRepo.save(rootRole);
    }

    Optional<Organization> tempOrganization = organizationsRepo.findByName("Alexandria Sporting Scouts");
    Organization rootOrganization = tempOrganization.isPresent() ? tempOrganization.get() : null;

    Optional<UserRank> tempRank = userRanksRepo.findByName("Rovers");
    UserRank finalRank = tempRank.isPresent() ? tempRank.get() : null;

    Optional<UserRole> tempAdminRole = userRolesRepo.findByName("Admin");
    UserRole finalAdminRole = tempAdminRole.isPresent() ? tempAdminRole.get() : null;

    Optional<UserRole> tempViewerRole = userRolesRepo.findByName("Viewer");
    UserRole finalViewerRole = tempViewerRole.isPresent() ? tempViewerRole.get() : null;

    if (createRoot) {
      rootUser = ApplicationUser.builder()
          .name("Root User")
          .email("root@example.com")
          .phoneNumber("01245678910")
          .organization(rootOrganization)
          .rank(finalRank)
          .role(finalAdminRole)
          .birthdate(LocalDate.of(2000, 1, 1))
          .password(this.passwordEncoder.encode(tempPassword))
          .build();
      usersRepo.save(rootUser);
    }

    if (createUsers) {
      for (int i = 0; i < 5; i++) {
        tempPassword = egyptianFaker.internet().password(8, 8, true, false, true);
        ApplicationUser tempUser = ApplicationUser.builder()
            .name(faker.name().name())
            .email(faker.internet().emailAddress())
            .phoneNumber(egyptianFaker.phoneNumber().cellPhone().replace(" ", ""))
            .organization(rootOrganization)
            .password(passwordEncoder.encode(tempPassword))
            .birthdate(LocalDate.of(2000, 1, 1))
            .rank(finalRank)
            .role(finalViewerRole)
            .build();
        ApplicationUser newUser = usersRepo.save(tempUser);
        System.out.println("Created temp user with ID " + newUser.getId() + " and password " + tempPassword);
      }
    }

    if (createTags) {
      List<ArticleTag> tags = List.of(
          new ArticleTag("EN"), new ArticleTag("AR"), new ArticleTag("Fire"),
          new ArticleTag("Scout Law"), new ArticleTag("Knots"), new ArticleTag("Tents"),
          new ArticleTag("Report"));
      if (rootUser != null) {
        for (ArticleTag tag : tags) {
          tag.setCreatedBy(rootUser);
          tag.setUpdatedBy(rootUser);
          articleTagsRepo.save(tag);
        }
      }
    }

    if (createTypes) {
      List<ArticleType> types = List.of(
          new ArticleType("Tutorial",
              "Tutorial articles are designed to teach the reader how to achieve a certain goal or perform a certain action in a step by step process"),
          new ArticleType("Documentation",
              "Documentation articles are designed to act as an absolute source of knowledge for a certain topic. It does not have to teach it in a step-by-step manner, however"),
          new ArticleType("Report",
              "Reports usually contain detailed descriptions about a particular event or statistical analysis"));

      if (rootUser != null) {
        for (ArticleType type : types) {
          type.setCreatedBy(rootUser);
          type.setUpdatedBy(rootUser);
          articleTypeRepo.save(type);
        }
      }
    }
  }
}
