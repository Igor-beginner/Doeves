package md.brainet.doeves.note;

public record LimitedListNoteRequest(
        Integer offset,
        Integer limit,
        Boolean includingCatalogs
) {
}
