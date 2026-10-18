package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final CategoriaPublicacionRepository categoriaRepository;
    private final TipoRecursoRepository tipoRecursoRepository;
    private final DisponibilidadVoluntarioRepository disponibilidadRepository;
    private final AuditoriaService auditoriaService;
    private final TrazabilidadService trazabilidadService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              UsuarioRepository usuarioRepository,
                              PerfilUsuarioRepository perfilUsuarioRepository,
                              CategoriaPublicacionRepository categoriaRepository,
                              TipoRecursoRepository tipoRecursoRepository,
                              DisponibilidadVoluntarioRepository disponibilidadRepository,
                              AuditoriaService auditoriaService,
                              TrazabilidadService trazabilidadService) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.tipoRecursoRepository = tipoRecursoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.auditoriaService = auditoriaService;
        this.trazabilidadService = trazabilidadService;
    }

    // ==========================================
    // HU9: Publicar Necesidad de Ayuda
    // ==========================================
    @Transactional
    public PublicacionResponseDTO publicarNecesidad(Long usuarioId, PublicarNecesidadRequestDTO request) {
        Usuario usuario = findUsuarioById(usuarioId);
        CategoriaPublicacion categoria = findCategoriaById(request.getCategoriaId());

        Publicacion publicacion = Publicacion.builder()
                .usuario(usuario)
                .tipo(TipoPublicacion.NECESIDAD)
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion().trim())
                .categoria(categoria)
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        publicacion = publicacionRepository.save(publicacion);
        auditoriaService.registrarEvento(publicacion, usuario, TipoAccionAuditoria.CREACION, "estado", null, "ABIERTA", "Publicación de necesidad registrada");
        trazabilidadService.registrarHito(publicacion, EstadoPublicacion.ABIERTA, "Solicitud de ayuda registrada en la plataforma", usuario);
        return mapToDTO(publicacion);
    }

    // ==========================================
    // HU10: Publicar Oferta de Voluntariado
    // ==========================================
    @Transactional
    public PublicacionResponseDTO publicarVoluntariado(Long usuarioId, PublicarVoluntariadoRequestDTO request) {
        Usuario usuario = findUsuarioById(usuarioId);
        CategoriaPublicacion categoriaPrincipal = findCategoriaById(request.getCategoriaPrincipalId());

        Set<CategoriaPublicacion> secundarias = new HashSet<>();
        if (request.getCategoriasSecundariasIds() != null && !request.getCategoriasSecundariasIds().isEmpty()) {
            secundarias = new HashSet<>(categoriaRepository.findAllById(request.getCategoriasSecundariasIds()));
        }

        Publicacion publicacion = Publicacion.builder()
                .usuario(usuario)
                .tipo(TipoPublicacion.VOLUNTARIADO)
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion().trim())
                .categoria(categoriaPrincipal)
                .categoriasSecundarias(secundarias)
                .disponibilidadHoraria(request.getDisponibilidadHoraria())
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        publicacion = publicacionRepository.save(publicacion);

        // Guardar disponibilidades de turnos
        if (request.getDisponibilidades() != null && !request.getDisponibilidades().isEmpty()) {
            List<DisponibilidadVoluntario> disps = new ArrayList<>();
            for (DisponibilidadDTO d : request.getDisponibilidades()) {
                DisponibilidadVoluntario disp = DisponibilidadVoluntario.builder()
                        .publicacion(publicacion)
                        .diaSemana(d.getDiaSemana())
                        .turno(d.getTurno())
                        .build();
                disps.add(disp);
            }
            disponibilidadRepository.saveAll(disps);
            publicacion.setDisponibilidades(disps);
        }

        auditoriaService.registrarEvento(publicacion, usuario, TipoAccionAuditoria.CREACION, "estado", null, "ABIERTA", "Oferta de voluntariado registrada");
        return mapToDTO(publicacion);
    }

    // ==========================================
    // HU11: Publicar Oferta de Donación
    // ==========================================
    @Transactional
    public PublicacionResponseDTO publicarDonacion(Long usuarioId, PublicarDonacionRequestDTO request) {
        Usuario usuario = findUsuarioById(usuarioId);
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(request.getTipoRecursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado con ID: " + request.getTipoRecursoId()));

        if (!tipoRecurso.getActivo()) {
            throw new BadRequestException("El tipo de recurso seleccionado se encuentra inactivo");
        }

        CategoriaPublicacion categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = findCategoriaById(request.getCategoriaId());
        }

        Publicacion publicacion = Publicacion.builder()
                .usuario(usuario)
                .tipo(TipoPublicacion.DONACION)
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion().trim())
                .categoria(categoria)
                .tipoRecurso(tipoRecurso)
                .cantidadTotal(request.getCantidad())
                .cantidadDisponible(request.getCantidad())
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        publicacion = publicacionRepository.save(publicacion);
        auditoriaService.registrarEvento(publicacion, usuario, TipoAccionAuditoria.CREACION, "estado", null, "ABIERTA", "Oferta de donación de recursos registrada");
        return mapToDTO(publicacion);
    }

    // ==========================================
    // HU12: Publicar Actividad Colectiva
    // ==========================================
    @Transactional
    public PublicacionResponseDTO publicarActividadColectiva(Long usuarioId, PublicarActividadColectivaRequestDTO request) {
        Usuario usuario = findUsuarioById(usuarioId);

        // Criterio de Aceptación 1: La fecha de fin debe ser estrictamente posterior o igual a la fecha de inicio
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio de la actividad");
        }

        CategoriaPublicacion categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = findCategoriaById(request.getCategoriaId());
        }

        Publicacion publicacion = Publicacion.builder()
                .usuario(usuario)
                .tipo(TipoPublicacion.ACTIVIDAD_COLECTIVA)
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion().trim())
                .categoria(categoria)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .lugar(request.getLugar().trim())
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        publicacion = publicacionRepository.save(publicacion);
        auditoriaService.registrarEvento(publicacion, usuario, TipoAccionAuditoria.CREACION, "estado", null, "ABIERTA", "Actividad colectiva solidaria convocada");
        return mapToDTO(publicacion);
    }

    // ==========================================
    // HU13: Consultar, Editar y Eliminar Publicaciones Propias (Soft Delete)
    // ==========================================
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> obtenerMisPublicaciones(Long usuarioId) {
        return publicacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtenerPublicacionPorId(Long id) {
        Publicacion pub = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + id));

        // Actualizar automáticamente actividades colectivas vencidas si corresponde
        actualizarEstadoSiVencida(pub);

        return mapToDTO(pub);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> listarPublicacionesActivas(TipoPublicacion tipo) {
        List<Publicacion> lista;
        if (tipo != null) {
            lista = publicacionRepository.findByTipoAndActivoTrueOrderByFechaCreacionDesc(tipo);
        } else {
            lista = publicacionRepository.findByActivoTrueOrderByFechaCreacionDesc();
        }

        return lista.stream()
                .peek(this::actualizarEstadoSiVencida)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PublicacionResponseDTO actualizarPublicacionPropia(Long publicacionId, Long usuarioId, boolean esAdmin,
                                                              ActualizarPublicacionRequestDTO request) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId));

        // Criterio de Aceptación 1: Solo el autor o el administrador pueden editar
        validarAutoriaOAdmin(publicacion, usuarioId, esAdmin);
        Usuario responsable = publicacion.getUsuario().getId().equals(usuarioId)
                ? publicacion.getUsuario()
                : findUsuarioById(usuarioId);

        if (request.getTitulo() != null && !request.getTitulo().trim().isEmpty() && !request.getTitulo().trim().equals(publicacion.getTitulo())) {
            auditoriaService.registrarEvento(publicacion, responsable, TipoAccionAuditoria.EDICION_TEXTO,
                    "titulo", publicacion.getTitulo(), request.getTitulo().trim(), "Actualización de título de la publicación");
            publicacion.setTitulo(request.getTitulo().trim());
        }
        if (request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty() && !request.getDescripcion().trim().equals(publicacion.getDescripcion())) {
            auditoriaService.registrarEvento(publicacion, responsable, TipoAccionAuditoria.EDICION_TEXTO,
                    "descripcion", "Versión previa de la descripción", request.getDescripcion().trim(), "Actualización del contenido de la publicación");
            publicacion.setDescripcion(request.getDescripcion().trim());
        }
        if (request.getCategoriaId() != null) {
            publicacion.setCategoria(findCategoriaById(request.getCategoriaId()));
        }
        if (request.getCategoriasSecundariasIds() != null) {
            Set<CategoriaPublicacion> secundarias = new HashSet<>(categoriaRepository.findAllById(request.getCategoriasSecundariasIds()));
            publicacion.setCategoriasSecundarias(secundarias);
        }
        if (request.getTipoRecursoId() != null) {
            TipoRecurso tr = tipoRecursoRepository.findById(request.getTipoRecursoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado"));
            publicacion.setTipoRecurso(tr);
        }
        if (request.getCantidadTotal() != null) {
            publicacion.setCantidadTotal(request.getCantidadTotal());
            if (publicacion.getCantidadDisponible() == null || publicacion.getCantidadDisponible() > request.getCantidadTotal()) {
                publicacion.setCantidadDisponible(request.getCantidadTotal());
            }
        }
        if (request.getFechaInicio() != null) {
            publicacion.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFin() != null) {
            if (publicacion.getFechaInicio() != null && request.getFechaFin().isBefore(publicacion.getFechaInicio())) {
                throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
            }
            publicacion.setFechaFin(request.getFechaFin());
        }
        if (request.getLugar() != null) {
            publicacion.setLugar(request.getLugar().trim());
        }
        if (request.getDisponibilidadHoraria() != null) {
            publicacion.setDisponibilidadHoraria(request.getDisponibilidadHoraria().trim());
        }
        if (request.getEstado() != null && request.getEstado() != publicacion.getEstado()) {
            auditoriaService.registrarEvento(publicacion, responsable, TipoAccionAuditoria.CAMBIO_ESTADO,
                    "estado", publicacion.getEstado().name(), request.getEstado().name(), "Cambio de estado por usuario/administrador");
            if (publicacion.getTipo() == TipoPublicacion.NECESIDAD) {
                trazabilidadService.registrarHito(publicacion, request.getEstado(), "Estado actualizado a " + request.getEstado().name(), responsable);
            }
            publicacion.setEstado(request.getEstado());
        }

        // Actualizar disponibilidades si es voluntariado
        if (request.getDisponibilidades() != null) {
            disponibilidadRepository.deleteByPublicacionId(publicacion.getId());
            List<DisponibilidadVoluntario> disps = new ArrayList<>();
            for (DisponibilidadDTO d : request.getDisponibilidades()) {
                disps.add(DisponibilidadVoluntario.builder()
                        .publicacion(publicacion)
                        .diaSemana(d.getDiaSemana())
                        .turno(d.getTurno())
                        .build());
            }
            disponibilidadRepository.saveAll(disps);
            publicacion.setDisponibilidades(disps);
        }

        publicacion = publicacionRepository.save(publicacion);
        return mapToDTO(publicacion);
    }

    @Transactional
    public void eliminarPublicacionPropia(Long publicacionId, Long usuarioId, boolean esAdmin) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId));

        // Criterio de Aceptación 1: Solo el autor o el administrador pueden eliminar
        validarAutoriaOAdmin(publicacion, usuarioId, esAdmin);
        Usuario responsable = publicacion.getUsuario().getId().equals(usuarioId)
                ? publicacion.getUsuario()
                : findUsuarioById(usuarioId);

        // Criterio de Aceptación 2: Borrado Lógico (Soft Delete)
        publicacion.setActivo(false);
        publicacion.setEstado(EstadoPublicacion.CANCELADA);
        publicacionRepository.save(publicacion);

        auditoriaService.registrarEvento(publicacion, responsable, TipoAccionAuditoria.CANCELACION_SOFT_DELETE,
                "activo", "true", "false", "Baja lógica (soft delete) solicitada por el usuario o administrador");
    }

    // ==========================================
    // Métodos Auxiliares
    // ==========================================
    private void validarAutoriaOAdmin(Publicacion publicacion, Long usuarioId, boolean esAdmin) {
        if (!publicacion.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("No tiene permisos para modificar o dar de baja esta publicación");
        }
    }

    private void actualizarEstadoSiVencida(Publicacion pub) {
        if (pub.getTipo() == TipoPublicacion.ACTIVIDAD_COLECTIVA
                && pub.getFechaFin() != null
                && pub.getFechaFin().isBefore(LocalDateTime.now())
                && pub.getEstado() == EstadoPublicacion.ABIERTA) {
            pub.setEstado(EstadoPublicacion.FINALIZADA);
            publicacionRepository.save(pub);
        }
    }

    private Usuario findUsuarioById(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
    }

    private CategoriaPublicacion findCategoriaById(Long categoriaId) {
        CategoriaPublicacion cat = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoriaId));
        if (!cat.getActivo()) {
            throw new BadRequestException("La categoría seleccionada se encuentra inactiva");
        }
        return cat;
    }

    // ==========================================
    // HU16: Buscar y Filtrar Publicaciones
    // ==========================================
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PublicacionResponseDTO> buscarYFiltrar(FiltroPublicacionDTO filtro) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion");
        if (filtro != null && filtro.getOrden() != null && filtro.getOrden().equalsIgnoreCase("antiguas")) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "fechaCreacion");
        }

        int page = (filtro != null) ? Math.max(0, filtro.getPage()) : 0;
        int size = (filtro != null && filtro.getSize() > 0) ? Math.min(filtro.getSize(), 50) : 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.domain.Page<Publicacion> publicacionesPage = publicacionRepository.findAll(
                bo.edu.uagrm.backend.repositories.specifications.PublicacionSpecification.conFiltros(filtro),
                pageable
        );

        return publicacionesPage.map(this::mapToDTO);
    }

    public PublicacionResponseDTO mapToDTO(Publicacion pub) {
        Usuario usuario = pub.getUsuario();
        PerfilUsuario perfil = perfilUsuarioRepository.findById(usuario.getId()).orElse(null);

        String autorNombre = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : usuario.getCorreo();
        String autorCarrera = (perfil != null) ? perfil.getCarrera() : null;
        String autorFacultad = (perfil != null) ? perfil.getFacultad() : null;
        String autorTelefono = (perfil != null) ? perfil.getTelefono() : null;
        String autorFoto = (perfil != null) ? perfil.getUrlFotoPerfil() : null;

        List<CategoriaDTO> secundariasDTO = pub.getCategoriasSecundarias().stream()
                .map(c -> CategoriaDTO.builder()
                        .id(c.getId())
                        .nombre(c.getNombre())
                        .descripcion(c.getDescripcion())
                        .icono(c.getIcono())
                        .activo(c.getActivo())
                        .build())
                .collect(Collectors.toList());

        List<DisponibilidadDTO> dispsDTO = pub.getDisponibilidades().stream()
                .map(d -> DisponibilidadDTO.builder()
                        .diaSemana(d.getDiaSemana())
                        .turno(d.getTurno())
                        .build())
                .collect(Collectors.toList());

        List<ArchivoAdjuntoDTO> archivosDTO = (pub.getArchivos() != null) ? pub.getArchivos().stream()
                .map(a -> ArchivoAdjuntoDTO.builder()
                        .id(a.getId())
                        .nombreOriginal(a.getNombreOriginal())
                        .nombreAlmacenado(a.getNombreAlmacenado())
                        .tipoMime(a.getTipoMime())
                        .tamanoBytes(a.getTamanoBytes())
                        .urlDescarga(a.getUrlDescarga())
                        .esImagen(a.getTipoMime() != null && a.getTipoMime().startsWith("image/"))
                        .fechaCreacion(a.getFechaCreacion())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        int cantidadComentarios = (pub.getComentarios() != null)
                ? (int) pub.getComentarios().stream().filter(Comentario::getActivo).count()
                : 0;

        return PublicacionResponseDTO.builder()
                .id(pub.getId())
                .usuarioId(usuario.getId())
                .autorNombre(autorNombre)
                .autorCorreo(usuario.getCorreo())
                .autorCarrera(autorCarrera)
                .autorFacultad(autorFacultad)
                .autorTelefono(autorTelefono)
                .autorFotoPerfil(autorFoto)
                .tipo(pub.getTipo())
                .titulo(pub.getTitulo())
                .descripcion(pub.getDescripcion())
                .categoriaId(pub.getCategoria() != null ? pub.getCategoria().getId() : null)
                .categoriaNombre(pub.getCategoria() != null ? pub.getCategoria().getNombre() : null)
                .categoriaIcono(pub.getCategoria() != null ? pub.getCategoria().getIcono() : null)
                .categoriasSecundarias(secundariasDTO)
                .estado(pub.getEstado())
                .activo(pub.getActivo())
                .tipoRecursoId(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getId() : null)
                .tipoRecursoNombre(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getNombre() : null)
                .tipoRecursoUnidad(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getUnidadMedida() : null)
                .cantidadTotal(pub.getCantidadTotal())
                .cantidadDisponible(pub.getCantidadDisponible())
                .fechaInicio(pub.getFechaInicio())
                .fechaFin(pub.getFechaFin())
                .lugar(pub.getLugar())
                .disponibilidadHoraria(pub.getDisponibilidadHoraria())
                .disponibilidades(dispsDTO)
                .archivos(archivosDTO)
                .cantidadComentarios(cantidadComentarios)
                .fechaCreacion(pub.getFechaCreacion())
                .fechaActualizacion(pub.getFechaActualizacion())
                .build();
    }
}
