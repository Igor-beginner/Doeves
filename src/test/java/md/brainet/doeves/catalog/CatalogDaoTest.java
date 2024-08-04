package md.brainet.doeves.catalog;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CatalogDaoTest extends IntegrationTestBase {

    @Autowired
    JdbcCatalogDao catalogDao;

    @Test
    void selectCatalogById_idExists_expectCatalogPresent() {
        //given
        final int catalogId = 10;

        //when
        var catalog = catalogDao.selectCatalogById(catalogId);

        //then
        assertTrue(catalog.isPresent());
    }

    @Test
    void selectCatalogById_idNotExists_expectCatalogNotePresent() {
        //given
        final int catalogId = 32324332;

        //when
        var catalog = catalogDao.selectCatalogById(catalogId);

        //then
        assertTrue(catalog.isEmpty());
    }

    @Test
    void insertCatalog() {
        //given
        final int expectedId = 17;

        var catalog = new CatalogDTO();
        catalog.setName("My First Catalog");
        var firstCatalogId = catalogDao.selectFirstCatalogIdByOwnerId(1);
        var firstCatalog = catalogDao.selectCatalogById(firstCatalogId).get();

        //when
        var actualId = catalogDao.insertCatalog(1, catalog).id();

        //then
        assertNull(firstCatalog.prevCatalogId());
        firstCatalog = catalogDao.selectCatalogById(firstCatalogId).get();
        assertEquals(expectedId, firstCatalog.prevCatalogId());
        assertEquals(expectedId, actualId);
    }

    @Test
    void selectAllCatalogsByOwnerId_userHave7Catalogs() {
        //given
        final int ownerId = 1;
        final int expectedCountOfData = 7;

        //when
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(ownerId,0,10);

        //then
        assertEquals(expectedCountOfData, catalogs.size());
    }

    @Test
    void selectAllCatalogsByOwnerId_userHave0Catalogs() {
        //given
        final int ownerId = 2;

        //when
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(ownerId,0,10);

        //then
        assertTrue(catalogs.isEmpty());
    }

    @Test
    void updateOrderNumberByCatalogId_changingCatalogsNotOnEdges() {
        //given
        final int prevCatalogId = 15;
        final Integer nextReplacingCatalogId = 16;

        final Integer prevInsteadCatalogId = 10;
        final int currentCatalogId = 11;
        final int nextInsteadCatalogId = 12;

        //when
        catalogDao.updateOrderNumberByCatalogId(prevCatalogId, currentCatalogId);

        //then
        var insteadCatalog = catalogDao.selectCatalogById(currentCatalogId);
        assertEquals(prevCatalogId, insteadCatalog.get().prevCatalogId());

        var nextCurrentCatalog = catalogDao.selectCatalogById(nextInsteadCatalogId);
        assertEquals(prevInsteadCatalogId, nextCurrentCatalog.get().prevCatalogId());

        var nextPrevCatalog = catalogDao.selectCatalogById(nextReplacingCatalogId);
        assertEquals(currentCatalogId, nextPrevCatalog.get().prevCatalogId());

        var allCatalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(7, allCatalogs.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromMiddleToStart() {
        //given
        final Integer prevCatalogId = null;
        final Integer nextReplacingCatalogId = 10;

        final Integer prevInsteadCatalogId = 13;
        final int currentCatalogId = 14;
        final int nextInsteadCatalogId = 15;

        //when
        catalogDao.updateOrderNumberByCatalogId(prevCatalogId, currentCatalogId);

        //then
        var insteadCatalog = catalogDao.selectCatalogById(currentCatalogId);
        assertEquals(prevCatalogId, insteadCatalog.get().prevCatalogId());

        var nextCurrentCatalog = catalogDao.selectCatalogById(nextInsteadCatalogId);
        assertEquals(prevInsteadCatalogId, nextCurrentCatalog.get().prevCatalogId());

        var nextPrevCatalog = catalogDao.selectCatalogById(nextReplacingCatalogId);
        assertEquals(currentCatalogId, nextPrevCatalog.get().prevCatalogId());

        var allCatalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(7, allCatalogs.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromEndToStart() {
        //given
        final Integer prevCatalogId = null;
        final Integer nextReplacingCatalogId = 10;

        final Integer prevInsteadCatalogId = 15;
        final int currentCatalogId = 16;

        //when
        catalogDao.updateOrderNumberByCatalogId(prevCatalogId, currentCatalogId);

        //then
        var insteadCatalog = catalogDao.selectCatalogById(currentCatalogId);
        assertEquals(prevCatalogId, insteadCatalog.get().prevCatalogId());

        var nextPrevCatalog = catalogDao.selectCatalogById(nextReplacingCatalogId);
        assertEquals(currentCatalogId, nextPrevCatalog.get().prevCatalogId());

        var allCatalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(7, allCatalogs.size());
    }

    @Test
    void updateOrderNumberByCatalogId_fromStartToEnd() {
        //given
        final Integer prevCatalogId = 16;
        final Integer nextReplacingCatalogId = null;

        final Integer prevInsteadCatalogId = null;
        final int currentCatalogId = 10;
        final int nextCurrentCatalogId = 11;

        //when
        catalogDao.updateOrderNumberByCatalogId(prevCatalogId, currentCatalogId);

        //then
        var insteadCatalog = catalogDao.selectCatalogById(currentCatalogId);
        assertEquals(prevCatalogId, insteadCatalog.get().prevCatalogId());

        var nextCurrentCatalog = catalogDao.selectCatalogById(nextCurrentCatalogId);
        assertNull(nextCurrentCatalog.get().prevCatalogId());

        var allCatalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(7, allCatalogs.size());
    }

    @Test
    void updateOrderNumberByCatalogId_idNotExists() {
        //given
        final Integer prevCatalogId = 13216;
        final int currentCatalogId = 132130;

        //when
        catalogDao.updateOrderNumberByCatalogId(prevCatalogId, currentCatalogId);

        //then
        var allCatalogs = catalogDao.selectAllCatalogsByOwnerId(1, 0, 10);
        assertEquals(7, allCatalogs.size());
    }

    @Test
    void updateNameByCatalogId_idExists_expectTrue() {
        //when
        final int catalogId = 10;
        final String newName = "New Task Title";

        //when
        boolean updated = catalogDao.updateNameByCatalogId(catalogId, newName);

        //then
        assertTrue(updated);
        var catalog = catalogDao.selectCatalogById(catalogId);
        assertTrue(catalog.isPresent());
        assertEquals(newName, catalog.get().name());

    }

    @Test
    void updateNameByCatalogId_idNotExists_expectFalse() {
        //when
        final int catalogId = 131232;
        final String newName = "New Task Title";

        //when
        var updated = catalogDao.updateNameByCatalogId(catalogId, newName);

        //then
        assertFalse(updated);
    }

    @Test
    void removeByCatalogId_firstOwnerCatalogId_expectTrue() {
        //given
        final int catalogId = 10;


        //when
        boolean removed = catalogDao.removeByCatalogId(catalogId);

        //then
        assertTrue(removed);
    }

    @Test
    void removeByCatalogId_middleCatalogId_checkRewritingLinks() {
        //given
        final int catalogId = 15;
        final int prevCatalogId = catalogDao.findPrevCatalogIdByCatalogId(catalogId);
        final int nextCatalogId = catalogDao.findNextIdFor(catalogId);

        //when
        boolean removed = catalogDao.removeByCatalogId(catalogId);

        //then
        var nextAfterDeletedCatalogDao = catalogDao.selectCatalogById(nextCatalogId);
        assertTrue(removed);
        assertEquals(prevCatalogId, (int) nextAfterDeletedCatalogDao.get().prevCatalogId());
        assertTrue(catalogDao.selectCatalogById(catalogId).isEmpty());
    }

    @Test
    void removeByCatalogId_idNotExists_expectFalse() {
        //given
        final int catalogId = 3123123;


        //when
        boolean removed = catalogDao.removeByCatalogId(catalogId);

        //then
        assertFalse(removed);
    }


    @Test
    void selectAllNotesByCatalogId_idExists_expectTrue() {
        //given
        final int catalogId = 10;
        final int expectedNotesCount = 3;


        //when
        var catalogs = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);

        //then
        assertEquals(expectedNotesCount, catalogs.size());
    }

    @Test
    void selectAllNotesByCatalogId_idNotExists_expectFalse() {
        //given
        final int catalogId = 23;
        final int expectedNotesCount = 0;


        //when
        var catalogs = catalogDao.selectAllNotesByCatalogId(catalogId, 0, 10);

        //then
        assertEquals(expectedNotesCount, catalogs.size());
    }
}