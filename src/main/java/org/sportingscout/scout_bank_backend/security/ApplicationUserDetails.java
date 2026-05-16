package org.sportingscout.scout_bank_backend.security;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ApplicationUserDetails implements UserDetailsService {
  private final UsersRepository usersRepo;

  public ApplicationUserDetails(UsersRepository usersRepo) {
    this.usersRepo = usersRepo;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    ApplicationUser user = usersRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

    return User.builder()
        .username(user.getEmail())
        .password(user.getPassword()).authorities(user.getRole().getAuthorities().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList()))
        .build();
  }
}
