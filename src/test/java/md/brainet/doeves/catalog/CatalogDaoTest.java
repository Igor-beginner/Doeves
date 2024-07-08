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
    CatalogDao catalogDao;

    @Test
    void selectCatalogById_idExists_expectCatalogPresent() {
        //given
        final int catalogId = 1;

        //when
        var catalog = catalogDao.selectCatalogById(catalogId);

        //then
        assertTrue(catalog.isPresent());
    }

    @Test
    void selectCatalogById_idNotExists_expectCatalogNotePresent() {
        //given
        final int catalogId = 12;

        //when
        var catalog = catalogDao.selectCatalogById(catalogId);

        //then
        assertTrue(catalog.isEmpty());
    }

    @Test
    void insertCatalog() {
        //given
        final int expectedId = 3;

        var catalog = new CatalogDTO(
                "My First Catalog",
                0
        );

        //when
        var actualId = catalogDao.insertCatalog(1, catalog).id();

        //then
        assertEquals(expectedId, actualId);
    }

    @Test
    void selectAllCatalogsByOwnerId() {
        //given
        final int ownerId = 1;
        final int expectedCountOfData = 2;

        //when
        var catalogs = catalogDao.selectAllCatalogsByOwnerId(ownerId,0,10);

        //then
        assertEquals(expectedCountOfData, catalogs.size());
    }

    @Test
    void updateOrderNumberByCatalogId_numberIsNotBusy_expectTrue() {
        //given
        final int baseCatalogId = 1;
        final int newOrderNum = 3;

        //when
        var updated = catalogDao.updateOrderNumberByCatalogId(baseCatalogId, newOrderNum);

        //then
        assertTrue(updated);
        var catalog = catalogDao.selectCatalogById(baseCatalogId);
        assertEquals(baseCatalogId, catalog.get().id());
    }

    @Test
    void updateOrderNumberByCatalogId_numberIsBusy_expectFalse() {
        //given
        final int baseCatalogId = 1;
        final int newOrderNum = 0;

        //when
        var updated = catalogDao.updateOrderNumberByCatalogId(baseCatalogId, newOrderNum);

        //then
        assertTrue(updated);
        var catalog = catalogDao.selectCatalogById(2);
        assertEquals(newOrderNum + 1, catalog.get().orderNumber());
        catalog = catalogDao.selectCatalogById(baseCatalogId);
        assertEquals(newOrderNum, catalog.get().orderNumber());
    }

    @Test
    void updateNameByCatalogId_idExists_expectTrue() {
        //when
        final int catalogId = 1;
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
        final int catalogId = 12;
        final String newName = "New Task Title";

        //when
        boolean updated = catalogDao.updateNameByCatalogId(catalogId, newName);

        //then
        assertFalse(updated);
    }

    @Test
    void removeByCatalogId_idExists_expectTrue() {
        //given
        final int catalogId = 1;


        //when
        boolean removed = catalogDao.removeByCatalogId(catalogId);

        //then
        assertTrue(removed);
    }

    @Test
    void removeByCatalogId_idNotExists_expectFalse() {
        //given
        final int catalogId = 10;


        //when
        boolean removed = catalogDao.removeByCatalogId(catalogId);

        //then
        assertFalse(removed);
    }


    @Test
    void selectAllNotesByCatalogId_idExists_expectTrue() {
        //given
        final int catalogId = 2;
        final int expectedNotesCount = 2;


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