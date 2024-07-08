package md.brainet.doeves.exception;

public class NoteNotFoundException extends RuntimeException {

    private final int noteId;

    public NoteNotFoundException(int noteId) {
        super("Note [id=%s] not found".formatted(noteId));
        this.noteId = noteId;
    }

    public int getNoteId() {
        return noteId;
    }
}
