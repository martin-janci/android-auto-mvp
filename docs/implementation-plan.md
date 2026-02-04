# MVP Plan: Android Auto Call Task Manager

## Scope (Absolute Minimum)

- **Mobile app** (Kotlin) - žiadny backend server
- **CSV import** taskov
- **2 Android Auto screeny** (ListTemplate + PaneTemplate)
- **LLM prioritizácia** (OpenAI API priamo z appky)

---

## Architektúra

```
┌─────────────────────────────────────┐
│         ANDROID AUTO                │
│  ListTemplate → PaneTemplate        │
│       ↓ CALL      ↓ DONE            │
└─────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│         MOBILE APP                  │
│  - CSV Import                       │
│  - Task List (Compose)              │
│  - Room Database                    │
│  - OpenAI API Client                │
└─────────────────────────────────────┘
```

---

## Štruktúra projektu

```
app/src/main/java/com/example/calltasks/
├── CallTasksApplication.kt        # Koin initialization
├── di/
│   └── AppModules.kt              # Koin modules
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room DB
│   │   ├── TaskDao.kt             # DAO
│   │   └── TaskEntity.kt          # Entity
│   ├── repository/
│   │   └── TaskRepository.kt
│   └── csv/
│       └── CsvImporter.kt
├── domain/
│   └── model/
│       └── Task.kt                # Domain model
├── ai/
│   ├── OpenAiClient.kt            # OpenAI API
│   └── TaskPrioritizer.kt         # LLM logic
├── auto/
│   ├── CallTasksCarAppService.kt  # Entry point
│   ├── CallTasksSession.kt
│   └── screens/
│       ├── TaskListScreen.kt      # ListTemplate
│       └── TaskDetailScreen.kt    # PaneTemplate
└── mobile/
    ├── MainActivity.kt
    ├── ui/MainScreen.kt           # Compose UI
    └── viewmodel/MainViewModel.kt
```

---

## Implementačné kroky

### Fáza 1: Project Setup
1. Vytvoriť Android projekt (minSdk 29, Kotlin)
2. Pridať dependencies:
   - `androidx.car.app:app:1.7.0-rc01`
   - `androidx.room:room-ktx:2.6.1`
   - `com.aallam.openai:openai-client:4.0.1`
   - `com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3`
   - `io.insert-koin:koin-android:3.5.3`
   - `io.insert-koin:koin-androidx-compose:3.5.3`
   - Jetpack Compose
3. Konfigurovať AndroidManifest.xml

### Fáza 2: Data Layer
4. `TaskEntity.kt` - Room entity (id, name, phone, description, priority, isCompleted)
5. `TaskDao.kt` - CRUD operácie
6. `AppDatabase.kt` - Room database
7. `TaskRepository.kt` - repository pattern

### Fáza 3: CSV Import
8. `CsvImporter.kt` - parsing CSV (name, phone, description, notes)
9. Integrácia s file picker v mobile UI

### Fáza 4: Mobile UI (Minimal)
10. `MainViewModel.kt` - state management
11. `MainScreen.kt` - Compose UI s:
    - Import CSV button
    - Prioritize button
    - Task list (readonly)
12. `MainActivity.kt` - file picker launcher

### Fáza 5: Android Auto Screens
13. `CallTasksCarAppService.kt` - CarAppService
14. `CallTasksSession.kt` - Session
15. `TaskListScreen.kt` - ListTemplate (max 6 položiek):
    - Meno + popis
    - Priority ikona
    - onClick → detail
16. `TaskDetailScreen.kt` - PaneTemplate:
    - Telefón + popis
    - CALL button → dialer
    - DONE button → mark complete
17. `PhoneDialerHelper.kt` - ACTION_DIAL intent

### Fáza 6: LLM Integration
18. `OpenAiClient.kt` - API wrapper (gpt-3.5-turbo)
19. `TaskPrioritizer.kt` - prioritizácia + fallback heuristika

---

## CSV Formát

```csv
name,phone,description,notes
Jan Novak,+421905123456,Invoice follow-up,VIP client
Peter Horak,+421905654321,Contract renewal,Urgent
```

---

## Kľúčové rozhodnutia

| Rozhodnutie | Voľba | Dôvod |
|-------------|-------|-------|
| DI | Koin | Lightweight, Kotlin-first, easy testing |
| OpenAI model | gpt-3.5-turbo | Lacnejšie, rýchle |
| Phone action | ACTION_DIAL | Nepotrebuje permission |
| Mobile UI | Compose | Menej boilerplate |
| API key | BuildConfig | MVP only |

---

## Testovanie

1. **Desktop Head Unit (DHU)** pre Android Auto testovanie
2. Test cases:
   - CSV import (valid/invalid)
   - Android Auto list (max 6 items)
   - CALL button → dialer
   - DONE button → návrat do listu
   - LLM prioritizácia + fallback

---

## Verifikácia

Po implementácii overiť:

1. [ ] CSV import funguje v mobile app
2. [ ] Tasky sa zobrazujú v Android Auto (DHU)
3. [ ] Kliknutie na task otvára detail
4. [ ] CALL button otvára dialer s číslom
5. [ ] DONE označí task ako vybavený
6. [ ] LLM prioritizácia zoradí tasky
7. [ ] Fallback funguje pri API chybe

---

## Súbory na vytvorenie (~15 súborov)

**Critical:**
- `app/build.gradle.kts`
- `AndroidManifest.xml`
- `CallTasksCarAppService.kt`
- `TaskListScreen.kt`
- `TaskDetailScreen.kt`
- `TaskDao.kt`
- `OpenAiClient.kt`
