package md.brainet.doeves.note;

public record LimitedListNoteRequest(
        Integer noteId,
        Integer offset,
        Integer limit,
        Boolean includingCatalogs
) {
}
