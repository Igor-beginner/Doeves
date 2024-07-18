package md.brainet.doeves.catalog;

import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.note.NotePreview;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final CatalogDao catalogDao;

    public CatalogServiceImpl(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    @Override
    public Catalog createCatalog(Integer ownerId, CatalogDTO catalogDTO) {
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
    public void rewriteLinkAsPrevCatalogIdFor(Integer editingCatalogId, Integer backCatalogId) {
        catalogDao.updateOrderNumberByCatalogId(
                backCatalogId, editingCatalogId
        );
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
    public List<NotePreview> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit) {
        return catalogDao.selectAllNotesByCatalogId(catalogId, offset, limit);
    }
}
