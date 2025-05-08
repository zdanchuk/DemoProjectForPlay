Zadanie

Hipotetyczny operator telekomunikacyjny dogadał się ze związkiem banków, że będzie zapobiegał otrzymywaniu phishingowych wiadomości tekstowych przez abonentów sieci, którzy wyrazili na to chęć. Stwórz proste rozwiązanie, które będzie służyło temu celowi.

Założenia
Operator przechowuje SMSy w postaci JSON, przykładowy rekord ma postać:

{
  “sender”: “234100200300”
  “recipient”: “48700800999”
  “message”: “Dzień dobry. W związku z audytem nadzór finansowy w naszym banku proszą o potwierdzanie danych pod adresem: https://www.m-bonk.pl.ng/personal-data”
}

Wymyśl rozwiązanie, które będzie obsługiwać wszystkie SMSy, wykrywać phishing i odrzucać takie przypadki

Do oceny czy URL wskazuje na stronę phishingową powinieneś użyć zewnętrznego serwisu dostępnego po HTTP. Serwis ten jest płatny za każde wywołanie. Możesz przyjąć, że serwis wygląda tak: https://cloud.google.com/web-risk/docs/reference/rest/v1eap1/TopLevel/evaluateUri
Pełna obsługa tego interfejsu nie jest jednak wymagana na potrzeby tego zadania. W szczególności nie jest wymagane uzyskanie tokenu autoryzacyjnego, który może być parametrem konfiguracyjnym.

Użytkownicy wyrażają chęć skorzystania lub rezygnacji z usługi przez wysłanie SMS o treści START lub STOP na określony numer.


Wymagania techniczne
Rozwiązanie należy stworzyć używając dowolnego języka JVM - Java/Kotlin/Scala. Powinno składać się z publicznego repozytorium kodu źródłowego w serwisie GitHub. Rozwiązanie powinno budować obraz(y) wykonywalny(e), przechowywany(e) w serwisie Docker Hub wraz z krótkim opisem potrzebnym do uruchomienia. Cały kod umieść w nowym pull request (by ułatwić nam review).
Jeśli przyjąłeś jakieś dodatkowe założenia umieść je w pliku README. Jednym z ocenianych elementów, jest przyjęta architektura rozwiązania. Przy decyzji o wyborze architektury, załóż, że masz pełną swobodę decyzji, nie jesteś ograniczony istniejącymi rozwiązaniami. Decyzje architektoniczne także opisz w README.
