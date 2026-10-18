--liquibase formatted sql

--changeset uagrm:09-create-archivos-adjuntos
CREATE TABLE archivos_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    publicacion_id BIGINT NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(255) NOT NULL UNIQUE,
    tipo_mime VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    url_descarga VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_archivos_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE
);

--changeset uagrm:10-create-comentarios
CREATE TABLE comentarios (
    id BIGSERIAL PRIMARY KEY,
    publicacion_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comentarios_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

--changeset uagrm:11-create-donaciones-concretadas
CREATE TABLE donaciones_concretadas (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    donante_id BIGINT NOT NULL,
    cantidad_entregada INTEGER,
    observaciones TEXT,
    fecha_entrega TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_donaciones_solicitud FOREIGN KEY (solicitud_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_donaciones_donante FOREIGN KEY (donante_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

--changeset uagrm:12-create-reportes-contenido
CREATE TABLE reportes_contenido (
    id BIGSERIAL PRIMARY KEY,
    denunciante_id BIGINT NOT NULL,
    publicacion_id BIGINT,
    comentario_id BIGINT,
    motivo VARCHAR(50) NOT NULL,
    detalles TEXT,
    estado_reporte VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_REVISION, APROBADO_SANCIONADO, DESCARTADO
    resolucion_admin TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP,
    CONSTRAINT fk_reportes_denunciante FOREIGN KEY (denunciante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_reportes_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_reportes_comentario FOREIGN KEY (comentario_id) REFERENCES comentarios(id) ON DELETE CASCADE
);

--changeset uagrm:13-create-performance-indexes-sprint3
CREATE INDEX idx_publicaciones_filtros ON publicaciones (tipo, estado, activo, fecha_creacion DESC);
CREATE INDEX idx_comentarios_publicacion ON comentarios (publicacion_id, activo, fecha_creacion ASC);
CREATE INDEX idx_reportes_estado ON reportes_contenido (estado_reporte, fecha_creacion DESC);
CREATE INDEX idx_donaciones_solicitud ON donaciones_concretadas (solicitud_id, fecha_entrega DESC);
