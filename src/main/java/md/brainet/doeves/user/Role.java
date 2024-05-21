package md.brainet.doeves.user;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_".concat(this.name());
    }

    public static Role getDefault() {
        return USER;
    }
}
