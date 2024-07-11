package md.brainet.doeves.note;

public enum ViewContext {
    CATALOG, HOME_PAGE;

    public Integer getContextId(Note note) {
        return switch (this){
            case CATALOG -> note.catalogId();
            case HOME_PAGE -> note.ownerId();
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
