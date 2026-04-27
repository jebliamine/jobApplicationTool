package de.jeb.japp.dao.genrationrequest;

import de.jeb.japp.repositories.GenerationRequestRepository;

public class GenerationRequest {
    
    private final GenerationRequestRepository requestRepository;


    public GenerationRequest(GenerationRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

}
