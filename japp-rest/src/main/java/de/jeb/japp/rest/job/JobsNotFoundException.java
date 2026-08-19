package de.jeb.japp.rest.job;

/** Thrown when a Company or Job doesn't exist. Shared between both since they're one vertical slice. */
public class JobsNotFoundException extends RuntimeException {
    public JobsNotFoundException(String message) {
        super(message);
    }
}
