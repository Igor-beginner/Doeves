package md.brainet.doeves.note;

import io.swagger.v3.oas.annotations.Operation;
import md.brainet.doeves.catalog.CatalogOrderingRequest;
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
@RequestMapping("/api/v1/note")
public class NoteController {

    private final static Logger LOG = LoggerFactory.getLogger(NoteController.class);
    private final NoteServiceImpl noteService;
    private final NotePermissionUtil notePermissionUtil;

    public NoteController(NoteServiceImpl noteService,
                          NotePermissionUtil notePermissionUtil) {
        this.noteService = noteService;
        this.notePermissionUtil = notePermissionUtil;
    }

    @Operation(summary = "Fetch concrete note")
    @GetMapping("{id}")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id)")
    public ResponseEntity<?> fetchConcreteNote(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId
    ) {
        Note note = noteService.fetchNote(noteId);
        LOG.info("User [email={}] requested note[id={}]", user.getEmail(), note.id());
        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @Operation(
            summary = "Fetch all user notes",
            description = "You can fetch all notes using this method by boolean 'including-catalogs' query param.")
    @GetMapping("all")
    public ResponseEntity<?> fetchAllOwnerNote(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "offset", defaultValue = "0") Integer offset,
            @RequestParam(name = "limit", defaultValue = "10") Integer limit,
            @RequestParam(name = "including-catalogs", defaultValue = "false") Boolean includingCatalogs
    ) {

        List<NotePreview> notes = noteService.fetchAllOwnerNote(
                user.getId(),
                new LimitedListNoteRequest(
                        offset,
                        limit,
                        includingCatalogs
                )
        );

        LOG.info("User [email={}] fetched all tasks [includingCatalogs={}]", user.getEmail(), includingCatalogs);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> postNote(
            @AuthenticationPrincipal User user,
            @RequestBody NoteDTO noteDTO
    ) {
        Integer noteId = noteService.insertEntityIntoContextOnTop(noteDTO, user);
        LOG.info("User [email={}] created note [id={}]", user.getEmail(), noteId);
        return new ResponseEntity<>(Map.of("id", noteId), HttpStatus.CREATED);
    }

    @PatchMapping("{id}/name")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.getId())")
    public ResponseEntity<?> changeName(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("val") String newName
    ) {

        noteService.changeName(noteId, newName);
        LOG.info("User [email={}] changed note [id={}] name on '{}'", user.getEmail(), noteId, newName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{id}/description")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id)")
    public ResponseEntity<?> changeDescription(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("val") String newDescription
    ) {

        noteService.changeDescription(noteId, newDescription);
        LOG.info("User [email={}] changed note [id={}] description on '{}'", user.getEmail(), noteId, newDescription);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @Operation(
            summary = "Change note catalog",
            description = "You can change note catalog using this method. Where {id} is editing note, {from} is current catalog id and {to} wishing catalog id")
    @PatchMapping("{id}/moving-to/{to}")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id) " +
            "&& @catalogPermissionUtil.haveEnoughRights(#fromCatalogId, #user.id)" +
            "&& @catalogPermissionUtil.haveEnoughRights(#toCatalogId, #user.id)")
    public ResponseEntity<?> changeCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("from") Integer fromCatalogId,
            @PathVariable("to") Integer toCatalogId
    ) {
        noteService.changeCatalog(new CatalogOrderingRequest(
                noteId,
                fromCatalogId,
                toCatalogId,
                user
        ));
        LOG.info("User [email={}] has moved [noteId={}] from catalog[id={}] to [id={}]'",
                user.getEmail(),
                noteId,
                fromCatalogId,
                toCatalogId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Operation(
            summary = "Copy note to",
            description = "You can change note catalog using this method. Where {id} is editing note, {from} is current catalog id and {to} wishing catalog id")
    @PostMapping("{noteId}/copy-to")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id) " +
            "&& @catalogPermissionUtil.haveEnoughRights(#toCatalogId, #user.id)")
    public ResponseEntity<?> duplicateNote(
            @AuthenticationPrincipal User user,
            @PathVariable("noteId") Integer noteId,
            @RequestParam("catalog-id") Integer toCatalogId
    ) {
        noteService.duplicate(noteId, toCatalogId);
        LOG.info("User [email={}] has duplicated [noteId={}] to [catalogId={}]'",
                user.getEmail(),
                noteId,
                toCatalogId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Change note order number")
    @PatchMapping("{editingNoteId}/order-after")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)" +
            "&& @notePermissionUtil.haveEnoughRights(#editingNoteId, #user.id)" +
            "&& @notePermissionUtil.haveEnoughRights(#prevNoteId, #user.id)")
    public ResponseEntity<?> changeNoteOrder(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "catalog-id", required = false) Integer catalogId,
            @PathVariable("editingNoteId") Integer editingNoteId,
            @RequestParam(value = "prev-note-id", required = false) Integer prevNoteId
    ) {
        if(catalogId == null) {
            catalogId = user.getRootCatalogId();
        }
        noteService.moveEntityBehind(prevNoteId, editingNoteId, catalogId);
        LOG.info("User [email={}] moved note [editingNoteId={}] after [frontNoteId={}]",
                user.getEmail(),
                editingNoteId,
                prevNoteId
        );
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#notesId, #user.id) " +
            "&& @catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteNote(
            @AuthenticationPrincipal User user,
            @PathVariable("id") List<Integer> notesId,
            @RequestParam(value = "catalog-id", required = false) Integer catalogId,
            @RequestParam(value = "anywhere", required = false, defaultValue = "false") boolean anywhere
    ) {
        noteService.deleteEntity(notesId, catalogId, user.getRootCatalogId(), anywhere);
        LOG.info("User [email={}] deleted notes [id's={}]", user.getEmail(), notesId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
