package md.brainet.doeves.note;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(
                    description = "Response with 'including-catalogs=true' can be seems like:",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "note-preview",
                    example = """
                            [
                                {
                                   "id" : 1,
                                   "name" : "Some note title",
                                   "description" : "Description here",
                                   "catalog" : {
                                       "id" : 3,
                                       "name" : "Some catalog name"
                                   },
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                },
                                {
                                   "id" : 2,
                                   "name" : null,
                                   "description" : "Description here 2",
                                   "catalog" : {
                                       "id" : 5,
                                       "name" : null
                                   },
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                },
                                {
                                   "id" : 2,
                                   "name" : null,
                                   "description" : "Description here 2",
                                   "catalog" : null
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                }
                            ]
                            """))
            ),
            @ApiResponse(
                    description = "Response with 'including-catalogs=false' can be seems like:",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "note-preview",
                                    example = """
                            [
                                {
                                   "id" : 2,
                                   "name" : null,
                                   "description" : "Description here 2",
                                   "catalog" : null
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                },
                                {
                                   "id" : 3,
                                   "name" : null,
                                   "description" : "Description here 212",
                                   "catalog" : null
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                },
                                {
                                   "id" : 10,
                                   "name" : null,
                                   "description" : "Description here 232",
                                   "catalog" : null
                                   "date_of_create" : "2024-07-27T14:47:39.4152046"
                                },
                            ]
                            """))
            )
    })
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


    @Operation(
            summary = "Change note catalog",
            description = "You can change note catalog using this method. Where {id} is editing note, {from} is current catalog id and {to} wishing catalog id")
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

        noteService.changeOrderNumber(editingNoteId, prevNoteId, catalogId);
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
            @RequestParam("catalog-id") Integer catalogId
    ) {
        noteService.removeNotes(notesId, catalogId);
        LOG.info("User [email={}] deleted notes [id's={}]", user.getEmail(), notesId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
