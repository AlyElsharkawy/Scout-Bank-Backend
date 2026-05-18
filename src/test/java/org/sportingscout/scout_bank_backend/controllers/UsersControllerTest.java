package org.sportingscout.scout_bank_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.sportingscout.scout_bank_backend.services.users.UsersService;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.mappers.users.AllUsersResponseMapper;
import org.sportingscout.scout_bank_backend.mappers.users.SingleUserResponseMapper;
import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponse;
import org.sportingscout.scout_bank_backend.dtos.users.SingleUserResponse;
import org.sportingscout.scout_bank_backend.controllers.users.UsersController;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import java.time.LocalDate;

import net.datafaker.Faker;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import net.datafaker.Faker;

import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UsersController.class)
public class UsersControllerTest {

  private static final Faker egyptianFaker = new Faker(Locale.of("ar", "EG"));

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UsersService usersService;

  @MockitoBean
  private SingleUserResponseMapper singleUserMapper;

  @MockitoBean
  private AllUsersResponseMapper allUsersMapper;

  private static final Faker faker = new Faker();

  @Test
  public void testGetExistingUser() throws Exception {
    ApplicationUser fakeUser = new ApplicationUser();
    fakeUser.setId(1L);
    fakeUser.setName("User-1");
    fakeUser.setBirthDate(LocalDate.of(2000, 1, 1));
    fakeUser.setPhoneNumber("01205678910");
    fakeUser.setEmail(faker.internet().emailAddress());
    fakeUser.setPassword("123456aA$");

    SingleUserResponse fakeDto = new SingleUserResponse(
        "User-1",
        faker.internet().emailAddress(),
        "",
        egyptianFaker.phoneNumber().cellPhone().replace(" ", ""),
        1L,
        "Sporting Scouts",
        6L,
        "Leader",
        6L,
        "Admin",
        java.time.LocalDateTime.now(),
        LocalDate.of(2000, 1, 1),
        List.of());

    when(usersService.getUserById(1L)).thenReturn(Optional.of(fakeUser));
    when(singleUserMapper.toResponse(fakeUser)).thenReturn(fakeDto);

    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("User-1"));
  }

  @Test
  void testGetNonExistingUser() throws Exception {
    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isNotFound());
  }

  /*
   * @Test
   * void testGetAllusers() throws Exception {
   * List<ApplicationUser> ls = new ArrayList<ApplicationUser>();
   * for (Long i = 0L; i < 10L; i++) {
   * ApplicationUser temp = new ApplicationUser();
   * temp.setId(i);
   * temp.setName(faker.name().fullName());
   * when(usersService.getUserById(i)).thenReturn(Optional.of(temp));
   * ls.add(temp);
   * }
   * List<AllUsersResponse> responseDtos = allUsersMapper.toResponse(ls);
   * when(usersService.getAllUsers()).thenReturn(responseDtos);
   * when(allUsersMapper.toResponse(ls)).thenReturn(responseDtos);
   * mockMvc.perform(get("/api/users"))
   * .andExpect(status().isOk())
   * .andExpect(jsonPath("$", hasSize(10)));
   * 
   * }
   */
}
