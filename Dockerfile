FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=/app/lib
COPY src ./src
COPY web ./web
COPY seed.json ./seed.json
RUN mkdir build && javac --release 11 -encoding UTF-8 -d build src/*.java && jar --create --file app.jar --main-class Servidor -C build . web seed.json
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar
COPY --from=build /app/lib /app/lib
RUN useradd --system --uid 10001 appuser && chown -R appuser /app
USER appuser
ENV PORT=10000
EXPOSE 10000
CMD ["java","-Xms32m","-Xmx256m","-cp","app.jar:lib/*","Servidor"]
