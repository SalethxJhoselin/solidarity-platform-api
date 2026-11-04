--liquibase formatted sql

--changeset uagrm:21-cleanup-test-data
-- Eliminar datos de prueba creados durante el desarrollo y testing
-- Solo se conservan los datos de administradores institucionales legítimos

-- 1. Eliminar usuarios de prueba demo (dominios no institucionales)
-- Los usuarios institucionales @uagrm.edu.bo se conservan
DELETE FROM trazabilidad_solicitudes
WHERE solicitud_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM auditoria_publicaciones
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM reportes_contenido
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
) OR denunciante_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM comentarios
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
) OR usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM archivos_adjuntos
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM donaciones_concretadas
WHERE solicitud_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
) OR donante_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM disponibilidades_voluntario
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM publicaciones_categorias
WHERE publicacion_id IN (
    SELECT p.id FROM publicaciones p
    JOIN usuarios u ON p.usuario_id = u.id
    WHERE u.correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM publicaciones
WHERE usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM validaciones_academicas
WHERE usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM tokens_recuperacion_contrasena
WHERE usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM usuarios_roles
WHERE usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM perfiles_usuario
WHERE usuario_id IN (
    SELECT id FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo'
);

DELETE FROM usuarios WHERE correo LIKE '%@demo.uagrm.bo';

--changeset uagrm:22-remove-role-estudiante
-- El documento del proyecto (HU6) define solo 4 roles:
-- Administrador, Solicitante, Voluntario, Donante
-- ROLE_ESTUDIANTE no forma parte del diseño del sistema
DELETE FROM usuarios_roles
WHERE rol_id = (SELECT id FROM roles WHERE nombre = 'ROLE_ESTUDIANTE');

DELETE FROM roles WHERE nombre = 'ROLE_ESTUDIANTE';

--changeset uagrm:23-cleanup-orphaned-audit-data
-- Limpiar registros de auditoría huérfanos de publicaciones eliminadas
DELETE FROM auditoria_publicaciones
WHERE publicacion_id NOT IN (SELECT id FROM publicaciones);

DELETE FROM trazabilidad_solicitudes
WHERE solicitud_id NOT IN (SELECT id FROM publicaciones);

--changeset uagrm:24-cleanup-orphaned-reports
-- Limpiar reportes de contenido huérfanos
DELETE FROM reportes_contenido
WHERE publicacion_id IS NOT NULL
  AND publicacion_id NOT IN (SELECT id FROM publicaciones);

DELETE FROM reportes_contenido
WHERE comentario_id IS NOT NULL
  AND comentario_id NOT IN (SELECT id FROM comentarios);

--changeset uagrm:25-add-trgm-extension
-- Habilitar extensión pg_trgm para búsqueda por similitud textual (HU16)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

--changeset uagrm:26-optimize-db-indexes
-- Índices de rendimiento para las consultas más frecuentes del sistema

-- Búsqueda de publicaciones por tipo y estado (HU16: búsqueda y filtrado)
CREATE INDEX IF NOT EXISTS idx_publicaciones_tipo_estado
    ON publicaciones (tipo, estado, activo);

-- Búsqueda de publicaciones por usuario (HU13: mis publicaciones)
CREATE INDEX IF NOT EXISTS idx_publicaciones_usuario
    ON publicaciones (usuario_id, fecha_creacion DESC);

-- Búsqueda de publicaciones por categoría (HU16: filtro por categoría)
CREATE INDEX IF NOT EXISTS idx_publicaciones_categoria
    ON publicaciones (categoria_id, activo);

-- Búsqueda full-text en publicaciones (HU16: búsqueda por texto)
CREATE INDEX IF NOT EXISTS idx_publicaciones_titulo_trgm
    ON publicaciones USING gin (titulo gin_trgm_ops);

-- Índice para reportes por estado (HU19: bandeja de reportes)
CREATE INDEX IF NOT EXISTS idx_reportes_estado
    ON reportes_contenido (estado_reporte, fecha_creacion DESC);

-- Índice para tokens de recuperación (HU3: recuperar contraseña)
CREATE INDEX IF NOT EXISTS idx_tokens_hash
    ON tokens_recuperacion_contrasena (hash_token);

CREATE INDEX IF NOT EXISTS idx_tokens_usuario_expiracion
    ON tokens_recuperacion_contrasena (usuario_id, fecha_expiracion);

-- Índice para validaciones académicas por usuario (HU5: validación pertenencia)
CREATE INDEX IF NOT EXISTS idx_validaciones_usuario
    ON validaciones_academicas (usuario_id, estado_validacion);

