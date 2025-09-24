# Стейдж сборки слоёв Spring Boot
FROM openjdk:21 AS builder
WORKDIR /application
# Копируем только собранный JAR из target
COPY target/*.jar app.jar
# Распаковываем слои Spring Boot
RUN java -Djarmode=layertools -jar app.jar extract

# Рантайм-образ (минимальный JRE)
FROM eclipse-temurin:21-jre-alpine AS FINAL
# Абсолютный путь во избежание сюрпризов
WORKDIR /app

# Копируем слои строго в правильном порядке
COPY --from=BUILDER /application/dependencies/            ./
COPY --from=BUILDER /application/snapshot-dependencies/   ./
COPY --from=BUILDER /application/spring-boot-loader/      ./
COPY --from=BUILDER /application/application/             ./

# Запускаем под непривилегированным пользователем
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080
# JarLauncher доступен в распакованной структуре spring-boot-loader
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]
