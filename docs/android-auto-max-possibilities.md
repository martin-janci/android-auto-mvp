# Android Auto – MAXIMÁLNE MOŽNOSTI (oficiálne, 2025/2026)

## 1. UI – tvrdý limit (žiadne obchádzky)

### Povolené šablóny

Môžeš použiť **iba** tieto typy:

* `ListTemplate`
* `PaneTemplate`
* `MessageTemplate`
* `NavigationTemplate`
* `SignInTemplate`

❌ **Nie je možné**

* custom layout
* vlastné komponenty
* drag & drop
* text input
* klávesnica
* scrolling mimo template

➡️ **Tvoj „custom screen" = kombinácia povolených templates**

---

## 2. Zoznam úloh – maximum čo sa dá

### `ListTemplate`

* max **6–8 položiek** (závisí od auta)
* každá položka:

  * title
  * 1–2 riadky textu
  * optional icon
  * primary action

### Každá položka môže:

* spustiť hovor
* otvoriť detail (`PaneTemplate`)
* vyvolať hlasovú odpoveď

➡️ Toto je **plný strop**

### `PaneTemplate` (detail)

* max 2–4 akcie
* iba ikony / krátke texty

Príklady:

* 🎙 „Next call"
* 📞 „Call now"
* ✅ „Done"

---

## 4. Hlas = hlavný vstup (tu je najväčší priestor)

### Čo Google POVOĽUJE:

* **Implicitné intents**
* **Custom App Actions**
* Kontextové hlasové príkazy

Príklady:

* „Hey Google, show my call tasks"
* „Call next task"
* „Mark this as done"
* „What should I call next?"

➡️ Hlas je **najsilnejší kanál**, UI je len doplnok.

---

## 5. AI – čo je reálne možné

### Povolené použitie AI

ÁNO:

* spracovanie hlasu (po Google ASR)
* sumarizácia
* priorita úloh
* odporúčanie ďalšej akcie
* generovanie krátkej hlasovej odpovede

NIE:

* generovať UI
* viesť dlhý dialóg
* autonómne rozhodovanie
* chatbot správanie

---

## 6. MAXIMÁLNA AI integrácia (bez porušenia pravidiel)

### Príklad reálneho flow:

1. User: „What's my next call?"
2. Google Assistant → tvoja appka
3. Backend AI:

   * vyhodnotí prioritu
   * vyberie najlepší task
4. Odpoveď: "Your next call is regarding invoice. Would you like to call now?"

➡️ **Áno / Nie** odpoveď je povolená.

---

## 7. Notifikácie – hranica

* iba **kontextové**
* krátke
* nevyrušujúce
* žiadne spamovanie

Príklad:

* „You have 2 pending calls."

---

## 8. Navigácia + úlohy (pokročilé, ale povolené)

Môžeš:

* počas navigácie
* ponúknuť „Call next task"

➡️ **Integrácia do jazdy je povolená**, ak nemeníš focus vodiča.

---

## 9. Čo je ABSOLÚTNY STROP

### Najsilnejší možný Android Auto app:

* hlasom ovládaný task manager
* dynamické poradie úloh (AI)
* kontextová reakcia (čas, poloha, kalendár)
* 1–2 kliky max
* zero typing

➡️ Všetko ostatné Google **zablokuje pri review**.

---

## 10. Google Play review – realita

Google zamietne ak:

* máš príliš veľa textu
* nútiš usera interagovať
* robíš „pseudo chat"
* máš vlastné UI triky

---

## 11. PM odporúčanie (stručne)

Ak chceš ísť **na maximum**:

* stavaj okolo **hlas + rozhodovací engine**
* UI je iba potvrdenie / navigácia
* AI pracuje na pozadí

---

## Ďalšie kroky

Možné pokračovanie:

* navrhnúť **konkrétny screen flow (template → template)**
* dať **checklist pre Google review**
* navrhnúť **AI decision engine (bez porušenia pravidiel)**
