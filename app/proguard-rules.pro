# Regras do R8 para a versão de loja (release) do Rede Cidadã.
# As bibliotecas (Room, Compose, Ktor, Google Maps/Location) já trazem as próprias regras;
# aqui ficam só os ajustes que elas não cobrem.

# Mantém nome do arquivo e linha nos relatórios de erro do Play Console (Android vitals).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# O Ktor no Android referencia classes da JVM que não existem no Android (só são usadas no servidor).
-dontwarn java.lang.management.**
-dontwarn org.slf4j.**
-dontwarn io.ktor.util.debug.**
