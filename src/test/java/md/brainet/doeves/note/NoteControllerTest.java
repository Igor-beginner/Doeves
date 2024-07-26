package md.brainet.doeves.note;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.catalog.CatalogDao;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("test@mail.ru")
class NoteControllerTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NoteDao noteDao;

    @Autowired
    UserDao userDao;

    @Autowired
    CatalogDao catalogDao;

    @Test
    void fetchConcreteNote_idExists() throws Exception {
        final int noteId = 2;

        mockMvc.perform(
                get(
                        "/api/v1/note/%s"
                                .formatted(noteId)
                )
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(noteId)
        );
    }

    @Test
    void fetchConcreteNote_idNotExists() throws Exception {
        final int noteId = 23;

        mockMvc.perform(
                get(
                        "/api/v1/note/%s"
                                .formatted(noteId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void fetchConcreteNote_notOwned() throws Exception {
        final var user = userDao.selectUserById(2).get();
        final int noteId = noteDao.insertNote(user, new NoteDTO()).get().id();

        mockMvc.perform(
                get(
                        "/api/v1/note/%s"
                                .formatted(noteId)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void fetchAllOwnerNote_includingCatalogs() throws Exception {

        mockMvc.perform(
                get(
                        "/api/v1/note/all?including-catalogs=true"
                )
        ).andExpectAll(
                status().isOk(),
                jsonPath("$", hasSize(5))
        );
    }

    @Test
    void fetchAllOwnerNote_withoutCatalogs() throws Exception {

        mockMvc.perform(
                get(
                        "/api/v1/note/all"
                )
        ).andExpectAll(
                status().isOk(),
                jsonPath("$", hasSize(2))
        );
    }

    @Test
    void postNote() throws Exception {
        var notesBefore = noteDao.selectAllNotesByOwnerIdWithoutCatalogs(1, 0, 10);

        mockMvc.perform(
                post(
                        "/api/v1/note"
                ).contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpectAll(
                status().isCreated()
        );

        var notesAfter = noteDao.selectAllNotesByOwnerIdWithoutCatalogs(1, 0, 10);

        assertEquals(notesBefore.size() + 1, notesAfter.size());
    }

    @Test
    void changeName_noteExists() throws Exception {
        var title = "Valera";
        var noteId = 2;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/name?val=%s"
                                .formatted(noteId, title)
                )
        ).andExpectAll(
                status().isAccepted()
        );

        var note = noteDao.selectByNoteId(noteId).get();
        assertEquals(title, note.name());
    }

    @Test
    void changeName_noteNotExists() throws Exception {
        var title = "Valera";
        var noteId = 13210;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/name?val=%s"
                                .formatted(noteId, title)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeName_noteNotOwned() throws Exception {
        var user = userDao.selectUserById(2);
        var insertingNote = new NoteDTO();
        var note = noteDao.insertNote(
                user.get(),
                insertingNote
        ).get();

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/name?val=%s"
                                .formatted(note.id(), "her")
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void changeDescription_noteExists() throws Exception {
        var title = "Valera";
        var noteId = 1;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/description?val=%s"
                                .formatted(noteId, title)
                )
        ).andExpectAll(
                status().isAccepted()
        );


    }

    @Test
    void changeDescription_noteNotExists() throws Exception {
        var title = "Valera";
        var noteId = 132130;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/description?val=%s"
                                .formatted(noteId, title)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeDescription_noteNotOwned() throws Exception {
        var title = "Valera";


        var user = userDao.selectUserById(2).get();
        Note note = noteDao.insertNote(user, new NoteDTO()).get();
        var noteId = note.id();

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/description?val=%s"
                                .formatted(noteId, title)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void changeCatalog_idsAreExist() throws Exception {
        final int noteId = 2;
        final int catalogIdFrom = 10;
        final int catalogIdTo = 14;

        var catalogUntilUpdateFrom = catalogDao.selectAllNotesByCatalogId(catalogIdFrom, 0, 10);
        var catalogUntilUpdateTo = catalogDao.selectAllNotesByCatalogId(catalogIdTo, 0, 10);

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isAccepted()
        );

        var catalogAfterUpdateFrom = catalogDao.selectAllNotesByCatalogId(catalogIdFrom, 0, 10);
        var catalogAfterUpdateTo = catalogDao.selectAllNotesByCatalogId(catalogIdTo, 0, 10);

        assertEquals(catalogUntilUpdateFrom.size() - 1, catalogAfterUpdateFrom.size());
        assertEquals(catalogUntilUpdateTo.size() + 1, catalogAfterUpdateTo.size());
    }

    @Test
    void changeCatalog_noteIdNotExists() throws Exception {
        final int noteId = 232;
        final int catalogIdFrom = 10;
        final int catalogIdTo = 14;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeCatalog_catalogIdFromNotExists() throws Exception {
        final int noteId = 2;
        final int catalogIdFrom = 12340;
        final int catalogIdTo = 14;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeCatalog_noteIsNotInRequestedCatalog() throws Exception {
        final int noteId = 2;
        final int catalogIdFrom = 15;
        final int catalogIdTo = 14;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeCatalog_catalogIdToNotExists() throws Exception {
        final int noteId = 2;
        final int catalogIdFrom = 10;
        final int catalogIdTo = 1244;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeCatalog_tryToStealNote() throws Exception {
        final var user = userDao.selectUserById(2).get();
        final int noteId = noteDao.insertNote(user, new NoteDTO()).get().id();
        final int catalogIdFrom = user.getRootCatalogId();
        final int catalogIdTo = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/from/%s/to/%s"
                                .formatted(noteId, catalogIdFrom, catalogIdTo)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void changeNoteOrder_idAreExists() throws Exception {
        final int currentNoteId = 2;
        final int noteAfterId = 3;
        final int catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isAccepted()
        );

        var notes = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(3, notes.size());
        assertEquals(currentNoteId, notes.get(1).id());
    }

    @Test
    void changeNoteOrder_catalogNotExists() throws Exception {
        final int currentNoteId = 2;
        final int noteAfterId = 3;
        final int catalogId = 1032;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeNoteOrder_catalogNotOwned() throws Exception {
        final int currentNoteId = 2;
        final int noteAfterId = 3;
        final int catalogId = userDao.selectUserById(2).get().getRootCatalogId();

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void changeNoteOrder_currentNoteIdNotExists() throws Exception {
        final int currentNoteId = 23223;
        final int noteAfterId = 3;
        final int catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeNoteOrder_noteAfterIdNoteExists() throws Exception {
        final int currentNoteId = 2;
        final int noteAfterId = 32;
        final int catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeNoteOrder_currentNoteIdNotOwned() throws Exception {
        var user = userDao.selectUserById(2).get();
        final int currentNoteId = noteDao.insertNote(user, new NoteDTO()).get().id();
        final int noteAfterId = 1;
        final int catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void changeNoteOrder_currentNoteAfterIdNotOwned() throws Exception {
        var user = userDao.selectUserById(2).get();
        final int currentNoteId = 2;
        final int noteAfterId = noteDao.insertNote(user, new NoteDTO()).get().id();
        final int catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
                                .formatted(currentNoteId, noteAfterId, catalogId)
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }
//    @Test
//    void changeNoteOrder_currentNoteAfterIdNotOwned() throws Exception {
//        var user = userDao.selectUserById(2).get();
//        final int currentNoteId = 2;
//        final int noteAfterId = noteDao.insertNote(user, new NoteDTO()).get().id();
//        final int catalogId = 10;
//
//        mockMvc.perform(
//                patch(
//                        "/api/v1/note/%s/order-after?prev-note-id=%s&catalog-id=%s"
//                                .formatted(currentNoteId, noteAfterId, catalogId)
//                )
//        ).andExpectAll(
//                status().isForbidden()
//        );
//    }


    @Test
    void deleteNote_noteExists() throws Exception {
        final int noteId = 2;
        final int catalogId = 10;

        var catalogBefore = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s/catalog?id=%s"
                                .formatted(noteId, catalogId)
                )
        ).andExpectAll(
                status().isOk()
        );

        var catalogAfter = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(catalogBefore.size() - 1, catalogAfter.size());
    }

    @Test
    void deleteNote_noteExistsButNotInSpecifiedCatalog() throws Exception {
        final int noteId = 2;
        final int catalogId = 15;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s/catalog?id=%s"
                                .formatted(noteId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void deleteNote_noteNotExists() throws Exception {
        final int noteId = 23123;
        final int catalogId = 15;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s/catalog?id=%s"
                                .formatted(noteId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void deleteNote_catalogNotExists() throws Exception {
        final int noteId = 2;
        final int catalogId = 13213;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s/catalog?id=%s"
                                .formatted(noteId, catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void deleteNote_severalNotes() throws Exception {
        final int[] notesId = {1, 2, 3};
        final int catalogId = 10;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s,%s,%s/catalog?id=%s"
                                .formatted(
                                        notesId[0],
                                        notesId[2],
                                        notesId[1],
                                        catalogId
                                )
                )
        ).andExpectAll(
                status().isOk()
        );

        var catalogAfter = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertTrue(catalogAfter.isEmpty());
    }

    @Test
    void deleteNote_oneOfNotesNotExists() throws Exception {
        final int[] notesId = {1, 232, 3};
        final int catalogId = 10;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s,%s,%s/catalog?id=%s"
                                .formatted(
                                        notesId[0],
                                        notesId[2],
                                        notesId[1],
                                        catalogId
                                )
                )
        ).andExpectAll(
                status().isNotFound()
        );

        var catalogAfter = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(3, catalogAfter.size());
    }

    @Test
    void deleteNote_oneOfNotesNotOwned() throws Exception {
        final var user = userDao.selectUserById(2).get();
        final int[] notesId = {1, noteDao.insertNote(user, new NoteDTO()).get().id(), 3};
        final int catalogId = 10;

        mockMvc.perform(
                delete(
                        "/api/v1/note/%s,%s,%s/catalog?id=%s"
                                .formatted(
                                        notesId[0],
                                        notesId[2],
                                        notesId[1],
                                        catalogId
                                )
                )
        ).andExpectAll(
                status().isForbidden()
        );

        var catalogAfter = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);
        assertEquals(3, catalogAfter.size());
    }
}