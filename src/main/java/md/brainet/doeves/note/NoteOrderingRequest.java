package md.brainet.doeves.note;

public record NoteOrderingRequest(
        Integer noteId,
        Integer catalogId,
        Integer orderNumber
) {
}
