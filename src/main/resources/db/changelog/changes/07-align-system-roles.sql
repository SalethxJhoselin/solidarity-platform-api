--liquibase formatted sql

--changeset uagrm:27-create-official-system-roles
-- Según la especificación oficial del proyecto (RF-03, HU6 y párrafo 456):
-- Los perfiles de acceso son: Usuario Visitante, Usuario Registrado, Moderador y Administrador general.
-- Solicitante, Voluntario y Donante son tipos de publicación, NO roles del sistema.

-- 1. Insertar roles institucionales oficiales si no existen
INSERT INTO roles (nombre, descripcion)
VALUES ('ROLE_USUARIO', 'Usuario Registrado de la comunidad universitaria UAGRM')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO roles (nombre, descripcion)
VALUES ('ROLE_MODERADOR', 'Moderador de contenido y reportes comunitarios')
ON CONFLICT (nombre) DO NOTHING;

-- 2. Asegurar que todos los usuarios registrados existentes tengan ROLE_USUARIO
INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
CROSS JOIN roles r
WHERE r.nombre = 'ROLE_USUARIO'
  AND NOT EXISTS (
      SELECT 1 FROM usuarios_roles ur
      WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
  );

-- 3. Eliminar asignaciones de roles espurios (Solicitante, Voluntario, Donante)
DELETE FROM usuarios_roles
WHERE rol_id IN (
    SELECT id FROM roles WHERE nombre IN ('ROLE_SOLICITANTE', 'ROLE_VOLUNTARIO', 'ROLE_DONANTE')
);

-- 4. Eliminar roles espurios de la tabla roles
DELETE FROM roles
WHERE nombre IN ('ROLE_SOLICITANTE', 'ROLE_VOLUNTARIO', 'ROLE_DONANTE');

