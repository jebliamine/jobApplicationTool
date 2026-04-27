package de.jeb.japp.dao.cv;

import de.jeb.japp.repositories.CVRepository;

public class CVDao {
    
    private final CVRepository cvRepository;

    public CVDao(CVRepository cvRepository) {
        this.cvRepository = cvRepository;

    }

}
