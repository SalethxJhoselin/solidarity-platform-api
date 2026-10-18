package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ComentarioRequestDTO;
import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.entities.Comentario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.ComentarioRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @InjectMocks
    private ComentarioService comentarioService;

    private Usuario usuario;
    private Publicacion publicacion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("voluntario@uagrm.edu.bo");

        publicacion = Publicacion.builder()
                .id(1L)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU15: Agregar comentario exitosamente a publicación activa")
    void agregarComentario_Exitoso() {
        ComentarioRequestDTO request = ComentarioRequestDTO.builder()
                .contenido("Hola, yo puedo donarte ese libro mañana por la tarde")
                .build();

        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(perfilUsuarioRepository.findById(5L)).thenReturn(Optional.empty());

        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> {
            Comentario c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        ComentarioResponseDTO response = comentarioService.agregarComentario(1L, 5L, request);

        assertNotNull(response);
        assertEquals("Hola, yo puedo donarte ese libro mañana por la tarde", response.getContenido());
        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test
    @DisplayName("HU15: Criterio 2 - Bloquear edición de comentario a usuario que no es el autor")
    void editarComentario_NoAutor_LanzaBadRequestException() {
        Usuario autor = new Usuario();
        autor.setId(1L);

        Comentario comentario = Comentario.builder()
                .id(10L)
                .usuario(autor) // Autor es ID 1
                .contenido("Texto original")
                .build();

        when(comentarioRepository.findById(10L)).thenReturn(Optional.of(comentario));

        ComentarioRequestDTO request = ComentarioRequestDTO.builder()
                .contenido("Intento de modificación maliciosa")
                .build();

        assertThrows(BadRequestException.class, () ->
                comentarioService.editarComentario(10L, 99L, false, request)); // Usuario 99 no es autor ni admin

        verify(comentarioRepository, never()).save(any(Comentario.class));
    }
}
