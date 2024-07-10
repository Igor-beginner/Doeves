package md.brainet.doeves.note;

public enum ViewContext {
    CATALOG, HOME_PAGE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
