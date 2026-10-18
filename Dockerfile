# ==========================================
# Etapa 1: Compilación con Maven y Temurin 21
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache de dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilación de la aplicación
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen de Producción Ligera (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario sin privilegios de root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio para archivos adjuntos
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copiar el artefacto generado
COPY --from=builder /build/target/*.jar /app/app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring

# Configuración optimizada de memoria para el plan gratuito de Render (512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
