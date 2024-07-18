package md.brainet.doeves.catalog;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class CatalogServiceTest extends IntegrationTestBase {

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    CatalogService catalogService;

    @Test
    void createCatalog_userExits_expectCorrectCommit() {
        //given
        var payload = new CatalogDTO(
                "Admin catalog",
                1
        );

        //when
        var catalog = catalogService.createCatalog(2, payload);

        //then
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(2, 0, 10);
        assertEquals(1, catalogs.size());
        assertEquals(catalog.id(), catalogs.get(0).id());
    }

    @Test
    void createCatalog_orderNumberIsNotPresent_expectAssigningOrderNumberZero() {
        //given
        var payload = new CatalogDTO(
                "Admin catalog",
                null
        );

        //when
        var catalog = catalogService.createCatalog(1, payload);

        //then
        assertEquals(0, catalog.orderNumber());
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals("Admin catalog", catalogs.get(0).name());
        assertEquals(0, catalogs.get(0).orderNumber());
        assertEquals(1, catalogs.get(1).orderNumber());
        assertEquals(2, catalogs.get(2).orderNumber());
    }

    @Test
    void createCatalog_userNotExits_expectUserNotFoundException() {
        //given
        var payload = new CatalogDTO(
                "Admin catalog",
                1
        );

        //when
        Executable catalog = () -> catalogService.createCatalog(223, payload);

        //then
        assertThrows(UserNotFoundException.class, catalog);
    }

    @Test
    void findCatalog_idExists_expectCatalog() {
        //given
        final int catalogId = 1;
        final int expectedOwnerId = 1;
        final int expectedOrderNumber = 1;

        //when
        var catalog = catalogService.findCatalog(catalogId);

        //then
        assertEquals(expectedOwnerId, catalog.ownerId());
        assertEquals(expectedOrderNumber, catalog.orderNumber());
    }

    @Test
    void findCatalog_idNotExists_expectCatalogNotFoundException() {
        //given
        final int catalogId = 1;

        //when
        Executable catalog = () -> catalogService.findCatalog(catalogId);

        //then
        assertThrows(CatalogNotFoundException.class, catalog);
    }

    @Test
    void fetchAllOwnerCatalogs_ownerHaveTwoCatalogs() {
        //given
        final int ownerId = 1;

        //when
        var catalogs = catalogService.fetchAllOwnerCatalogs(ownerId, 0, 10);

        //then
        assertEquals(2, catalogs.size());
    }

    @Test
    void fetchAllOwnerCatalogs_ownerHaveNotCatalogs() {
        //given
        final int ownerId = 2;

        //when
        var catalogs = catalogService.fetchAllOwnerCatalogs(ownerId, 0, 10);

        //then
        assertEquals(0, catalogs.size());
    }

    @Test
    void changeOrderNumber_catalogIdExists_expectNewOrderNumber() {
        //given
        final int catalogId = 1;
        final int newOrderNumber = 10;

        //when
        catalogService.rewriteLinkAsPrevCatalogIdFor(catalogId, newOrderNumber);

        //then
        var catalog = catalogDao.selectCatalogById(catalogId);
        assertEquals(newOrderNumber, catalog.get().orderNumber());
    }

    @Test
    void changeOrderNumber_catalogIdNotExists_expectCatalogNotFoundException() {
        //given
        final int catalogId = 1231;
        final int newOrderNumber = 10;

        //when
        Executable executable = () -> catalogService.rewriteLinkAsPrevCatalogIdFor(catalogId, newOrderNumber);

        //then
        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void removeCatalog_catalogIdExists_expectRemoved() {
        //given
        final int catalogId = 1;

        //when
        catalogService.removeCatalog(catalogId);

        //then
        var catalog = catalogDao.selectCatalogById(catalogId);
        assertFalse(catalog.isPresent());
    }

    @Test
    void removeCatalog_catalogIdNotExists_expectCatalogNotFoundException() {
        //given
        final int catalogId = 231;

        //when
        Executable executable = () -> catalogService.removeCatalog(catalogId);

        //then
        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void fetchAllCatalogNotes_catalogIdExists_expectTwoNotes() {
        //given
        final int catalogId = 1;

        //when
        var catalogs = catalogService.fetchAllCatalogNotes(catalogId, 0, 10);

        //then
        assertEquals(2, catalogs.size());
    }

    @Test
    void fetchAllCatalogNotes_catalogIdNotExists_expectZeroNotes() {
        //given
        final int catalogId = 31231;

        //when
        var catalogs = catalogService.fetchAllCatalogNotes(catalogId, 0, 10);

        //then
        assertEquals(0, catalogs.size());
    }
}