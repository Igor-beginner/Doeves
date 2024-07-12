package md.brainet.doeves.catalog;

import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.note.Note;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final CatalogDao catalogDao;

    public CatalogServiceImpl(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    @Override
    public Catalog createCatalog(Integer ownerId, CatalogDTO catalogDTO) {
        if(Objects.isNull(catalogDTO.orderNumber())) {
            catalogDTO = new CatalogDTO(
                    catalogDTO.name(),
                    0
            );
        }

        return catalogDao.insertCatalog(ownerId, catalogDTO);
    }

    @Override
    public Catalog findCatalog(Integer catalogId) {
        return catalogDao.selectCatalogById(catalogId)
                .orElseThrow(() -> new CatalogNotFoundException(catalogId));
    }

    @Override
    public List<Catalog> fetchAllOwnerCatalogs(Integer ownerId, Integer offset, Integer limit) {
        return catalogDao.selectAllCatalogsByOwnerId(ownerId, offset, limit);
    }

    @Override
    public Integer changeOrderNumber(Integer editingCatalogId, Integer backCatalogId) {
        //todo fetch two catalogs
        var editingCatalog = findCatalog(editingCatalogId);
        var backCatalogOrderNumber = backCatalogId == null
                ? 0
                : findCatalog(backCatalogId).orderNumber();

        boolean updated = catalogDao.updateOrderNumberByCatalogId(
                new CatalogOrderingRequest(
                        editingCatalogId,
                        editingCatalog.orderNumber(),
                        backCatalogOrderNumber,
                        editingCatalog.ownerId()
                )
        );
        if(!updated) {
            //todo throw nothingToUpdate
        }
        return backCatalogOrderNumber;
    }

    @Override
    public void removeCatalog(Integer catalogId) {
        boolean removed = catalogDao.removeByCatalogId(catalogId);
        if(!removed) {
            throw new CatalogNotFoundException(catalogId);
        }
    }

    @Override
    public void changeName(Integer catalogId, String newName) {
        boolean updated = catalogDao.updateNameByCatalogId(catalogId, newName);
        if(!updated) {
            throw new CatalogNotFoundException(catalogId);
        }
    }

    @Override
    public List<Note> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit) {
        return catalogDao.selectAllNotesByCatalogId(catalogId, offset, limit);
    }
}
