# MVP Analýza: AI Call Task Manager pre Android Auto

## 1. Vízia produktu

**Čo to je:** Appka pre obchodníkov/salesákov, ktorá počas jazdy inteligentne navrhuje koho volať a umožňuje jedným klikom/hlasom spustiť hovor.

**Core value proposition:** "Premení čas strávený v aute na produktívne obvolávanie klientov."

---

## 2. Cieľová skupina (MVP)

- Obchodní zástupcovia v teréne
- Sales tímy s veľkým počtom klientov
- Realitní makléri
- Poisťovací agenti
- Ktokoľvek kto trávi veľa času v aute a potrebuje volať

---

## 3. Core Features (MVP scope)

### 3.1 Hlavný screen - ListTemplate

```
┌─────────────────────────────┐
│  📞 Call Tasks              │
├─────────────────────────────┤
│ ⭐ Jan Novák                │
│    Invoice follow-up        │
│    [CALL]                   │
├─────────────────────────────┤
│ 🔴 Peter Horák              │
│    Contract renewal         │
│    [CALL]                   │
├─────────────────────────────┤
│    Maria Kováčová           │
│    New lead                 │
│    [CALL]                   │
└─────────────────────────────┘
```

**Položky zoradené podľa AI priority:**
- ⭐ = AI odporúča (najvyššia priorita)
- 🔴 = urgentné (deadline)
- bez ikony = normálna priorita

### 3.2 Detail screen - PaneTemplate

```
┌─────────────────────────────┐
│  Jan Novák                  │
│  +421 905 123 456           │
│                             │
│  Invoice follow-up          │
│  Last contact: 3 days ago   │
│                             │
│  [📞 CALL]  [✅ DONE]       │
│  [⏭ SKIP]   [📝 NOTE]      │
└─────────────────────────────┘
```

### 3.3 Hlasové príkazy (App Actions)

| Príkaz | Akcia |
|--------|-------|
| "Hey Google, show my calls" | Otvorí ListTemplate |
| "Call next task" | Zavolá #1 v zozname |
| "Who should I call?" | AI odpovie + ponúkne hovor |
| "Skip this one" | Presunie na koniec |
| "Mark as done" | Označí ako vybavené |

### 3.4 AI prioritizácia (backend)

**Vstupné faktory:**
- Čas od posledného kontaktu
- Deadline/urgencia tasku
- Hodnota klienta (deal size)
- Optimálny čas na volanie (business hours)
- Historická úspešnosť (kedy klient odpovedá)

**Výstup:**
- Zoradený zoznam
- Krátke odôvodnenie ("Best time to reach")

---

## 4. Technická architektúra (MVP)

### 4.1 Komponenty

```
┌─────────────────────────────────────────┐
│            ANDROID AUTO                  │
│  ┌─────────────────────────────────┐    │
│  │   Car App Library               │    │
│  │   - ListTemplate                │    │
│  │   - PaneTemplate                │    │
│  │   - Voice Actions               │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│         MOBILE APP (Kotlin)              │
│  ┌─────────────────────────────────┐    │
│  │   CarAppService                 │    │
│  │   Task Repository               │    │
│  │   Phone Dialer Integration      │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│            BACKEND API                   │
│  ┌─────────────────────────────────┐    │
│  │   Task CRUD                     │    │
│  │   AI Priority Engine            │    │
│  │   User Authentication           │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

### 4.2 Tech Stack (odporúčanie)

**Android:**
- Kotlin
- Jetpack Car App Library
- Room (local cache)
- Retrofit (API)

**Backend:**
- Node.js / Python FastAPI
- PostgreSQL
- OpenAI API (prioritizácia)

**Integrácie (post-MVP):**
- CRM (Salesforce, HubSpot, Pipedrive)
- Google Calendar
- Kontakty

---

## 5. User Flow (MVP)

```
[Nastúpi do auta]
       │
       ▼
[Pripojí sa Android Auto]
       │
       ▼
[Otvorí appku / hlasový príkaz]
       │
       ▼
