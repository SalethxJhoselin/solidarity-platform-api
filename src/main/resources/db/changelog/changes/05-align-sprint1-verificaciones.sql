--liquibase formatted sql

--changeset uagrm:17-align-sprint1-verificaciones
ALTER TABLE validaciones_academicas 
    ADD COLUMN IF NOT EXISTS tipo_miembro VARCHAR(30),
    ADD COLUMN IF NOT EXISTS identificador_institucional VARCHAR(100),
    ADD COLUMN IF NOT EXISTS tipo_verificacion VARCHAR(40),
    ADD COLUMN IF NOT EXISTS verificado_por_usuario_id BIGINT,
    ADD COLUMN IF NOT EXISTS fecha_verificacion TIMESTAMP;

--changeset uagrm:18-add-fk-verificado-por
ALTER TABLE validaciones_academicas
    DROP CONSTRAINT IF EXISTS fk_validaciones_verificado_por;

ALTER TABLE validaciones_academicas
    ADD CONSTRAINT fk_validaciones_verificado_por
    FOREIGN KEY (verificado_por_usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL;

--changeset uagrm:19-sync-existing-verification-data
UPDATE validaciones_academicas 
SET tipo_miembro = tipo 
WHERE tipo_miembro IS NULL;

UPDATE validaciones_academicas 
SET identificador_institucional = registro_uagrm 
WHERE identificador_institucional IS NULL;

UPDATE validaciones_academicas 
SET tipo_verificacion = CASE 
    WHEN estado_validacion = 'VALIDADO' THEN 'CORREO_INSTITUCIONAL'
    ELSE 'DOCUMENTO_MANUAL'
END 
WHERE tipo_verificacion IS NULL;

--changeset uagrm:20-create-view-verificaciones-usuario
CREATE OR REPLACE VIEW verificaciones_usuario AS 
SELECT 
    id,
    usuario_id,
    COALESCE(tipo_miembro, tipo) AS tipo_miembro,
    COALESCE(identificador_institucional, registro_uagrm) AS identificador_institucional,
    tipo_verificacion,
    estado_validacion AS estado,
    verificado_por_usuario_id,
    fecha_verificacion,
    fecha_creacion
FROM validaciones_academicas;
