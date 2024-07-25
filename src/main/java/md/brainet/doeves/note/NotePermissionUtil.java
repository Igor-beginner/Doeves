package md.brainet.doeves.note;

import md.brainet.doeves.exception.NoteNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotePermissionUtil {

    private final NoteDao noteDao;

    public NotePermissionUtil(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public boolean haveEnoughRights(Integer noteId, Integer userId) {
        Integer ownerId = noteDao.selectOwnerIdByNoteId(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
        return userId.equals(ownerId);
    }

    public boolean haveEnoughRights(List<Integer> notesId, Integer userId) {
        return notesId.stream()
                .allMatch(id -> haveEnoughRights(id, userId));
    }
}
