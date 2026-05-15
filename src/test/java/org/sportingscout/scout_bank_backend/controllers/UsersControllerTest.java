package org.sportingscout.scout_bank_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.sportingscout.scout_bank_backend.services.UsersService;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.mappers.users.AllUsersResponseMapper;
import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponseDTO;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import net.datafaker.Faker;

import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UsersController.class)
public class UsersControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AllUsersResponseMapper mapper;

  @MockitoBean
  private UsersService usersService;

  private static final Faker faker = new Faker();

  @Test
  public void testGetExistingUser() throws Exception {
    ApplicationUser fakeUser = new ApplicationUser();
    fakeUser.setId(1L);
    fakeUser.setName("User-1");

    when(usersService.getUserById(1L)).thenReturn(Optional.of(fakeUser));

    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("User-1"));
  }

  @Test
  void testGetNonExistingUser() throws Exception {
    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetAllusers() throws Exception {
    List<ApplicationUser> ls = new ArrayList<ApplicationUser>();
    for (Long i = 0L; i < 10L; i++) {
      ApplicationUser temp = new ApplicationUser();
      temp.setId(i);
      temp.setName(faker.name().fullName());
      when(usersService.getUserById(i)).thenReturn(Optional.of(temp));
      ls.add(temp);
    }
    List<AllUsersResponseDTO> responseDtos = mapper.toResponse(ls);
    when(usersService.getAllUsers()).thenReturn(responseDtos);
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(10)));

  }
}
