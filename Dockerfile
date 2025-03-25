# === 1단계: 빌드 ===
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# 종속성 캐싱을 위해 먼저 복사
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# 전체 프로젝트 복사 및 빌드
COPY . .
RUN ./mvnw clean package -DskipTests

# === 2단계: 실행 ===
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/target/FixBot-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
