package md.brainet.doeves.catalog;


import md.brainet.doeves.note.*;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);
    private final CatalogServiceImpl catalogService;
    private final NoteService noteService;
    private final CatalogPermissionUtil catalogPermissionUtil;

    public CatalogController(CatalogServiceImpl catalogService,
                             NoteService noteService,
                             CatalogPermissionUtil catalogPermissionUtil) {
        this.catalogService = catalogService;
        this.noteService = noteService;
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
        LOG.info("Catalog [id={}] was requested by user [email={}].", catalogId, user.getEmail());
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("notes")
    public ResponseEntity<?> fetchNotesFromRootCatalog(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "including-catalogs", defaultValue = "false") Boolean includingCatalogs
    ) {

        List<NotePreview> notes = noteService.fetchAllOwnerNote(
                user.getId(),
                new LimitedListNoteRequest(
                        offset,
                        limit,
                        includingCatalogs
                )
        );
        LOG.info("Root catalog [id={}] was requested by user [email={}].", user.getRootCatalogId(), user.getEmail());
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> postCatalog(
            @RequestBody CatalogDTO catalogDTO,
            @AuthenticationPrincipal User user) {

        catalogDTO.setOwnerId(user.getId());
        Integer catalogId = catalogService.insertEntityIntoContextOnTop(catalogDTO, user.getId());
        LOG.info("Catalog [id={}] was created by [email={}]", catalogId, user.getEmail());
        return new ResponseEntity<>(Map.of("id", catalogId), HttpStatus.CREATED);
    }

    @PatchMapping("{editingCatalogId}/order-after/{backCatalogId}")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#editingCatalogId, #user.id)" +
            "&& @catalogPermissionUtil.haveEnoughRights(#backCatalogId, #user.id)")
    public ResponseEntity<?> changeCatalogOrder(
            @AuthenticationPrincipal User user,
            @PathVariable("editingCatalogId") Integer editingCatalogId,
            @PathVariable(value = "backCatalogId") Integer backCatalogId
            ) {

        catalogService.moveEntityBehind(backCatalogId, editingCatalogId, user.getId());
        LOG.info("User [email={}] has set prev_id as {} for catalog_id {}",
                user.getEmail(),
                backCatalogId,
                editingCatalogId
        );
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{editingCatalogId}/order-after")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#editingCatalogId, #user.id)")
    public ResponseEntity<?> changeCatalogOrder(
            @AuthenticationPrincipal User user,
            @PathVariable("editingCatalogId") Integer editingCatalogId
    ) {
        return changeCatalogOrder(user, editingCatalogId, null);
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
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogsId, #user.id)")
    public ResponseEntity<?> deleteCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") List<Integer> catalogsId
    ) {
        catalogService.deleteEntityAnywhere(catalogsId);
        LOG.info("User [email={}] deleted catalog [id={}]", user.getEmail(), catalogsId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
