
# Symulacja ruchu drogowego na skrzyżowaniu

Program symuluje ruch na skrzyżowaniu. Jest to skrzyżowanie, do którego prowadzą cztery drogi, przy każdej drodze znajdują się światła kontrolujące ruch pojazdów.
Symulacja jest kontrolowana komendami umieszczonymi w pliku JSON i wyniki symulacji w formie listy stanów po kolejnych krokach symulacji zapisuje do pliku JSON. 
## Działanie algorytmu świateł
Światła na zmianę świecą się na zielono i czerwono w parach dla drogi północnej i południowej oraz wschodniej i zachodniej. To w którym momencie światła zmienią kolor decyduje sumaryczna liczba pojazdów na drogach o tym samym kierunku. Gdy na drodze ze wschodu na zachód jest więcej świateł to te drogi mają zapalone zielone światło, analogicznie dla świateł przy drogach północ-południe. W przypadku gdy światła są zapalone w taki sam sposób przez zbyt długi czas to układ świateł odwraca się. 
## Uruchamianie programu

Zaczynając w ```./Symulacja-main/out/artifacts/TrafficLightsSimulation_jar/``` program jest uruchamiany następująco:

```
java -jar TrafficLightsSimulation.jar [opcje] [ścieżka do wejściowego pliku json] [ścieżka zapisania wyjściowego pliku json]
```

Możliwe opcje uruchamiania:\
```-v``` - uruchom program z graficzną reprezentacją symulacji
## Struktura pliku wejściowego

Pliki JSON powinny być zgodne z poniższym schematem, służącym do symulacji działań na pojazdach. Każdy plik zawiera pole `commands`, które jest tablicą obiektów określających kolejne kroki symulacji.

### Struktura Podstawowa
Każdy plik JSON powinien wyglądać następująco:

```json
{
  "commands": [
    // Kolejne obiekty-komendy
  ]
}
```

Pole `commands` zawiera listę komend, gdzie każda komenda ma określony format.

---

### Typy Komend

#### 1. Dodanie Pojazdu (`addVehicle`)
Komenda `addVehicle` służy do dodania nowego pojazdu do symulacji.

#### Format
```json
{
  "type": "addVehicle",
  "vehicleId": "<unikalny identyfikator pojazdu>",
  "startRoad": "<nazwa początkowej drogi>",
  "endRoad": "<nazwa docelowej drogi>"
}
```

#### Uwagi
- **`vehicleId`** – unikalny identyfikator pojazdu w całej symulacji.
- **`startRoad`** – nazwa drogi, od której zaczyna się ruch pojazdu.
- **`endRoad`** – nazwa drogi, na którą zmierza pojazd.
- możliwe wartości dla pól startRoad i endRoad to **"south"**, **"east"**, **"north"** i **"west"**
---

#### 2. Wykonanie Kroku Symulacji (`step`)
Komenda `step` oznacza wykonanie jednego kroku w symulacji.

#### Format
```json
{
  "type": "step"
}
```

---

### Przykładowy Plik JSON
Poniżej znajduje się kompletny przykład poprawnego pliku JSON:

```json
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "vehicle1",
      "startRoad": "south",
      "endRoad": "north"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle2",
      "startRoad": "north",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle3",
      "startRoad": "west",
      "endRoad": "south"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle4",
      "startRoad": "west",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    }
  ]
}
```
## Zrzuty ekranu

![App Screenshot](https://github.com/zie-w/Symulacja/blob/66b01de798d4b1b43c67a8681e36102e3ee352e9/screenshots/screenshot1.png)

