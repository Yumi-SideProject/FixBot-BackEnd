# === 1단계: 빌드 ===
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# 종속성 캐싱용
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 실행 권한 먼저 부여
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# 프로젝트 전체 복사
COPY . .

# 🔥 복사 후 다시 권한 부여 (여기 포인트!!)
RUN chmod +x mvnw

# 빌드
RUN ./mvnw clean package -DskipTests

# === 2단계: 실행 ===
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/FixBot-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
