# Testowy projekt Play

Zrealizowana przeze mnie usługa korzysta z bazy PostgreSQL do przechowywania informacji o klientach, statystyk wysłanych linków do weryfikacji dla każdego klienta oraz szablonów serwisowych SMS.

Do wysyłania SMS-ów informujących użytkownika o aktywacji/dezaktywacji usługi, a także wiadomości, które przeszły weryfikację, używam innej usługi, do której przesyłam JSON z polami sender, recipient i message.

Dla każdego użytkownika w bazie przechowywany jest status usługi Active/Inactive, liczba wysłanych linków do weryfikacji oraz liczba opłaconych weryfikacji.

Zaimplementowałem dodatkowy kontroler ClientController:

    GET /client/status – na podstawie numeru telefonu zwraca różnicę między liczbą wysłanych weryfikacji a liczbą opłaconych weryfikacji.

    POST /client/pay – ustawia liczba opłaconych weryfikacji = liczba wysłanych linków.

Główny POST /sms/receive odbiera SMS w formacie JSON. Jeśli odbiorcą jest jeden z numerów serwisowych, sprawdzamy, czy klient wysłał tekst START/STOP i aktywujemy/dezaktywujemy usługę dla użytkownika-wysyłającego. Pozostałe komendy są ignorowane.

Dla wszystkich innych numerów odbiorców usługa sprawdza ich status w bazie pod kątem aktywności usługi oraz obecności linku w wiadomości. Jeśli oba warunki są spełnione, wysyłamy link do zewnętrznej usługi weryfikującej pod kątem phishingu. Wysyłka odbywa się asynchronicznie przy użyciu CompletableFuture. Podstawą jest usługa Google do wykrywania zagrożeń. Link uznawany jest za zweryfikowany, jeśli odpowiedź z usługi zawiera poprawny kod 2xx oraz JSON nie zawiera poziomów zagrożenia o wysokim ryzyku. Jeśli link został pomyślnie zweryfikowany, JSON z wiadomością jest przekazywany do zewnętrznej usługi w celu dalszej wysyłki SMS.

W pliku konfiguracyjnym przechowywane są:

    adresy URL do usługi weryfikacyjnej i usługi wysyłki SMS,

    typy zagrożeń do weryfikacji,

    dopuszczalne poziomy zagrożeń (Safe, Low, Medium),

    numery serwisowe.

Budowanie projektu odbywa się w kontenerze Docker. Polecenie do budowania i uruchamiania z katalogu głównego projektu:

    docker-compose up --build

Polecenie do uruchomienia wcześniej zbudowanego projektu:

    docker-compose up
    
Przy działającym kontenerze bazy danych projekt można uruchomić poza kontenerem za pomocą mvnw lub jako aplikację Spring Boot bezpośrednio z IDE.


Możliwe ulepszenia:

1) Jeśli nie ma potrzeby przechowywania statystyk zapytań weryfikacyjnych dla poszczególnych użytkowników, a jedna globalna statystyka powinna być przypisana do całej usługi, to przechowywanie użytkowników można przenieść do bazy NoSQL (np. Redis) i tam przechowywać powiązania „numer telefonu” – „status usługi”. Szablony wiadomości SMS również można tam umieścić.
2) Można wydzielić wywołanie zewnętrznej usługi do osobnej metody oznaczonej @Cacheable i podłączyć do tego Redis lub Memcached, przechowując tam Request-Response przez krótki czas (np. do 12 godzin).

