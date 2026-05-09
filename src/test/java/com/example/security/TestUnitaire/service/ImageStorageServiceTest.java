package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        imageStorageService = new ImageStorageService();
        ReflectionTestUtils.setField(imageStorageService, "uploadDir", tempDir.toString());
    }

    // ==================== saveImage ====================

    @Test
    void saveImage_WithJpgFile_ShouldReturnPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "fake-jpeg-content".getBytes());

        String path = imageStorageService.saveImage(file, "charge-sheets");

        assertThat(path).isNotNull();
        assertThat(path).contains("charge-sheets");
        assertThat(path).endsWith(".jpg");
    }

    @Test
    void saveImage_WithPngFile_ShouldReturnPathWithPngExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "photo.png", "image/png", "fake-png-content".getBytes());

        String path = imageStorageService.saveImage(file, "claims");

        assertThat(path).endsWith(".png");
        assertThat(path).contains("claims");
    }

    @Test
    void saveImage_ShouldCreateSubFolderIfNotExists() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "content".getBytes());

        imageStorageService.saveImage(file, "new-subfolder");

        Path subFolder = tempDir.resolve("new-subfolder");
        assertThat(Files.exists(subFolder)).isTrue();
    }

    @Test
    void saveImage_ShouldGenerateUniqueFilenames() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile(
                "image", "same.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "image", "same.jpg", "image/jpeg", "content2".getBytes());

        String path1 = imageStorageService.saveImage(file1, "charge-sheets");
        String path2 = imageStorageService.saveImage(file2, "charge-sheets");

        assertThat(path1).isNotEqualTo(path2);
    }

    @Test
    void saveImage_ShouldActuallyWriteFileOnDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "actual-content".getBytes());

        String returnedPath = imageStorageService.saveImage(file, "charge-sheets");

        // Extract filename from returned path
        String filename = returnedPath.substring(returnedPath.lastIndexOf("/") + 1);
        Path physicalFile = tempDir.resolve("charge-sheets").resolve(filename);
        assertThat(Files.exists(physicalFile)).isTrue();
    }

    // ==================== getImage ====================

    @Test
    void getImage_WithExistingFile_ShouldReturnBytes() throws IOException {
        // Create a file in the temp dir
        Path subDir = tempDir.resolve("charge-sheets");
        Files.createDirectories(subDir);
        Path file = subDir.resolve("test-image.jpg");
        byte[] content = "fake-image-bytes".getBytes();
        Files.write(file, content);

        byte[] result = imageStorageService.getImage("charge-sheets/test-image.jpg");

        assertThat(result).isEqualTo(content);
    }

    @Test
    void getImage_WithPathPrefixUploads_ShouldStripPrefix() throws IOException {
        Path subDir = tempDir.resolve("claims");
        Files.createDirectories(subDir);
        Path file = subDir.resolve("claim-img.jpg");
        Files.write(file, "img-bytes".getBytes());

        byte[] result = imageStorageService.getImage("uploads/claims/claim-img.jpg");

        assertThat(result).isEqualTo("img-bytes".getBytes());
    }

    @Test
    void getImage_WithNonExistentFile_ShouldThrowIOException() {
        assertThatThrownBy(() -> imageStorageService.getImage("charge-sheets/nonexistent.jpg"))
                .isInstanceOf(IOException.class);
    }

    // ==================== deleteImage ====================
/*
    @Test
    void deleteImage_WithExistingFile_ShouldDeleteFile() throws IOException {
        Path subDir = tempDir.resolve("charge-sheets");
        Files.createDirectories(subDir);
        Path file = subDir.resolve("to-delete.jpg");
        Files.write(file, "data".getBytes());
        assertThat(Files.exists(file)).isTrue();

        imageStorageService.deleteImage("charge-sheets/to-delete.jpg");

        assertThat(Files.exists(file)).isFalse();
    }
*/
    @Test
    void deleteImage_WithNonExistentFile_ShouldNotThrow() throws IOException {
        // Should not throw even if file doesn't exist
        imageStorageService.deleteImage("charge-sheets/does-not-exist.jpg");
    }
}