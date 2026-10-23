package ru.agroeco.pulse.videoevents.security;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class AppUserDetailsService implements UserDetailsService{
  private final Map<String,UserDetails> users=Map.of(
    "operator",User.withUsername("operator").password("{noop}operator").roles("OPERATOR").build(),
    "admin",User.withUsername("admin").password("{noop}admin").roles("ADMIN").build());
  @Override
  public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException{return users.get(username);}
}
