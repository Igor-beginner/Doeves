package md.brainet.doeves.note;

public record LimitedListNoteRequest(
        Integer ownerId,
        Integer offset,
        Integer limit,
        Boolean includingCatalogs
) {
}