[Vidí prioritizovaný zoznam]
       │
       ├──► [Klikne na kontakt] ──► [Detail] ──► [CALL]
       │
       └──► [Povie "Call next"] ──► [Volá #1]
                   │
                   ▼
            [Po hovore]
                   │
       ┌───────────┴───────────┐
       │                       │
   [DONE]                  [SKIP]
       │                       │
       ▼                       ▼
[Odstráni z listu]    [Presunie na koniec]
       │                       │
       └───────────┬───────────┘
                   ▼
         [Ďalší kontakt]
```

---

## 6. MVP Scope - Čo JE a NIE JE

### ✅ MVP obsahuje:

1. **Android Auto app** s ListTemplate + PaneTemplate
2. **Zoznam úloh** (max 6-8)
3. **Jeden klik = hovor**
4. **Základné hlasové príkazy** (3-4)
5. **AI prioritizácia** (jednoduchá)
6. **Backend API** (CRUD + priority)
7. **Mobile app** pre správu taskov

### ❌ MVP NEOBSAHUJE:

1. CRM integrácie
2. Komplexné AI dialógy
3. Kalendár sync
4. Poznámky po hovore (len označenie done/skip)
5. Analytika
6. Team features
7. Offline mode (potrebuje internet)

---

## 7. Riziká a mitigácie

| Riziko | Pravdepodobnosť | Dopad | Mitigácia |
|--------|-----------------|-------|-----------|
| Google zamietne app | Stredná | Kritický | Striktne dodržať guidelines |
| AI odporúčania sú zlé | Stredná | Vysoký | Jednoduchý algoritmus na začiatok |
| Používatelia nechcú platiť | Vysoká | Vysoký | Freemium model |
| Konkurencia | Nízka | Stredný | First mover advantage |

---

## 8. Konkurenčná analýza

| App | Čo robí | Slabina |
|-----|---------|---------|
| Dialpad | VoIP + CRM | Nie je pre driving |
| Salesforce Mobile | CRM | Žiadna AA integrácia |
| HubSpot | CRM | Žiadna AA integrácia |
| **Naša app** | **Driving-first call tasks** | **Nový hráč** |

**Diferenciátor:** Jediná appka postavená PRIMÁRNE pre obvolávanie počas jazdy.

---

## 9. Monetizácia (návrh)

### Freemium model:

**Free:**
- 5 taskov denne
- Základná prioritizácia
- 1 hlasový príkaz

**Pro (9.99€/mesiac):**
- Neobmedzené tasky
- AI prioritizácia
- Všetky hlasové príkazy
- Sync s kontaktmi

**Business (29.99€/mesiac):**
- Všetko z Pro
- CRM integrácie
- Team dashboard
- Analytika

---

## 10. Časový odhad (MVP)

| Fáza | Trvanie |
|------|---------|
| Design & Planning | 1-2 týždne |
| Android Auto app | 3-4 týždne |
| Backend API | 2-3 týždne |
| AI priority engine | 1-2 týždne |
| Mobile companion app | 2-3 týždne |
| Testing & Polish | 2 týždne |
| Google Play review | 1-2 týždne |
| **TOTAL** | **12-18 týždňov** |

---

## 11. Ďalšie kroky

1. **Validácia nápadu** - rozhovory s cieľovou skupinou
2. **Prototyp** - Figma mockupy Android Auto screens
3. **Technical spike** - overiť Car App Library limity
4. **MVP development** - začať s Android Auto core
5. **Closed beta** - 10-20 sales ľudí

---

## 12. Otázky na rozhodnutie

1. **Ako sa budú tasky vytvárať?**
   - Manuálne v mobile app?
   - Import z CSV?
   - CRM sync (post-MVP)?

2. **Aký je minimálny AI scope?**
   - Jednoduchá formula (čas + urgencia)?
   - Alebo už od začiatku LLM?

3. **Potrebujeme mobile companion app v MVP?**
   - Alebo len web dashboard?

4. **Aká je cieľová cena?**
   - Freemium vs. paid-only?

---

## 13. Záver

MVP je **realisticky dosiahnuteľné** v rámci Android Auto limitov. Kľúčové je:

1. **Voice-first approach** - UI je len doplnok
2. **Jednoduchá AI** - neprekombinovat
3. **Jeden jasný use case** - obvolávanie, nič viac
4. **Striktné dodržanie guidelines** - inak zamietnutie

Odporúčam začať **technical spike** na Car App Library a paralelne **user research** s cieľovou skupinou.
