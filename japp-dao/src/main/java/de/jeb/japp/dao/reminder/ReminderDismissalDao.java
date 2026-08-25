package de.jeb.japp.dao.reminder;

import de.jeb.japp.model.reminder.ReminderDismissal;
import de.jeb.japp.model.reminder.ReminderKind;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.ReminderDismissalRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReminderDismissalDao {

    private final ReminderDismissalRepository repository;

    public ReminderDismissalDao(ReminderDismissalRepository repository) {
        this.repository = repository;
    }

    public List<ReminderDismissal> getAllByUser(User user) {
        return repository.findByUser(user);
    }

    public Optional<ReminderDismissal> getByUserAndApplicationAndKindAndDueDate(
            User user, UUID applicationId, ReminderKind kind, LocalDate dueDate) {
        return repository.findByUserAndApplicationIdAndKindAndDueDate(user, applicationId, kind, dueDate);
    }

    public ReminderDismissal save(ReminderDismissal dismissal) {
        return repository.save(dismissal);
    }
}
