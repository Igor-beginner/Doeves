package md.brainet.doeves.exception;

import java.util.List;

public class NotesNotExistException extends RuntimeException {
    private final List<Integer> noteIds;

    public NotesNotExistException(List<Integer> noteIds) {
        super("Request was rejected cause the next note id's do not exist: "
                .concat(noteIds.toString()));
        this.noteIds = noteIds;
    }

    public List<Integer> getNoteIds() {
        return noteIds;
    }
}
