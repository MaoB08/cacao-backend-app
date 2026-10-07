package com.cacaoscan.backend.service;

import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PerfilServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsPngMimeTypeWhenFileBytesAreNotPng() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PerfilService service = new PerfilService(repository);
        ReflectionTestUtils.setField(service, "uploadsDir", tempDir.toString());
        MockMultipartFile forgedPng = new MockMultipartFile(
                "foto", "avatar.png", "image/png", "not a png image".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.updateFoto("user@example.com", forgedPng));
        verify(repository, never()).findByIdentificador("user@example.com");
    }

    @Test
    void rejectsUnsupportedImageSignature() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PerfilService service = new PerfilService(repository);
        ReflectionTestUtils.setField(service, "uploadsDir", tempDir.toString());
        MockMultipartFile unsupported = new MockMultipartFile(
                "foto", "avatar.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.updateFoto("user@example.com", unsupported));
        verify(repository, never()).findByIdentificador("user@example.com");
    }
}
