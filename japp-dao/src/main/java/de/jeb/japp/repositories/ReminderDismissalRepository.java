package de.jeb.japp.repositories;

import de.jeb.japp.model.reminder.ReminderDismissal;
import de.jeb.japp.model.reminder.ReminderKind;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderDismissalRepository extends JpaRepository<ReminderDismissal, UUID> {
    List<ReminderDismissal> findByUser(User user);

    Optional<ReminderDismissal> findByUserAndApplicationIdAndKindAndDueDate(
            User user, UUID applicationId, ReminderKind kind, LocalDate dueDate);
}
