package ru.agroeco.pulse.videoevents.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;

@Service @RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final SecurityUserRepository repository;
    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var user = repository.findByUsername(username).filter(SecurityUser::isEnabled)
            .orElseThrow(() -> new UsernameNotFoundException("User not available"));
        var names = new HashSet<String>();
        for (AppRole role : user.getRoles()) {
            names.add("ROLE_" + role.name());
            names.addAll(role.permissions());
        }
        var authorities = names.stream().sorted()
            .map(name -> (GrantedAuthority) new SimpleGrantedAuthority(name)).toList();
        return new AppPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), authorities);
    }
}
