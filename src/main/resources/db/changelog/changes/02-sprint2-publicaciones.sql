--liquibase formatted sql

--changeset uagrm:03-create-categorias-publicacion
CREATE TABLE categorias_publicacion (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    icono VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_categoria_nombre_lower UNIQUE (nombre)
);

--changeset uagrm:04-create-tipos-recurso
CREATE TABLE tipos_recurso (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'Unidades',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tipo_recurso_nombre_lower UNIQUE (nombre)
);

--changeset uagrm:05-create-publicaciones
CREATE TABLE publicaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL, -- NECESIDAD, VOLUNTARIADO, DONACION, ACTIVIDAD_COLECTIVA
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria_id BIGINT,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTA', -- ABIERTA, EN_PROCESO, RESUELTA, FINALIZADA, CANCELADA, BLOQUEADA
    activo BOOLEAN NOT NULL DEFAULT TRUE, -- Soft delete
    
    -- Campos específicos de Donación
    tipo_recurso_id BIGINT,
    cantidad_total INTEGER,
    cantidad_disponible INTEGER,
    
    -- Campos específicos de Actividad Colectiva
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    lugar VARCHAR(255),
    
    -- Campos específicos de Voluntariado
    disponibilidad_horaria VARCHAR(255),

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_publicaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_publicaciones_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_publicacion(id) ON DELETE RESTRICT,
    CONSTRAINT fk_publicaciones_tipo_recurso FOREIGN KEY (tipo_recurso_id) REFERENCES tipos_recurso(id) ON DELETE RESTRICT
);

--changeset uagrm:06-create-publicaciones-categorias
CREATE TABLE publicaciones_categorias (
    publicacion_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (publicacion_id, categoria_id),
    CONSTRAINT fk_pubcat_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_pubcat_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_publicacion(id) ON DELETE CASCADE
);

--changeset uagrm:07-create-disponibilidades-voluntario
CREATE TABLE disponibilidades_voluntario (
    id BIGSERIAL PRIMARY KEY,
    publicacion_id BIGINT NOT NULL,
    dia_semana VARCHAR(30) NOT NULL, -- LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO, FINES_DE_SEMANA
    turno VARCHAR(30) NOT NULL, -- MANANA, TARDE, NOCHE, COMPLETO
    CONSTRAINT fk_disp_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones(id) ON DELETE CASCADE
);

--changeset uagrm:08-insert-seed-data-sprint2
INSERT INTO categorias_publicacion (nombre, descripcion, icono, activo) VALUES
('Apoyo Económico', 'Ayuda financiera solidaria para matrícula, transporte o emergencias estudiantiles', 'FaMoneyBillWave', true),
('Tutoría Académica', 'Clases de reforzamiento, asesoría en materias troncales y proyectos', 'FaGraduationCap', true),
('Salud y Medicamentos', 'Apoyo con medicamentos básicos, recetas o insumos de primeros auxilios', 'FaHeartbeat', true),
('Alimentación y Víveres', 'Canastas solidarias, almuerzos y alimentos no perecederos', 'FaUtensils', true),
('Material de Estudio y Libros', 'Libros de texto, fotocopias, calculadoras y útiles universitarios', 'FaBook', true),
('Apoyo Psicológico y Bienestar', 'Escucha activa y orientación entre pares estudiantiles', 'FaUserFriends', true);

INSERT INTO tipos_recurso (nombre, descripcion, unidad_medida, activo) VALUES
('Víveres No Perecederos', 'Arroz, fideo, avena, aceite, enlatados y azúcar', 'Kilogramos', true),
('Libros Universitarios', 'Textos guía de ingeniería, ciencias exactas, humanidades y salud', 'Unidades', true),
('Cuadernos y Útiles', 'Hojas, carpetas, bolígrafos, lápices y calculadoras científicas', 'Paquetes', true),
('Ropa e Indumentaria', 'Abrigos, poleras y ropa en buen estado para estudiantes', 'Piezas', true),
('Medicamentos Básicos', 'Paracetamol, analgésicos, gasas y alcohol medicinal', 'Cajas', true),
('Aporte Monetario Directo', 'Fondos directos para pasajes o gastos médicos urgentes', 'Bolivianos', true);
