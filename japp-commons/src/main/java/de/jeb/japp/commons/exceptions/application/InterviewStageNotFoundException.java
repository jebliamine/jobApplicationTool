package de.jeb.japp.commons.exceptions.application;

/** Thrown when an Application's interview stage sub-resource doesn't exist. */
public class InterviewStageNotFoundException extends RuntimeException {
    public InterviewStageNotFoundException(String message) {
        super(message);
    }
}
