package org.sportingscout.scout_bank_backend.security;

import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserDetails implements UserDetailsService {
  private final UsersRepository usersRepo;

  public ApplicationUserDetails(UsersRepository usersRepo) {
    this.usersRepo = usersRepo;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return usersRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }
}
