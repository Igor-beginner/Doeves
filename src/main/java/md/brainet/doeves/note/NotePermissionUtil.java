package md.brainet.doeves.note;

import org.springframework.stereotype.Component;

@Component
public class NotePermissionUtil {

    private final NoteDao noteDao;

    public NotePermissionUtil(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public boolean haveEnoughRights(Integer noteId, Integer userId) {
        Integer ownerId = noteDao.selectOwnerIdByNoteId(noteId);
        return userId.equals(ownerId);
    }
}
