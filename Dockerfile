
# ЭТАП 1: BUILD (СБОРКА)
# Используем полный JDK для компиляции и сборки JAR-файла
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл pom.xml для загрузки зависимостей (используется кэш Docker)
COPY pom.xml .

# Загружаем зависимости (этот шаг кэшируется, если pom.xml не меняется)
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Запускаем сборку приложения
# Флаг -DskipTests пропускает тесты для ускорения сборки образа
RUN mvn package -DskipTests

# ЭТАП 2: RUN (ЗАПУСК)
# Используем минимальный JRE для запуска готового JAR-файла
# Выбираем минимальный образ только с JRE (runtime environment)
# slim-jre обеспечивает небольшой размер образа
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Определяем порт, который слушает приложение
EXPOSE 8080

# Копируем готовый JAR-файл с этапа 'builder' в текущий образ
# Используем ENV для имени JAR, чтобы было удобно менять
ARG JAR_FILE=collectionofrecipes-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/target/${JAR_FILE} app.jar

# Команда для запуска приложения (в контейнере)
ENTRYPOINT ["java", "-jar", "app.jar"]