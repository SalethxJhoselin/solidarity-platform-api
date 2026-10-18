--liquibase formatted sql

--changeset uagrm:01-init-schema-tables
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    correo VARCHAR(255) NOT NULL UNIQUE,
    hash_contrasena VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE perfiles_usuario (
    usuario_id BIGINT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    descripcion TEXT,
    facultad VARCHAR(150),
    carrera VARCHAR(150),
    url_foto_perfil VARCHAR(500),
    telefono VARCHAR(30),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perfiles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE tokens_recuperacion_contrasena (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    hash_token VARCHAR(255) NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_uso TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE validaciones_academicas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    registro_uagrm VARCHAR(50) NOT NULL UNIQUE,
    tipo VARCHAR(30) NOT NULL, -- ESTUDIANTE, DOCENTE
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_validaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE permisos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE roles_permisos (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_roles_permisos_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

--changeset uagrm:02-insert-default-roles
INSERT INTO roles (nombre, descripcion) VALUES ('ROLE_ADMIN', 'Administrador del sistema');
INSERT INTO roles (nombre, descripcion) VALUES ('ROLE_SOLICITANTE', 'Usuario solicitante de extension o ayuda');
INSERT INTO roles (nombre, descripcion) VALUES ('ROLE_VOLUNTARIO', 'Usuario voluntario para proyectos');
INSERT INTO roles (nombre, descripcion) VALUES ('ROLE_DONANTE', 'Usuario que realiza donaciones');
