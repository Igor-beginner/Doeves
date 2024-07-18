package md.brainet.doeves.catalog;

import md.brainet.doeves.user.User;

public class CatalogOrderingRequest {
    private Integer noteId;
    private Integer sourceCatalogId;
    private Integer destinationCatalogId;
    private User user;

    public CatalogOrderingRequest(Integer noteId,
                                  Integer sourceCatalogId,
                                  Integer destinationCatalogId,
                                  User user) {
        this.noteId = noteId;
        this.sourceCatalogId = sourceCatalogId;
        this.destinationCatalogId = destinationCatalogId;
        this.user = user;
    }

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public Integer getSourceCatalogId() {
        return sourceCatalogId;
    }

    public void setSourceCatalogId(Integer sourceCatalogId) {
        this.sourceCatalogId = sourceCatalogId;
    }

    public Integer getDestinationCatalogId() {
        return destinationCatalogId;
    }

    public void setDestinationCatalogId(Integer destinationCatalogId) {
        this.destinationCatalogId = destinationCatalogId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
