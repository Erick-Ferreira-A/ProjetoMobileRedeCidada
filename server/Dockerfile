# Imagem do servidor Rede Cidadã para produção (Railway ou qualquer host com Docker).
# Build a partir da RAIZ do repositório:  docker build -f server/Dockerfile .

# ---------- 1) Compilação ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY gradlew gradle.properties build.gradle.kts ./
COPY gradle ./gradle
COPY server ./server

# Só o módulo do servidor: o app Android exigiria o Android SDK dentro da imagem.
RUN printf '%s\n' \
    'pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }' \
    'plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }' \
    'dependencyResolutionManagement { repositories { google(); mavenCentral() } }' \
    'rootProject.name = "ProjetoMobileRedeCidada"' \
    'include(":server")' > settings.gradle.kts \
 && chmod +x gradlew \
 && ./gradlew :server:installDist --no-daemon --no-configuration-cache -x test

# ---------- 2) Execução ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/server/build/install/server /app

# Memória enxuta: o Railway cobra por uso, e isto mantém o servidor perto de 250 MB.
ENV JAVA_OPTS="-Xmx192m -Xss512k -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8"

# O banco é Postgres (variável DATABASE_URL, definida no Railway).
# Fotos e vídeos ficam em /data/uploads: monte um volume persistente em /data.
ENV APP_ENV=production \
    TRUST_PROXY=true \
    SEED_DEMO_DATA=false \
    MEDIA_DIR=/data/uploads

EXPOSE 8080
CMD ["/app/bin/server"]
