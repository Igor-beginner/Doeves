package md.brainet.doeves.catalog;

import md.brainet.doeves.exception.CatalogNotFoundException;
import md.brainet.doeves.exception.CatalogsNotExistException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.general.EntityWithinContextLinkedListService;
import md.brainet.doeves.note.NotePreview;
import md.brainet.doeves.note.NoteServiceImpl;
import md.brainet.doeves.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogServiceImpl extends EntityWithinContextLinkedListService<CatalogDTO> implements CatalogService {

    private final CatalogDao catalogDao;
    private final NoteServiceImpl noteService;

    public CatalogServiceImpl(CatalogDao catalogDao, NoteServiceImpl noteService) {
        super(catalogDao);
        this.catalogDao = catalogDao;
        this.noteService = noteService;
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
    @Transactional
    public void removeCatalogs(List<Integer> catalogsId) {
        List<Integer> notRemovedCatalogsId = new ArrayList<>();

        catalogsId.forEach(id -> {
            boolean updated = catalogDao.removeByCatalogId(id);
            if(!updated) {
                notRemovedCatalogsId.add(id);
            }
        });


        if(!notRemovedCatalogsId.isEmpty()) {
            throw new CatalogsNotExistException(catalogsId);
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
