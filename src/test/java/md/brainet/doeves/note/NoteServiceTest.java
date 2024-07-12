package md.brainet.doeves.note;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.catalog.CatalogDao;
import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.exception.NoteNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoteServiceTest extends IntegrationTestBase {

    @Autowired
    NoteDao noteDao;

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    NoteService noteService;

    @Test
    void createNote_orderNumberPresent_expectOffsetNextItems() {
        //given
        final int ownerId = 1;
        final NoteDTO noteDTO = new NoteDTO(
                "Some note1",
                "Some description 2",
                2,
                3
        );

        //when
        var note = noteService.createNote(ownerId, noteDTO);

        //then
        assertEquals(ownerId, note.ownerId());
        var catalog = catalogDao.selectAllNotesByCatalogId(note.catalogId(), 0, 10);
        assertEquals(1, catalog.size());
    }

    @Test
    void createNote_orderNumberIsNotPresent_expectAssigningOrderNumberZero() {
        //given
        final int ownerId = 1;
        final int expectedAssigningOrderNumber = 0;
        final NoteDTO noteDTO = new NoteDTO(
                "Some note1",
                "Some description 2",
                2,
                null
        );

        //when
        var note = noteService.createNote(ownerId, noteDTO);

        //then
        assertEquals(expectedAssigningOrderNumber, note.orderNumber());
    }

    @Test
    void changeName_idNoteExists_expectChanges() {
        //given
        final String newName = "some new name";
        final int noteId = 1;

        //when
        noteDao.updateNameByNoteId(noteId, newName);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertEquals(newName, note.get().name());
    }

    @Test
    void changeName_idNoteNotExists_expectNoteNotFoundException() {
        //given
        final String newDescription = "some new description";
        final int noteId = 1;

        //when
        Executable executable = () -> noteDao.updateDescriptionByNoteId(noteId, newDescription);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void changeDescription_idNoteExists_expectChanges() {
        //given
        final String newDescription = "some new description";
        final int noteId = 1;

        //when
        noteDao.updateDescriptionByNoteId(noteId, newDescription);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertEquals(newDescription, note.get().description());
    }

    @Test
    void changeDescription_idNoteNotExists_expectNoteNotFoundException() {
        //given
        final String newDescription = "some new description";
        final int noteId = 1;

        //when
        Executable executable = () -> noteDao.updateDescriptionByNoteId(noteId, newDescription);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }


//    @Test
//    void changeOrderNumber_noteIdExists_expectChanges() {
//        //given
//        final int newOrder = 2;
//        final int noteId = 1;
//
//        //when
//        noteService.changeOrderNumber(noteId, newOrder);
//
//        //then
//        var note = noteDao.selectByNoteId(3);
//        assertEquals(3, note.get().orderNumber());
//    }

//    @Test
//    void changeOrderNumber_noteIdNotExists_expectNoteNotFoundException() {
//        //given
//        final int newOrder = 2;
//        final int noteId = 1321;
//
//        //when
//        Executable executable = () -> noteService.changeOrderNumber(noteId, newOrder);
//
//        //then
//        assertThrows(NoteNotFoundException.class, executable);
//    }

    @Test
    void removeNote_noteIdExists_expectRemoved() {
        //given
        var noteId = 1;

        //when
        noteService.removeNote(noteId);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertFalse(note.isPresent());
    }

    @Test
    void removeNote_noteIdNotExists_expectNoteNotFoundException() {
        //given
        var noteId = 1321;

        //when
        Executable executable = () -> noteService.removeNote(noteId);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void fetchNote_noteIdExits_expectNote() {
        //given
        final int noteId = 1;

        //when
        var note = noteService.fetchNote(noteId);

        //then
        assertEquals("Test catalog 0", note.name());
    }

    @Test
    void fetchNote_noteIdNotExits_expectNote() {
        //given
        final int noteId = 1213;

        //when
        Executable executable = () -> noteService.fetchNote(noteId);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void changeCatalog_catalogExists_expectChanges() {
        //given
        final int noteId = 1;
        final int catalogId = 2;

        //when
        noteService.changeCatalog(noteId, catalogId);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertEquals(catalogId, note.get().catalogId());
    }

    @Test
    void changeCatalog_catalogNotExists_expectCatalogNotFound() {
        //given
        final int noteId = 1;
        final int catalogId = 22313;

        //when
        Executable executable = () -> noteService.changeCatalog(noteId, catalogId);

        //then
        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void changeCatalog_noteNotExists_expectNoteNotFound() {
        //given
        final int noteId = 3213;
        final int catalogId = 2;

        //when
        Executable executable = () -> noteService.changeCatalog(noteId, catalogId);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void fetchAllOwnerNote_includingCatalogs_expectThreeTasks() {
        //given
        final int userId = 1;
        var request = new LimitedListNoteRequest(
                0,
                10,
                true
        );

        //when
        var notes = noteService.fetchAllOwnerNote(userId, request);

        //then
        assertEquals(3, notes.size());
    }

    @Test
    void fetchAllOwnerNote_withoutCatalogs_expectOneTask() {
        //given
        final int userId = 1;
        var request = new LimitedListNoteRequest(
                0,
                10,
                false
        );

        //when
        var notes = noteService.fetchAllOwnerNote(userId, request);

        //then
        assertEquals(1, notes.size());
    }
}