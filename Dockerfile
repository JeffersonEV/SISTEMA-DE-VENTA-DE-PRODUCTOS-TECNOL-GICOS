# =========================================================
# TechStore - Backend (Spring Boot 4 / Java 21)
# Imagen multi-etapa: compila con JDK y ejecuta con JRE.
# =========================================================

# ---------- Etapa 1: compilación ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Wrapper de Maven y descriptor de dependencias (se cachean si no cambian)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

# Código fuente y empaquetado (se omiten los tests: ya corren en CI/local)
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios
RUN groupadd -r techstore && useradd -r -g techstore techstore

COPY --from=build /app/target/*.jar app.jar
RUN chown -R techstore:techstore /app
USER techstore

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
