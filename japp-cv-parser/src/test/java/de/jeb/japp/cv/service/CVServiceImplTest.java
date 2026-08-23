package de.jeb.japp.cv.service;

import de.jeb.japp.cv.service.parser.DocumentExtractionService;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQuality;
import de.jeb.japp.cv.service.parser.normalizer.NormalizedDocument;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.file.storage.services.FileStorageServiceInterface;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.ExtractionStatus;
import de.jeb.japp.model.storage.StoredFile;
import de.jeb.japp.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CVServiceImplTest {

    @Mock
    private CVDao cvDao;
    @Mock
    private UserDao userDao;
    @Mock
    private FileStorageServiceInterface storageService;
    @Mock
    private DocumentExtractionService extractionService;

    private CVServiceImpl cvService;
    private User owner;

    @BeforeEach
    void setUp() {
        cvService = new CVServiceImpl(cvDao, userDao, storageService, extractionService);

        owner = new User();
        owner.setId(UUID.randomUUID());

        lenient().when(cvDao.saveCV(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private MultipartFile validFile() {
        return new MockMultipartFile("file", "cv.pdf", "application/pdf", "content".getBytes());
    }

    private StoredFile stubStorage() throws Exception {
        StoredFile stored = new StoredFile("stored-key", "cv.pdf", "application/pdf", 7L);
        when(storageService.save(any(), eq(owner.getId()))).thenReturn(stored);
        return stored;
    }

    @Test
    void uploadPersistsExtractedTextOnSuccess() throws Exception {
        StoredFile stored = stubStorage();
        Resource resource = new ByteArrayResource("bytes".getBytes());
        when(storageService.load(stored.getStorageKey())).thenReturn(resource);
        when(extractionService.process(resource, stored.getOriginalFilename(), stored.getContentType()))
                .thenReturn(new NormalizedDocument("Extracted CV text.", "cv.pdf", "application/pdf",
                        ExtractionQuality.GOOD, ExtractionMethod.TIKA));

        CVDocument result = cvService.uploadCv(validFile(), "My CV", owner);

        assertThat(result.getExtractedText()).isEqualTo("Extracted CV text.");
        assertThat(result.getExtractionStatus()).isEqualTo(ExtractionStatus.COMPLETED);
        assertThat(result.getExtractionQuality()).isEqualTo("GOOD");
        assertThat(result.getExtractedAt()).isNotNull();
    }

    @Test
    void uploadStillSucceedsWhenExtractionFails() throws Exception {
        StoredFile stored = stubStorage();
        Resource resource = new ByteArrayResource("bytes".getBytes());
        when(storageService.load(stored.getStorageKey())).thenReturn(resource);
        when(extractionService.process(any(), any(), any())).thenThrow(new RuntimeException("extraction blew up"));

        CVDocument result = cvService.uploadCv(validFile(), "My CV", owner);

        assertThat(result.getExtractionStatus()).isEqualTo(ExtractionStatus.FAILED);
        assertThat(result.getExtractedText()).isNull();
        verify(cvDao).saveCV(any());
    }

    @Test
    void uploadStoresTheDocumentEvenWhenTheExtractionServiceThrows() throws Exception {
        StoredFile stored = stubStorage();
        when(storageService.load(stored.getStorageKey())).thenThrow(new RuntimeException("storage unavailable"));

        CVDocument result = cvService.uploadCv(validFile(), "My CV", owner);

        ArgumentCaptor<CVDocument> captor = ArgumentCaptor.forClass(CVDocument.class);
        verify(cvDao).saveCV(captor.capture());
        assertThat(captor.getValue().getExtractionStatus()).isEqualTo(ExtractionStatus.FAILED);
        assertThat(result.getFileName()).isEqualTo("cv.pdf");
    }
}
