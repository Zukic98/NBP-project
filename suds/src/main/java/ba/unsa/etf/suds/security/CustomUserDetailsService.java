package ba.unsa.etf.suds.security;

import ba.unsa.etf.suds.model.NbpRole;
import ba.unsa.etf.suds.model.NbpUser;
import ba.unsa.etf.suds.repository.NbpRoleRepository;
import ba.unsa.etf.suds.repository.NbpUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final NbpUserRepository userRepository;
    private final NbpRoleRepository roleRepository;

    public CustomUserDetailsService(NbpUserRepository userRepository, NbpRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NbpUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));

        NbpRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UsernameNotFoundException("Rola nije pronađena za korisnika: " + username));

        return new CustomUserDetails(user, role.getName());
    }
}
