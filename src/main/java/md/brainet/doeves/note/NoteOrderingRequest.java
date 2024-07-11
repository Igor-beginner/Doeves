package md.brainet.doeves.note;

public record NoteOrderingRequest(
        Integer noteId,
        Integer currentOrderNumber,
        Integer newOrderNumber,
        ViewContext context,
        Integer contextId
) {
}
