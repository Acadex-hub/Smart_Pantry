# 🥕 Smart Pantry Manager

A Java Android app that reduces food waste by suggesting recipes **strictly** from the ingredients you already own — no shopping trip required.

## Features
- Pantry CRUD (name, quantity, unit, optional expiry date) with validation
- 18 pre-loaded recipes (seeded on first run)
- **Strict matching**: a recipe is suggested only if *every* ingredient is present in the required quantity
  - tolerant of plurals (`Tomatoes` = `tomato`) and units (`0.5 kg` covers `200 g`, `tbsp`/`tsp`/`cup` → ml)
- Recipes that use soon-to-expire food are ranked first
- Bonus: separate "Almost There" list (exactly one ingredient missing)
- Settings: expiring-soon alerts, show/hide Almost There

## Database choice: SQLite (SQLiteOpenHelper)
Local, zero-setup, works offline, and persists across app restarts. Recipes and pantry data are small and relational, so
three simple tables (`pantry`, `recipes`, `recipe_ingredients`) fit perfectly without needing a backend.

## Setup
1. Open the project folder in Android Studio (Koala or newer) and let Gradle sync.
2. Run on an emulator or device with Android 8.0+ (API 26).

No Google Maps, mapping SDKs or location services are used.
