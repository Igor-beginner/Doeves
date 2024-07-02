package md.brainet.doeves.note;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.catalog.CatalogDao;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(
        scripts = "classpath:data/catalog_test_data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class NoteDaoTest extends IntegrationTestBase {

    @Autowired
    NoteDao noteDao;

    @Autowired
    CatalogDao catalogDao;


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
    void insertNote_catalogExists_noteInserted() {
        //given
        final int catalogId = 1;
        final int expectedCatalogDataCount = 1;
        var noteDTO = new NoteDTO("SomeName", null, catalogId, 0);

        //when
        var note = noteDao.insertNote(noteDTO);

        //then
        var notes = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(expectedCatalogDataCount, notes.size());
        assertTrue(note.isPresent());
    }

    @Test
    void insertNote_catalogNotExists_noteNoteInserted() {
        //given
        final int catalogId = 10;
        final int expectedCatalogDataCount = 0;
        var noteDTO = new NoteDTO("SomeName", null, catalogId, 0);

        //when
        var note = noteDao.insertNote(noteDTO);

        //then
        var notes = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(expectedCatalogDataCount, notes.size());
        assertFalse(note.isPresent());
    }

    @Test
    void updateOrderNumberByNoteId_idExists_expectTrue() {
        //given
        final int noteId = 1;
        final int orderNumber = 10;

        //when
        var updated = noteDao.updateOrderNumberByNoteId(noteId, orderNumber);

        //then
        assertTrue(updated);
    }

    @Test
    void updateOrderNumberByNoteId_idNotExists_expectFalse() {
        //given
        final int noteId = 13123;
        final int orderNumber = 0;

        //when
        var updated = noteDao.updateOrderNumberByNoteId(noteId, orderNumber);

        //then
        assertFalse(updated);
    }

    @Test
    void updateOrderNumberByNoteId_orderNumberIsBusy_expectFalse() {
        //given
        final int noteId = 1;
        final int orderNumber = 1;

        //when
        var updated = noteDao.updateOrderNumberByNoteId(noteId, orderNumber);

        //then
        assertFalse(updated);
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
    void removeByNoteId_idExists_expectTrue() {
        //given
        final int noteId = 1;

        //when
        var updated = noteDao.removeByNoteId(noteId);

        //then
        assertTrue(updated);
    }

    @Test
    void removeByNoteId_idNotExists_expectFalse() {
        //given
        final int noteId = 11;

        //when
        var updated = noteDao.removeByNoteId(noteId);

        //then
        assertFalse(updated);
    }
}