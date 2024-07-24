package md.brainet.doeves.catalog;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("test@mail.ru")
class CatalogControllerTest extends IntegrationTestBase {

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    UserDao userDao;

    @Autowired
    MockMvc mockMvc;

    @Test
    void fetchAllUserCatalogs() throws Exception {
        mockMvc.perform(
                get("/api/v1/catalog/all?offset=0&limit=10")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$", hasSize(7))
        );
    }

    @Test
    void fetchNotesFromConcreteCatalog_firstCatalog_expectThreeNotes() throws Exception {
        mockMvc.perform(
                get("/api/v1/catalog/10/notes?offset=0&limit=10")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$", hasSize(3))
        );
    }

    @Test
    void fetchNotesFromConcreteCatalog_rootCatalogIdWithoutCatalogs_expectTwo() throws Exception {
        mockMvc.perform(
                get("/api/v1/catalog/notes?offset=0&limit=10")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$", hasSize(2))
        );
    }

    @Test
    void fetchNotesFromConcreteCatalog_rootCatalogIdIncludingCatalogs_expectTwo() throws Exception {
        mockMvc.perform(
                get("/api/v1/catalog/notes?offset=0&limit=10&including-catalogs=true")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$", hasSize(5))
        );
    }

    @Test
    void postCatalog() throws Exception {
        var title = "Valera";
        var json = """
                {
                    "name" : "%s"
                }
                """.formatted(title);

        mockMvc.perform(
                post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpectAll(
                status().isCreated(),
                jsonPath("$.id").exists(),
                jsonPath("$.name").value(title)
        );
    }

    @Test
    void changeCatalogOrder_toStart() throws Exception {
        final int catalogId = 15;

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/order-after"
                                .formatted(catalogId)
                )
        ).andExpectAll(
                status().isAccepted()
        );

        var catalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);

        assertEquals(catalogId, catalogs.get(0).id());
    }

    @Test
    void changeCatalogOrder_toNotExistsCatalog() throws Exception {
        final int catalogId = 1532;

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/order-after"
                                .formatted(catalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeCatalogOrder_toAnotherCatalogThatNotOwned() throws Exception {
        final int catalogId = userDao.selectUserById(2)
                .get()
                .getRootCatalogId();

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/order-after"
                                .formatted(catalogId)
                )
        ).andExpectAll(
                status().isForbidden()
        );

        var catalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);

        assertNotEquals(catalogId, catalogs.get(0).id());
    }

    @Test
    void changeName_catalogExists() throws Exception {
        var title = "Valera";
        var catalogId = 10;

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/name?val=%s"
                                .formatted(catalogId, title)
                )
        ).andExpectAll(
                status().isAccepted()
        );

        var catalog = catalogDao.selectCatalogById(catalogId).get();
        assertEquals(title, catalog.name());
    }

    @Test
    void changeName_catalogNotExists() throws Exception {
        var title = "Valera";
        var catalogId = 13210;

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/name?val=%s"
                                .formatted(catalogId, title)
                )
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void changeName_catalogNotOwned() throws Exception {
        var rootCatalogIdAnotherUser = userDao.selectUserById(2)
                .get()
                .getRootCatalogId();

        mockMvc.perform(
                patch(
                        "/api/v1/catalog/%s/name?val=%s"
                                .formatted(rootCatalogIdAnotherUser, "her")
                )
        ).andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void deleteCatalog_catalogExists() throws Exception {
        final int deletingCatalogId = 10;

        mockMvc.perform(
                delete(
                        "/api/v1/catalog/%s"
                                .formatted(deletingCatalogId)
                )
        ).andExpectAll(
                status().isOk()
        );

        var catalog = catalogDao.selectCatalogById(deletingCatalogId);
        assertFalse(catalog.isPresent());

        var userCatalogs =  catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(6, userCatalogs.size());
    }

    @Test
    void deleteCatalog_catalogNotExists() throws Exception {
        final int deletingCatalogId = 10321;

        mockMvc.perform(
                delete(
                        "/api/v1/catalog/%s"
                                .formatted(deletingCatalogId)
                )
        ).andExpectAll(
                status().isNotFound()
        );

        var catalog = catalogDao.selectCatalogById(deletingCatalogId);
        assertFalse(catalog.isPresent());
    }

    @Test
    void deleteCatalog_catalogNotOwned() throws Exception {
        final int deletingCatalogId = userDao
                .selectUserById(2)
                .get()
                .getRootCatalogId();

        mockMvc.perform(
                delete(
                        "/api/v1/catalog/%s"
                                .formatted(deletingCatalogId)
                )
        ).andExpectAll(
                status().isForbidden()
        );

        var catalog = catalogDao.selectCatalogById(deletingCatalogId);
        assertTrue(catalog.isPresent());
    }
}