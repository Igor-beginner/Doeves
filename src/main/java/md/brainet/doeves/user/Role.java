package md.brainet.doeves.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;

public enum Role implements GrantedAuthority {
    USER, ADMIN;

    public static Role parse(GrantedAuthority authority) {
        return Arrays.stream(values())
                .filter(role -> role.name()
                        .equals(
                                authority
                                        .getAuthority()
                                        .substring(5)
                        )
                ).findFirst().orElseThrow();
    }

    @Override
    public String getAuthority() {
        return "ROLE_".concat(this.name());
    }

    public static Role getDefault() {
        return USER;
    }
}
