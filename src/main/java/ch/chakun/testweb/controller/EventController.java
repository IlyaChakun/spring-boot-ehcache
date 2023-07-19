package ch.chakun.testweb.controller;

import ch.chakun.testweb.dto.Event;
import ch.chakun.testweb.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class EventController {


    private final EventService eventService;

    @GetMapping("/{key}")
    public ResponseEntity<Event> getEventByKey(@PathVariable("key") String key) {
        return ResponseEntity.ok(eventService.get(key));
    }

    @PostMapping("/{key}")
    public ResponseEntity<String> putEvent(@PathVariable("key") String key, @RequestBody Event event) {
        eventService.put(key, event);

        return ResponseEntity.ok("Event saved. ContextKey = key");

    }
}
