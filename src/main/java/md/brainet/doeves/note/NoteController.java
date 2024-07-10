package md.brainet.doeves.note;

import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/note")
public class NoteController {

    private final static Logger LOG = LoggerFactory.getLogger(NoteController.class);
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("{id}")
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
            LimitedListNoteRequest request
    ) {

        List<Note> notes = noteService.fetchAllOwnerNote(user.getId(), request);
        LOG.info("User [email={}] fetched all tasks [includingCatalogs={}]", user.getEmail(), request.includingCatalogs());
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> postNote(
            @AuthenticationPrincipal User user,
            NoteDTO noteDTO
    ) {
        Note note = noteService.createNote(user.getId(), noteDTO);
        LOG.info("User [email={}] created note [id={}]", user.getEmail(), note.id());
        return new ResponseEntity<>(note, HttpStatus.CREATED);
    }

    @PatchMapping("{id}/order")
    public ResponseEntity<?> changeNoteOrder(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("v")Integer orderNumber
    ) {

        noteService.changeOrderNumber(noteId, orderNumber);
        LOG.info("User [id={}] changed note [id={}] order number on {}",
                user.getId(),
                noteId,
                orderNumber
        );
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{id}/name")
    public ResponseEntity<?> changeName(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("v") String newName
    ) {

        noteService.changeName(noteId, newName);
        LOG.info("User [email={}] changed note [id={}] name on '{}'", user.getEmail(), noteId, newName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{id}/description")
    public ResponseEntity<?> changeDescription(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("v") String newDescription
    ) {

        noteService.changeDescription(noteId, newDescription);
        LOG.info("User [email={}] changed note [id={}] description on '{}'", user.getEmail(), noteId, newDescription);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("{id}/catalog")
    public ResponseEntity<?> changeCatalog(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId,
            @RequestParam("v") Integer catalogId
    ) {
        noteService.changeCatalog(noteId, catalogId);
        LOG.info("User [email={}] changed note [noteId={}] catalog on [catalogId={}]'", user.getEmail(), noteId, catalogId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteNote(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer noteId
    ) {
        noteService.removeNote(noteId);
        LOG.info("User [email={}] deleted note [id={}]", user.getEmail(), noteId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
