package md.brainet.doeves.note;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.catalog.CatalogDTO;
import md.brainet.doeves.catalog.CatalogDao;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoteDaoTest extends IntegrationTestBase {

    @Autowired
    JdbcNoteDao noteDao;

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    UserDao userDao;


    @Test
    void selectByNoteId_idExists_expectNotePresent() {
        //given
        var noteId = 1;

        //when
        var note = noteDao.selectByNoteId(noteId);

        //then
        assertTrue(note.isPresent());
    }

    @Test
    void selectByNoteId_idNotExists_expectNoteEmpty() {
        //given
        var noteId = 12;

        //when
        var note = noteDao.selectByNoteId(noteId);

        //then
        assertFalse(note.isPresent());
    }

    @Test
    void insertNote() {
        //given
        final int expectedId = 6;

        var note = new NoteDTO();
        note.setCatalogId(10);
        var firstNoteId = noteDao.selectFirstNoteIdFromCatalog(10);

        //when
        var actualId = noteDao.insertNote(
                userDao.selectUserById(1).get(),
                note)
                .get().id();

        //then
        assertEquals(actualId, noteDao.findPrevIdFor(firstNoteId, note.getCatalogId()));
        assertEquals(expectedId, actualId);
    }

    @Test
    void updateOrderNumberByNoteId_changingNotesNotOnEdges() {
        //given
        final int prevNoteId = 1;
        final Integer nextReplacingNoteId = 2;

        final Integer prevInsteadNoteId = null;
        final int currentNoteId = 3;
        final int nextInsteadNoteId = 1;

        final int catalogId = 10;

        //when
        noteDao.updateOrderNumberByNoteId(prevNoteId, currentNoteId, catalogId);

        //then
        var insteadNote = noteDao.selectByNoteId(currentNoteId);
        assertEquals(prevNoteId, noteDao.findPrevIdFor(insteadNote.get().id(), catalogId));

        var nextCurrentNote = noteDao.selectByNoteId(nextInsteadNoteId);
        assertEquals(prevInsteadNoteId, noteDao.findPrevIdFor(nextCurrentNote.get().id(), catalogId));

        var nextPrevNote = noteDao.selectByNoteId(nextReplacingNoteId);
        assertEquals(currentNoteId, noteDao.findPrevIdFor(nextPrevNote.get().id(), catalogId));

        var allNotes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(1, 0, 10);
        assertEquals(5, allNotes.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromMiddleToStart() {
        //given
        final Integer prevNoteId = null;
        final Integer nextReplacingNoteId = 3;

        final Integer prevInsteadNoteId = 3;
        final int currentNoteId = 1;
        final int nextInsteadNoteId = 2;

        final int catalogId = 10;

        //when
        noteDao.updateOrderNumberByNoteId(prevNoteId, currentNoteId, catalogId);

        //then
        var insteadNote = noteDao.selectByNoteId(currentNoteId);
        assertEquals(prevNoteId, noteDao.findPrevIdFor(insteadNote.get().id(), catalogId));

        var nextCurrentNote = noteDao.selectByNoteId(nextInsteadNoteId);
        assertEquals(prevInsteadNoteId, noteDao.findPrevIdFor(nextCurrentNote.get().id(), catalogId));

        var nextPrevNote = noteDao.selectByNoteId(nextReplacingNoteId);
        assertEquals(currentNoteId, noteDao.findPrevIdFor(nextPrevNote.get().id(), catalogId));

        var allNotes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(1, 0, 10);
        assertEquals(5, allNotes.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromEndToStart() {
        //given
        final Integer prevNoteId = 3;
        final Integer nextReplacingNoteId = 1;

        final Integer prevInsteadNoteId = 1;
        final int currentNoteId = 2;

        final int catalogId = 10;

        //when
        noteDao.updateOrderNumberByNoteId(prevNoteId, currentNoteId, catalogId);

        //then
        var insteadNote = noteDao.selectByNoteId(currentNoteId);
        assertEquals(prevNoteId, noteDao.findPrevIdFor(insteadNote.get().id(), catalogId));

        var nextPrevNote = noteDao.selectByNoteId(nextReplacingNoteId);
        assertEquals(currentNoteId, noteDao.findPrevIdFor(nextPrevNote.get().id(), catalogId));

        var allNotes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(1, 0, 10);
        assertEquals(5, allNotes.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromStartToEnd() {
        //given
        final Integer prevNoteId = 2;

        final int currentNoteId = 3;

        final int catalogId = 10;

        //when
        noteDao.updateOrderNumberByNoteId(prevNoteId, currentNoteId, catalogId);

        //then
        var insteadNote = noteDao.selectByNoteId(currentNoteId);
        assertEquals(prevNoteId, noteDao.findPrevIdFor(insteadNote.get().id(), catalogId));

        var prevNote = noteDao.selectByNoteId(1);
        assertNull(noteDao.findPrevIdFor(prevNote.get().id(), catalogId));

        var allNotes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(1, 0, 10);
        assertEquals(5, allNotes.size());
    }

    @Test
    void updateOrderNumberByCatalogId_idNotExists() {
        //given
        final Integer prevNoteId = 13216;
        final int currentNoteId = 132130;

        //when
        Executable executable = () -> noteDao.updateOrderNumberByNoteId(prevNoteId, currentNoteId, 10);

        //then
        assertThrows(EmptyResultDataAccessException.class, executable);
    }


    @Test
    void updateNameByNoteId_idExists_expectTrue() {
        //when
        final int noteId = 1;
        final String newName = "New Task Title";

        //when
        boolean updated = noteDao.updateNameByNoteId(noteId, newName);

        //then
        assertTrue(updated);
        var note = noteDao.selectByNoteId(noteId);
        assertTrue(note.isPresent());
        assertEquals(newName, note.get().name());
    }

    @Test
    void updateNameByNoteId_idNotExists_expectFalse() {
        //when
        final int noteId = 123;
        final String newName = "New Task Title";

        //when
        boolean updated = noteDao.updateNameByNoteId(noteId, newName);

        //then
        assertFalse(updated);
    }

    @Test
    void updateDescriptionByNoteId_idExists_expectTrue() {
        //when
        final int noteId = 1;
        final String newDescription = "New Description";

        //when
        boolean updated = noteDao.updateDescriptionByNoteId(noteId, newDescription);

        //then
        assertTrue(updated);
        var note = noteDao.selectByNoteId(noteId);
        assertTrue(note.isPresent());
        assertEquals(newDescription, note.get().description());
    }

    @Test
    void updateDescriptionByNoteId_idNotExists_expectFalse() {
        //when
        final int noteId = 123;
        final String newName = "description";

        //when
        boolean updated = noteDao.updateNameByNoteId(noteId, newName);

        //then
        assertFalse(updated);
    }

    @Test
    void removeByCatalogId_firstOwnerCatalogId_expectTrue() {
        //given
        final int catalogId = 10;
        final int noteId = 1;

        //when
        boolean removed = noteDao.removeByNoteId(noteId, catalogId);

        //then
        assertTrue(removed);
    }

    @Test
    void removeByCatalogId_middleNoteId_checkRewritingLinks() {
        //given
        final int catalogId = 10;

        final int prevNoteId = 3;
        final int noteId = 1;
        final int nextNoteId = 2;

        //when
        boolean removed = noteDao.removeByNoteId(noteId, catalogId);

        //then
        int actualPrevId = noteDao.findPrevIdFor(nextNoteId, catalogId);
        assertTrue(removed);
        assertEquals(prevNoteId, actualPrevId);
    }

    @Test
    void removeByCatalogId_startingNoteId_checkRewritingLinks() {
        //given
        final int catalogId = 10;

        final Integer prevNoteId = null;
        final int noteId = 3;
        final int nextNoteId = 1;

        //when
        boolean removed = noteDao.removeByNoteId(noteId, catalogId);

        //then
        Integer actualPrevId = noteDao.findPrevIdFor(nextNoteId, catalogId);
        assertTrue(removed);
        assertEquals(prevNoteId, actualPrevId);
    }

    @Test
    void removeByCatalogId_idNotExists_expectFalse() {
        //given
        final int catalogId = 12133210;
        final int noteId = 23321;

        //when
        Executable executable = () -> noteDao.removeByNoteId(noteId, catalogId);

        //then
        assertThrows(EmptyResultDataAccessException.class, executable);
    }
    @Test
    void updateCatalogId_catalogNotExists_expectFalse() {
        //given
        final int prevId = 1;
        final int noteId = 3;
        final int oldCatalogId = 101231;
        final int newCatalogId = 14;

        //when
        Executable executable = () -> noteDao.moveNoteIdToNewCatalogId(noteId, oldCatalogId, newCatalogId);

        //then
        assertThrows(EmptyResultDataAccessException.class, executable);
    }

    @Test
    void updateCatalogId_noteNotExists_expectFalse() {
        //given
        final int prevId = 1;
        final int noteId = 32313;
        final int oldCatalogId = 10;
        final int newCatalogId = 14;

        //when
        Executable executable = () -> noteDao.moveNoteIdToNewCatalogId(noteId, oldCatalogId, newCatalogId);

        //then
        assertThrows(EmptyResultDataAccessException.class, executable);
    }

    @Test
    void updateCatalogIdNull_expectTrue() {
        //given
        final int prevId = 1;
        final int noteId = 3;
        final int oldCatalogId = 10;
        final int newCatalogId = 14;

        //when
        noteDao.moveNoteIdToNewCatalogId(noteId, oldCatalogId, newCatalogId);

        //then

        var newCatalogNotes = catalogDao.selectAllNotesByCatalogId(newCatalogId, 0, 10);
        assertEquals(1, newCatalogNotes.size());

        assertNull(noteDao.findPrevIdFor(prevId, oldCatalogId));
    }

    @Test
    void selectAllNotesByOwnerIdIncludingCatalogs_expectCorrectResponseSize() {
        //given
        final int expectedNotesCountIncludingCatalogs = 5;
        final int userId = 1;

        //when
        var notes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(userId, 0 ,10);

        //then
        long countNotesWithoutCatalogs = notes.stream().filter(n -> n.catalog() == null).count();
        assertEquals(expectedNotesCountIncludingCatalogs, notes.size());
        assertEquals(2, countNotesWithoutCatalogs);
    }

    @Test
    void selectAllNotesByOwnerIdWithoutCatalogs_expectCorrectResponseSize() {
        //given
        final int expectedNotesCountIncludingCatalogs = 3;
        final int userId = 1;
        final var user = userDao.selectUserById(userId).get();
        Note note = noteDao.insertNote(user, new NoteDTO()).get();


        //when
        var notes = noteDao.selectAllNotesByOwnerIdWithoutCatalogs(user.getId(), 0 ,10);

        //then
        assertEquals(expectedNotesCountIncludingCatalogs, notes.size());
        assertNull(notes.get(0).catalog());
        assertEquals(6, notes.get(0).id());
    }
}