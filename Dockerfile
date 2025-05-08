# Pobieramy oficjalny obraz OpenJDK 21 z minimalnym systemem
FROM openjdk:21-jdk-slim

# Ustawiamy katalog roboczy wewnątrz kontenera
WORKDIR /app

# Tworzymy katalog dla repozytorium Maven
RUN #mkdir -p /.m2

# Kopiujemy cały kod źródłowy projektu do kontenera
COPY . .

# Pobieramy zależności, sprawdzając najpierw lokalne repozytorium Maven
#RUN ./mvnw dependency:go-offline -Dmaven.repo.local=/root/.m2

# Kompilujemy aplikację
RUN ./mvnw clean package -Dmaven.repo.local=/.m2

# Uruchamiamy aplikację jako proces główny w kontenerze
CMD ["sh", "-c", "java -jar target/PlayDemo-*.jar"]