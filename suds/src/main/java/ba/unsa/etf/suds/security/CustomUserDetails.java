package ba.unsa.etf.suds.security;

import ba.unsa.etf.suds.model.NbpUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final NbpUser user;
    private final String roleName;

    public CustomUserDetails(NbpUser user, String roleName) {
        this.user = user;
        this.roleName = roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRoleName() {
        return roleName;
    }

    public NbpUser getUser() {
        return user;
    }
}
