package md.brainet.doeves.catalog;


import md.brainet.doeves.note.Note;
import md.brainet.doeves.note.NoteDao;
import md.brainet.doeves.note.NotePreview;
import md.brainet.doeves.note.NoteService;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);
    private final CatalogService catalogService;
    private final CatalogPermissionUtil catalogPermissionUtil;

    public CatalogController(CatalogService catalogService,
                             CatalogPermissionUtil catalogPermissionUtil) {
        this.catalogService = catalogService;
        this.catalogPermissionUtil = catalogPermissionUtil;
    }

    @GetMapping("all")
    public ResponseEntity<?> fetchAllUserCatalogs(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        List<Catalog> catalogs = catalogService.fetchAllOwnerCatalogs(
                user.getId(),
                offset,
                limit
        );
        LOG.info("User [email={}] was requested all catalogs from {} to {}", user.getEmail(), offset, limit);
        return new ResponseEntity<>(catalogs, HttpStatus.OK);
    }

    @GetMapping("{id}/notes")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)")
    public ResponseEntity<?> fetchNotesFromConcreteCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer catalogId,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        List<NotePreview> notes = catalogService.fetchAllCatalogNotes(catalogId, offset, limit);
        LOG.info("Catalog notes [id={}] was requested by user [email={}].", catalogId, user.getEmail());
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> postCatalog(
            CatalogDTO catalogDTO,
            @AuthenticationPrincipal User user) {

        Catalog catalog = catalogService.createCatalog(user.getId(), catalogDTO);
        LOG.info("Catalog [id={}] was created by [email={}]", user.getId(), user.getEmail());
        return new ResponseEntity<>(catalog, HttpStatus.CREATED);
    }

    @PatchMapping("{editingCatalogId}/order-after/{backCatalogId}")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#editingCatalogId, #user.id)" +
            "&& @catalogPermissionUtil.haveEnoughRights(#backCatalogId, #user.id)")
    public ResponseEntity<?> changeCatalogOrder(
            @AuthenticationPrincipal User user,
            @PathVariable("editingCatalogId") Integer editingCatalogId,
            @RequestParam("backCatalogId")Integer backCatalogId
            ) {

        catalogService.rewriteLinkAsPrevCatalogIdFor(editingCatalogId, backCatalogId);
        LOG.info("User [email={}] has set prev_id as {} for catalog_id {}",
                user.getEmail(),
                backCatalogId,
                editingCatalogId
        );
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{id}/name")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)")
    public ResponseEntity<?> changeName(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer catalogId,
            @RequestParam("val") String newName
    ) {

        catalogService.changeName(catalogId, newName);
        LOG.info("User [email={}] changed catalog [id={}] name on '{}'", user.getEmail(), catalogId, newName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @DeleteMapping("{id}")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)")
    public ResponseEntity<?> deleteCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer catalogId
    ) {
        catalogService.removeCatalog(catalogId);
        LOG.info("User [email={}] deleted catalog [id={}]", user.getEmail(), catalogId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
