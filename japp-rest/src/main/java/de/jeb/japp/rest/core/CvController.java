package de.jeb.japp.rest.core;

import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.model.cv.CVDocument;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/cv")
public class CvController {
    private final CVDao cvDao;

    public CvController(CVDao cvDao) {
        this.cvDao = cvDao;
    }

    @GetMapping("/{id}")
    public CVDocument getCv(@PathVariable UUID id) {
        Optional<CVDocument> optCvDocument = cvDao.getCVById(id);
        if (optCvDocument.isPresent())
            return optCvDocument.get();

        else
            throw new RuntimeException("not found");
    }

    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    public List<CVDocument> getAllCvs() {
        return cvDao.getAllCVs();
    }


}
