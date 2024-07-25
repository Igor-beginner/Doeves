package md.brainet.doeves.note;

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

@RestController
@RequestMapping("/api/v1/note")
public class NoteController {

    private final static Logger LOG = LoggerFactory.getLogger(NoteController.class);
    private final NoteService noteService;
    private final NotePermissionUtil notePermissionUtil;

    public NoteController(NoteService noteService,
                          NotePermissionUtil notePermissionUtil) {
        this.noteService = noteService;
        this.notePermissionUtil = notePermissionUtil;
    }

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
            NoteDTO noteDTO
    ) {
        Note note = noteService.createNote(user, noteDTO);
        LOG.info("User [email={}] created note [id={}]", user.getEmail(), note.id());
        return new ResponseEntity<>(note, HttpStatus.CREATED);
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

    @PatchMapping("{id}/from/{from}/to/{to}")
    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id) " +
            "&& @catalogPermissionUtil.haveEnoughRights(#fromCatalogId, #user.id)" +
            "&& @catalogPermissionUtil.haveEnoughRights(#toCatalogId, #user.id)")
    public ResponseEntity<?> changeCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @PathVariable("from") Integer fromCatalogId,
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

    @PatchMapping("{editingNoteId}/order-after/{prevNoteId}/catalog")
    @PreAuthorize("@catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)" +
            "&& @notePermissionUtil.haveEnoughRights(#editingNoteId, #user.id)" +
            "&& @notePermissionUtil.haveEnoughRights(#prevNoteId, #user.id)")
    public ResponseEntity<?> changeNoteOrder(
            @AuthenticationPrincipal User user,
            @RequestParam("id") Integer catalogId,
            @PathVariable("editingNoteId") Integer editingNoteId,
            @PathVariable("prevNoteId")Integer prevNoteId
    ) {

        noteService.changeOrderNumber(editingNoteId, prevNoteId, catalogId);
        LOG.info("User [email={}] moved note [editingNoteId={}] after [frontNoteId={}]",
                user.getEmail(),
                editingNoteId,
                prevNoteId
        );
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PreAuthorize("@notePermissionUtil.haveEnoughRights(#noteId, #user.id) " +
            "&& @catalogPermissionUtil.haveEnoughRights(#catalogId, #user.id)")
    @DeleteMapping("{id}/catalog")
    public ResponseEntity<?> deleteNote(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("id") Integer catalogId
    ) {
        noteService.removeNote(noteId, catalogId);
        LOG.info("User [email={}] deleted note [id={}]", user.getEmail(), noteId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
