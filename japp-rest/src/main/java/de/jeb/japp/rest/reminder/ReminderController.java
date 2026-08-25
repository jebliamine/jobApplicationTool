package de.jeb.japp.rest.reminder;

import de.jeb.japp.model.reminder.dto.ReminderDismissRequest;
import de.jeb.japp.model.reminder.dto.ReminderResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.reminder.service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<ReminderResponse> getReminders(@AuthenticationPrincipal User user) {
        return reminderService.list(user);
    }

    @PostMapping("/dismiss")
    public ResponseEntity<Void> dismissReminder(
            @RequestBody ReminderDismissRequest request,
            @AuthenticationPrincipal User user
    ) {
        reminderService.dismiss(request, user);
        return ResponseEntity.noContent().build();
    }
}
