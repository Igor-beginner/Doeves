package md.brainet.doeves.util;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SpringActiveProfiles {

    private final List<String> activeProfiles;

    public SpringActiveProfiles(Environment environment) {
        this.activeProfiles = Arrays.stream(environment.getActiveProfiles()).toList();
    }

    public boolean contains(Profile profile) {
        return activeProfiles.contains(profile.getProfileName());
    }
}
