package md.brainet.doeves.note;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.catalog.CatalogDao;
import md.brainet.doeves.catalog.CatalogOrderingRequest;
import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.NotesNotExistException;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoteServiceTest extends IntegrationTestBase {

    @Autowired
    NoteDao noteDao;

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    NoteServiceImpl noteService;

    @Autowired
    UserDao userDao;

    @Test
    void createNote_orderNumberPresent_expectOffsetNextItems() {
        //given
        final int ownerId = 2;
        final var user = userDao.selectUserById(ownerId).get();
        final NoteDTO noteDTO = new NoteDTO();


        //when
        var note = noteService.createNote(user, noteDTO);

        //then
        var catalog = noteDao.selectAllNotesByOwnerIdWithoutCatalogs(ownerId, 0, 10);
        assertEquals(1, catalog.size());
    }

    @Test
    void changeName_idNoteExists_expectChanges() {
        //given
        final String newName = "some new name";
        final int noteId = 1;

        //when
        noteService.changeName(noteId, newName);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertEquals(newName, note.get().name());
    }

    @Test
    void changeName_idNoteNotExists_expectNoteNotFoundException() {
        //given
        final String newDescription = "some new description";
        final int noteId = 1323;

        //when
        Executable executable = () -> noteService.changeName(noteId, newDescription);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void changeDescription_idNoteExists_expectChanges() {
        //given
        final String newDescription = "some new description";
        final int noteId = 1;

        //when
        noteService.changeDescription(noteId, newDescription);

        //then
        var note = noteDao.selectByNoteId(noteId);
        assertEquals(newDescription, note.get().description());
    }

    @Test
    void changeDescription_idNoteNotExists_expectNoteNotFoundException() {
        //given
        final String newDescription = "some new description";
        final int noteId = 32123;

        //when
        Executable executable = () -> noteService.changeDescription(noteId, newDescription);

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }


    @Test
    void changeOrderNumber_noteIdExists_expectChanges() {
        //given
        final Integer prevNoteId = null;
        final int noteId = 2;
        final int catalogId = 10;

        //when
        noteService.changeOrderNumber(noteId, prevNoteId, catalogId);

        //then
        var notes = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(noteId, notes.get(0).id());
    }

    @Test
    void changeOrderNumber_noteIdNotExists_expectNoteNotFoundException() {
//        //given
//        final Integer prevNoteId = null;
//        final int noteId = 2321;
//        final int catalogId = 10;
//
//        //when
//        Executable executable = () -> noteService.changeOrderNumber(noteId, prevNoteId, catalogId);
//
//        //then
//        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void changeOrderNumber_catalogIdNotExists_expectNoteNotFoundException() {
        //given
//        final Integer prevNoteId = null;
//        final int noteId = 1;
//        final int catalogId = 1321230;
//
//        //when
//        Executable executable = () -> noteService.changeOrderNumber(noteId, prevNoteId, catalogId);
//
//        //then
//        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void removeNote_noteIdExists_expectRemoved() {
        //given
        final var noteId = 1;
        final var catalogId = 10;

        //when
        noteService.removeNotes(List.of(noteId), catalogId);

        //then
        var note = catalogDao.selectAllNotesByCatalogId(catalogId, 0 , 10);
        assertEquals(2, note.size());
    }

    @Test
    void removeNote_noteIdNotExists_expectNoteNotFoundException() {
        //given
        final var noteId = 13123;
        final var catalogId = 10;

        //when
        Executable executable = () -> noteService.removeNotes(List.of(noteId), catalogId);

        //then
        assertThrows(NotesNotExistException.class, executable);
    }

    @Test
    void removeNote_severalNotes_expectNoteNotFoundException() {
        //given
        final var noteId = List.of(3, 2);
        final var catalogId = 10;

        //when
        noteService.removeNotes(noteId, catalogId);

        //then
        noteId.forEach(id ->
            assertFalse(noteDao.selectByNoteId(id).isPresent())
        );
    }

    @Test
    void removeNote_oneOfNotesDontExist_expectNoteNotFoundException() {
        //given
        final var noteId = List.of(3, 2, 123);
        final var catalogId = 10;

        //when
        Executable executable = () -> noteService.removeNotes(noteId, catalogId);

        //then
        assertThrows(NotesNotExistException.class, executable);
    }

    @Test
    void removeNote_catalogIdNotExists_expectCatalogNotFoundException() {
//        //given
//        final var noteId = 1;
//        final var catalogId = 100;
//
//        //when
//        Executable executable = () -> noteService.removeNote(noteId, catalogId);
//
//        //then
//        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void fetchNote_noteIdExits_expectNote() {
        //given
        final int noteId = 1;

        //when
        var note = noteService.fetchNote(noteId);

        //then
        assertEquals("Some Note Title", note.name());
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
        final int catalogId = 10;
        final int newCatalogId = 15;

        final User user = userDao.selectUserById(1).get();
        //when
        noteService.changeCatalog(new CatalogOrderingRequest(
                noteId,
                catalogId,
                newCatalogId,
                user
        ));

        //then
        var notesFromOldCatalog = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        var notesFromNewCatalog = catalogDao.selectAllNotesByCatalogId(newCatalogId, 0, 10);

        assertEquals(2, notesFromOldCatalog.size());
        assertEquals(1, notesFromNewCatalog.size());
    }

    @Test
    void changeCatalog_catalogNotExists_expectCatalogNotFound() {
//        //given
//        final int noteId = 1;
//        final int catalogId = 22313;
//        final int newCatalogId = 15;
//
//        final User user = userDao.selectUserById(1).get();
//        //when
//        Executable executable = () -> noteService.changeCatalog(new CatalogOrderingRequest(
//                noteId,
//                catalogId,
//                newCatalogId,
//                user
//        ));
//
//        //then
//        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void changeCatalog_noteNotExists_expectNoteNotFound() {
        //given
        final int noteId = 3213;
        final int catalogId = 2;
        final int newCatalogId = 15;

        final User user = userDao.selectUserById(1).get();
        //when
        Executable executable = () -> noteService.changeCatalog(new CatalogOrderingRequest(
                noteId,
                catalogId,
                newCatalogId,
                user
        ));

        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void fetchAllOwnerNote_includingCatalogs_expectThreeNotes() {
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
        assertEquals(5, notes.size());
    }

    @Test
    void fetchAllOwnerNote_withoutCatalogs_expectOneNotes() {
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
        assertEquals(2, notes.size());
    }
}