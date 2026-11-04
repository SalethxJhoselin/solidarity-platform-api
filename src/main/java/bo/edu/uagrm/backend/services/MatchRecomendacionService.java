package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.dtos.MatchRecomendacionDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchRecomendacionService {

    private final PublicacionRepository publicacionRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public MatchRecomendacionService(PublicacionRepository publicacionRepository,
                                     PerfilUsuarioRepository perfilUsuarioRepository) {
        this.publicacionRepository = publicacionRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchRecomendacionDTO> calcularMejoresMatches(Long solicitudId, int topK) {
        Publicacion solicitud = publicacionRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        PerfilUsuario perfilSolicitante = perfilUsuarioRepository.findById(solicitud.getUsuario().getId()).orElse(null);

        // Candidatos: Voluntarios activos y Donaciones activas
        List<Publicacion> ofertasVoluntariado = publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.VOLUNTARIADO, EstadoPublicacion.ABIERTA);

        List<Publicacion> ofertasDonacion = publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.DONACION, EstadoPublicacion.ABIERTA);

        List<Publicacion> candidatos = new ArrayList<>();
        candidatos.addAll(ofertasVoluntariado);
        candidatos.addAll(ofertasDonacion);

        List<MatchRecomendacionDTO> resultados = new ArrayList<>();

        for (Publicacion candidato : candidatos) {
            // No emparejar al estudiante consigo mismo
            if (candidato.getUsuario().getId().equals(solicitud.getUsuario().getId())) {
                continue;
            }

            PerfilUsuario perfilCandidato = perfilUsuarioRepository.findById(candidato.getUsuario().getId()).orElse(null);
            List<String> factores = new ArrayList<>();

            double score = evaluarCompatibilidad(solicitud, perfilSolicitante, candidato, perfilCandidato, factores);

            if (score > 30.0) { // Umbral mínimo de relevancia
                long porcentajeRedondeado = Math.round(score);
                String etiqueta = "Match " + porcentajeRedondeado + "%";

                Usuario oferente = candidato.getUsuario();
                String nombreOferente = (perfilCandidato != null) ? perfilCandidato.getNombres() + " " + perfilCandidato.getApellidos() : oferente.getCorreo();
                String carrera = (perfilCandidato != null) ? perfilCandidato.getCarrera() : null;
                String facultad = (perfilCandidato != null) ? perfilCandidato.getFacultad() : null;
                String telefono = (perfilCandidato != null) ? perfilCandidato.getTelefono() : null;
                String foto = (perfilCandidato != null) ? perfilCandidato.getUrlFotoPerfil() : null;

                MatchRecomendacionDTO dto = MatchRecomendacionDTO.builder()
                        .publicacionId(candidato.getId())
                        .tipo(candidato.getTipo())
                        .titulo(candidato.getTitulo())
                        .descripcion(candidato.getDescripcion())
                        .nombreOferente(nombreOferente)
                        .correoOferente(oferente.getCorreo())
                        .carrera(carrera)
                        .facultad(facultad)
                        .telefono(telefono)
                        .fotoPerfil(foto)
                        .categoriaNombre(candidato.getCategoria() != null ? candidato.getCategoria().getNombre() : null)
                        .tipoRecursoNombre(candidato.getTipoRecurso() != null ? candidato.getTipoRecurso().getNombre() : null)
                        .cantidadDisponible(candidato.getCantidadDisponible())
                        .disponibilidadHoraria(candidato.getDisponibilidadHoraria())
                        .porcentajeCompatibilidad(score)
                        .etiquetaCompatibilidad(etiqueta)
                        .factoresCoincidencia(factores)
                        .build();

                resultados.add(dto);
            }
        }

        // Criterio 2: Ordenar de mayor a menor afinidad y limitar al Top 3 (o topK)
        resultados.sort((a, b) -> Double.compare(b.getPorcentajeCompatibilidad(), a.getPorcentajeCompatibilidad()));

        int limite = (topK > 0) ? Math.min(topK, resultados.size()) : Math.min(3, resultados.size());
        return resultados.stream().limit(limite).collect(Collectors.toList());
    }

    private double evaluarCompatibilidad(Publicacion solicitud, PerfilUsuario perfilSol,
                                         Publicacion oferta, PerfilUsuario perfilOf,
                                         List<String> factores) {
        double score = 35.0; // Puntuación base solidaria en la UAGRM

        // 1. Coincidencia de Categoría (Peso alto: +35%)
        if (solicitud.getCategoria() != null && oferta.getCategoria() != null) {
            if (solicitud.getCategoria().getId().equals(oferta.getCategoria().getId())) {
                score += 35.0;
                factores.add("Misma categoría de ayuda: " + solicitud.getCategoria().getNombre());
            } else if (oferta.getCategoriasSecundarias().stream()
                    .anyMatch(c -> c.getId().equals(solicitud.getCategoria().getId()))) {
                score += 20.0;
                factores.add("Coincide en especialidad secundaria de ayuda");
            }
        }

        // 2. Afinidad Académica UAGRM (Facultad y Carrera: +15%)
        if (perfilSol != null && perfilOf != null) {
            if (perfilSol.getFacultad() != null && perfilOf.getFacultad() != null
                    && perfilSol.getFacultad().equalsIgnoreCase(perfilOf.getFacultad())) {
                score += 10.0;
                factores.add("Misma facultad universitaria: " + perfilSol.getFacultad());

                if (perfilSol.getCarrera() != null && perfilOf.getCarrera() != null
                        && perfilSol.getCarrera().equalsIgnoreCase(perfilOf.getCarrera())) {
                    score += 5.0;
                    factores.add("Compañeros de la carrera de " + perfilSol.getCarrera());
                }
            }
        }

        // 3. Coincidencia de Recurso o Disponibilidad (+10%)
        if (oferta.getTipo() == TipoPublicacion.DONACION && oferta.getTipoRecurso() != null) {
            score += 10.0;
            factores.add("Disponibilidad de insumo: " + oferta.getTipoRecurso().getNombre());
        } else if (oferta.getTipo() == TipoPublicacion.VOLUNTARIADO && oferta.getDisponibilidadHoraria() != null) {
            score += 10.0;
            factores.add("Horario disponible: " + oferta.getDisponibilidadHoraria());
        }

        // 4. Similitud Semántica y Palabras Clave (+5%)
        double similitudTextual = calcularSimilitudTextual(solicitud.getTitulo() + " " + solicitud.getDescripcion(),
                oferta.getTitulo() + " " + oferta.getDescripcion());
        if (similitudTextual > 0.15) {
            score += 5.0;
            factores.add("Alta coincidencia semántica en descripción");
        }

        return Math.min(score, 98.5); // Calibración de techo al 98.5%
    }

    private double calcularSimilitudTextual(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return 0.0;

        Set<String> palabras1 = Arrays.stream(texto1.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());

        Set<String> palabras2 = Arrays.stream(texto2.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0;

        Set<String> interseccion = new HashSet<>(palabras1);
        interseccion.retainAll(palabras2);

        Set<String> union = new HashSet<>(palabras1);
        union.addAll(palabras2);

        return (double) interseccion.size() / union.size();
    }
}
