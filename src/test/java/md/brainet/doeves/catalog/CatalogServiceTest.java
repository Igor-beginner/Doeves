package md.brainet.doeves.catalog;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.exception.CatalogsNotExistException;
import md.brainet.doeves.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
        var payload = new CatalogDTO("Admin catalog");

        //when
        var catalog = catalogService.createCatalog(2, payload);

        //then
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(2, 0, 10);
        assertEquals(1, catalogs.size());
        assertEquals(catalog.id(), catalogs.get(0).id());
    }

    @Test
    void createCatalog_userNotExits_expectUserNotFoundException() {
        //given
        var payload = new CatalogDTO("Admin catalog");

        //when
        Executable catalog = () -> catalogService.createCatalog(223, payload);

        //then
        assertThrows(UserNotFoundException.class, catalog);
    }

    @Test
    void findCatalog_idExists_expectCatalog() {
        //given
        final int catalogId = 10;
        final int expectedOwnerId = 1;

        //when
        var catalog = catalogService.findCatalog(catalogId);

        //then
        assertEquals(expectedOwnerId, catalog.ownerId());
    }

    @Test
    void findCatalog_idNotExists_expectCatalogNotFoundException() {
        //given
        final int catalogId = 10323;

        //when
        Executable executable = () -> catalogService.findCatalog(catalogId);

        //then
        assertThrows(CatalogNotFoundException.class, executable);
    }

    @Test
    void fetchAllOwnerCatalogs_ownerHaveTwoCatalogs() {
        //given
        final int ownerId = 1;

        //when
        var catalogs = catalogService.fetchAllOwnerCatalogs(ownerId, 0, 10);

        //then
        assertEquals(7, catalogs.size());
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
        final Integer catalogId = 15;
        final Integer newCatalogId = null;

        //when
        catalogService.rewriteLinkAsPrevCatalogIdFor(catalogId, newCatalogId);

        //then
        var catalog = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(catalogId, catalog.get(0).id());
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
        final int catalogId = 10;

        //when
        catalogService.removeCatalogs(List.of(catalogId));

        //then
        var catalog = catalogDao.selectCatalogById(catalogId);
        assertFalse(catalog.isPresent());
    }

    @Test
    void removeCatalog_catalogIdNotExists_expectCatalogNotFoundException() {
        //given
        final int catalogId = 231;

        //when
        Executable executable = () -> catalogService.removeCatalogs(List.of(catalogId));

        //then
        assertThrows(CatalogsNotExistException.class, executable);
    }

    @Test
    void removeCatalog_severalCatalogs_expectDeleted() {
        //given
        final List<Integer> catalogsId = List.of(10, 11, 15);

        catalogsId.forEach(id ->
                assertTrue(catalogDao.selectCatalogById(id).isPresent())
        );

        //when
        catalogService.removeCatalogs(catalogsId);

        //then
        catalogsId.forEach(id ->
                assertFalse(catalogDao.selectCatalogById(id).isPresent())
        );
    }

    @Test
    void removeCatalog_severalCatalogsOneOfThemIsNotExists_expectDeleted() {
        //given
        final List<Integer> catalogsId = List.of(10, 141, 15);
        var existingCatalogs = List.of(10, 15);
        existingCatalogs.forEach(id ->
                assertTrue(catalogDao.selectCatalogById(id).isPresent())
        );

        //when
        Executable executable = () -> catalogService.removeCatalogs(catalogsId);

        //then
        assertThrows(CatalogsNotExistException.class, executable);
        existingCatalogs.forEach(id ->
                assertTrue(catalogDao.selectCatalogById(id).isPresent())
        );
    }

    @Test
    void fetchAllCatalogNotes_catalogIdExists_expectThreeNotes() {
        //given
        final int catalogId = 10;

        //when
        var catalogs = catalogService.fetchAllCatalogNotes(catalogId, 0, 10);

        //then
        assertEquals(3, catalogs.size());
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