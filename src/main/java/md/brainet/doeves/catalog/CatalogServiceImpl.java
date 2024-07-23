package md.brainet.doeves.catalog;

import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.note.NotePreview;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            return catalogDao.insertCatalog(ownerId, catalogDTO);
        } catch (DataIntegrityViolationException e) {
            throw new UserNotFoundException(ownerId, e.getCause());
        }
    }

    @Override
    public Catalog findCatalog(Integer catalogId) {
        return catalogDao.selectCatalogById(catalogId)
                .orElseThrow(() -> new CatalogNotFoundException(catalogId));
    }

    @Override
    public List<Catalog> fetchAllOwnerCatalogs(Integer ownerId, Integer offset, Integer limit) {
        try {
            return catalogDao.selectAllCatalogsByOwnerId(ownerId, offset, limit);
        } catch (DataIntegrityViolationException e) {
            throw new UserNotFoundException(ownerId);
        }
    }

    @Override
    public void rewriteLinkAsPrevCatalogIdFor(Integer editingCatalogId, Integer backCatalogId) {
        try {
            catalogDao.updateOrderNumberByCatalogId(backCatalogId, editingCatalogId);
        } catch (DataIntegrityViolationException e) {
            throw new CatalogNotFoundException(editingCatalogId);
        }
    }

    @Override
    public void removeCatalog(Integer catalogId) {
        boolean updated = catalogDao.removeByCatalogId(catalogId);
        if(!updated) {
            throw new CatalogNotFoundException(catalogId);
        }
    }

    @Override
    public void changeName(Integer catalogId, String newName) {
        try {
            catalogDao.updateNameByCatalogId(catalogId, newName);
        } catch (DataIntegrityViolationException e) {
            throw new CatalogNotFoundException(catalogId);
        }
    }

    @Override
    public List<NotePreview> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit) {
        try {
            return catalogDao.selectAllNotesByCatalogId(catalogId, offset, limit);
        } catch (DataIntegrityViolationException e) {
            throw new CatalogNotFoundException(catalogId);
        }
    }
}
