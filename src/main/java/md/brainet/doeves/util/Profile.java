package md.brainet.doeves.util;

public enum Profile {
    //TODO replace active profiles by this enum
    TEST("test"),
    DEVELOPMENT("dev"),
    PRODUCTION("prod");

    private final String profileName;

    Profile(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}
