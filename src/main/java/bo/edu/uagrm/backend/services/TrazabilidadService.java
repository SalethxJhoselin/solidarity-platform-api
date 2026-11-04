package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.dtos.SeguimientoSolicitudDTO;
import bo.edu.uagrm.backend.dtos.TimelineHitoDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrazabilidadService {

    private final TrazabilidadSolicitudRepository trazabilidadRepository;
    private final PublicacionRepository publicacionRepository;
    private final DonacionConcretadaRepository donacionRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public TrazabilidadService(TrazabilidadSolicitudRepository trazabilidadRepository,
                               PublicacionRepository publicacionRepository,
                               DonacionConcretadaRepository donacionRepository,
                               PerfilUsuarioRepository perfilUsuarioRepository) {
        this.trazabilidadRepository = trazabilidadRepository;
        this.publicacionRepository = publicacionRepository;
        this.donacionRepository = donacionRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    @Transactional
    public void registrarHito(Publicacion solicitud, EstadoPublicacion estado, String descripcionHito, Usuario responsable) {
        TrazabilidadSolicitud hito = TrazabilidadSolicitud.builder()
                .solicitud(solicitud)
                .estadoAlcanzado(estado)
                .descripcionHito(descripcionHito)
                .usuarioResponsable(responsable)
                .build();

        trazabilidadRepository.save(hito);
    }

    @Transactional(readOnly = true)
    public SeguimientoSolicitudDTO obtenerSeguimiento(Long solicitudId) {
        Publicacion solicitud = publicacionRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        Usuario solicitante = solicitud.getUsuario();
        PerfilUsuario perfil = perfilUsuarioRepository.findById(solicitante.getId()).orElse(null);
        String solicitanteNombre = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : solicitante.getCorreo();

        List<TrazabilidadSolicitud> hitosRegistrados = trazabilidadRepository.findBySolicitudIdOrderByFechaHitoAsc(solicitudId);
        List<DonacionConcretada> donaciones = donacionRepository.findBySolicitudIdOrderByFechaEntregaDesc(solicitudId);

        List<TimelineHitoDTO> lineaDeTiempo = new ArrayList<>();

        // Hito 1: Creación de la Solicitud (Siempre completado)
        lineaDeTiempo.add(TimelineHitoDTO.builder()
                .estado(EstadoPublicacion.ABIERTA)
                .titulo("Solicitud Publicada")
                .descripcion("El estudiante registró la necesidad de ayuda en el sistema")
                .usuarioResponsable(solicitanteNombre)
                .fecha(solicitud.getFechaCreacion())
                .completado(true)
                .build());

        // Hito 2: Coordinación y En Proceso
        boolean enProceso = solicitud.getEstado() == EstadoPublicacion.EN_PROCESO
                || solicitud.getEstado() == EstadoPublicacion.RESUELTA
                || (solicitud.getComentarios() != null && !solicitud.getComentarios().isEmpty())
                || !donaciones.isEmpty();

        LocalDateTime fechaEnProceso = solicitud.getFechaCreacion().plusHours(1);
        String descEnProceso = "Voluntarios o donantes comenzaron interacción y coordinación para brindar ayuda";
        String respEnProceso = "Comunidad Universitaria";

        for (TrazabilidadSolicitud h : hitosRegistrados) {
            if (h.getEstadoAlcanzado() == EstadoPublicacion.EN_PROCESO) {
                fechaEnProceso = h.getFechaHito();
                descEnProceso = h.getDescripcionHito();
                if (h.getUsuarioResponsable() != null) {
                    respEnProceso = h.getUsuarioResponsable().getCorreo();
                }
            }
        }

        lineaDeTiempo.add(TimelineHitoDTO.builder()
                .estado(EstadoPublicacion.EN_PROCESO)
                .titulo("En Proceso y Coordinación")
                .descripcion(descEnProceso)
                .usuarioResponsable(respEnProceso)
                .fecha(fechaEnProceso)
                .completado(enProceso)
                .build());

        // Hito 3: Donación / Ayuda Entregada (Resuelta)
        boolean resuelta = solicitud.getEstado() == EstadoPublicacion.RESUELTA;
        LocalDateTime fechaResolucion = null;
        String descResolucion = "Pendiente de entrega o confirmación final de la ayuda";
        String donanteNombre = null;

        if (!donaciones.isEmpty()) {
            DonacionConcretada d = donaciones.get(0);
            fechaResolucion = d.getFechaEntrega();
            PerfilUsuario pDon = perfilUsuarioRepository.findById(d.getDonante().getId()).orElse(null);
            donanteNombre = (pDon != null) ? pDon.getNombres() + " " + pDon.getApellidos() : d.getDonante().getCorreo();
            descResolucion = "Ayuda entregada con éxito. " + (d.getObservaciones() != null ? d.getObservaciones() : "");
        } else if (resuelta) {
            fechaResolucion = solicitud.getFechaActualizacion();
            descResolucion = "Caso marcado como resuelto satisfactoriamente";
        }

        lineaDeTiempo.add(TimelineHitoDTO.builder()
                .estado(EstadoPublicacion.RESUELTA)
                .titulo("Ayuda Concretada y Resuelta")
                .descripcion(descResolucion)
                .usuarioResponsable(donanteNombre != null ? donanteNombre : "Donante / Voluntario")
                .fecha(fechaResolucion)
                .completado(resuelta)
                .build());

        return SeguimientoSolicitudDTO.builder()
                .solicitudId(solicitud.getId())
                .titulo(solicitud.getTitulo())
                .descripcion(solicitud.getDescripcion())
                .estadoActual(solicitud.getEstado())
                .solicitanteNombre(solicitanteNombre)
                .solicitanteCorreo(solicitante.getCorreo())
                .categoriaNombre(solicitud.getCategoria() != null ? solicitud.getCategoria().getNombre() : "General")
                .fechaCreacion(solicitud.getFechaCreacion())
                .lineaDeTiempo(lineaDeTiempo)
                .build();
    }
}
