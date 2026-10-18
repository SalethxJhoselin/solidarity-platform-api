--liquibase formatted sql

--changeset uagrm:14-create-auditoria-publicaciones
CREATE TABLE auditoria_publicaciones (
    id BIGSERIAL PRIMARY KEY,
    publicacion_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_accion VARCHAR(50) NOT NULL, -- CREACION, EDICION, CAMBIO_ESTADO, CANCELACION, BLOQUEO_ADMIN
    campo_modificado VARCHAR(100),
    valor_anterior TEXT,
    valor_nuevo TEXT,
    motivo VARCHAR(255),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

--changeset uagrm:15-create-trazabilidad-solicitudes
CREATE TABLE trazabilidad_solicitudes (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    estado_alcanzado VARCHAR(30) NOT NULL, -- ABIERTA, EN_PROCESO, RESUELTA, CANCELADA, BLOQUEADA
    descripcion_hito VARCHAR(255) NOT NULL,
    usuario_responsable_id BIGINT,
    fecha_hito TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trazabilidad_solicitud FOREIGN KEY (solicitud_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_trazabilidad_usuario FOREIGN KEY (usuario_responsable_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

--changeset uagrm:16-create-performance-indexes-sprint4
CREATE INDEX idx_auditoria_publicacion ON auditoria_publicaciones (publicacion_id, fecha_registro DESC);
CREATE INDEX idx_trazabilidad_solicitud ON trazabilidad_solicitudes (solicitud_id, fecha_hito ASC);
