# Konzept & Implementierungsplan: Gebäude-Upgrades System ( Cookie Clicker inspiriert )

Dieses Dokument beschreibt die Erweiterung des Upgrade-Systems in **AerpClicker** durch neue, gestaffelte Gebäude-Upgrades ("Building Upgrades") und Synergien, angelehnt an die Verkaufs- und Meilenstein-Mechaniken von Cookie Clicker.

---

## 1. Konzeptuelle Inspiration & Mechanik

In Cookie Clicker zeichnen sich Gebäude-Upgrades durch folgende Kernpunkte aus:
1. **Stufen-/Meilenstein-Upgrades (Tiers):**
   - Sobald ein Gebäude ein bestimmtes Level / eine bestimmte Anzahl erreicht (z. B. Level 1, 5, 10, 25, 50, 100), wird ein **einmaliges Tier-Upgrade** im Shop freigeschaltet.
   - Jedes Tier-Upgrade verdoppelt die Effizienz des jeweiligen Gebäudes ($2\times$ Produktion oder halbiertes Intervall) oder schaltet eine besondere Funktion frei.
2. **Thematische Benennung & Lore:**
   - Jedes Upgrade besitzt eigene Namen, Icons und Humor-Beschreibungen (z. B. *Verstärkter Zeigefinger*, *Ergonomische Maus*, *Quanten-Tapper*).
3. **Synergie-Upgrades:**
   - Verbindet zwei Gebäude miteinander (z. B. *Fabrikations-Automatik*: Das Fabrik-Level erhöht die Effizienz des Auto-Aerpers).

---

## 2. Geplante Upgrade-Katalogübersicht

### A. Auto-Aerper Upgrades (Auto-Clicker)

| Tier / Name | Freischaltung | Kosten | Effekt |
| :--- | :--- | :--- | :--- |
| **Tier 1: Plastik-Finger** | Auto-Aerper Aktiv | 250 Aerps | Auto-Aerper Geschwindigkeit $+50\%$ |
| **Tier 2: Ergonomische Maus** | Auto-Aerper Level 5 | 1.000 Aerps | Auto-Aerper Ertrag $\times 2$ |
| **Tier 3: Makro-Bot Script** | Auto-Aerper Level 10 | 5.000 Aerps | Auto-Clicks profitieren zu $20\%$ vom manuellen Klick-Multiplikator |
| **Tier 4: Quanten-Tapper** | Auto-Aerper Level 25 | 25.000 Aerps | Auto-Aerper Ertrag $\times 2$ & Intervall minimal reduziert |
| **Tier 5: Neuronale Klick-KI** | Auto-Aerper Level 50 | 100.000 Aerps | Auto-Aerper Ertrag $\times 3$ |

---

### B. Aerp-Fabrik Upgrades (Passiver Generator)

| Tier / Name | Freischaltung | Kosten | Effekt |
| :--- | :--- | :--- | :--- |
| **Tier 1: Dampfantrieb** | Fabrik Aktiv | 500 Aerps | Fabrik-Basisproduktion $\times 2$ |
| **Tier 2: Fließband-Automatisierung** | Fabrik-Produktion Level 5 | 2.500 Aerps | Fabrik-Basisproduktion $\times 2$ |
| **Tier 3: KI-Fabrikleiter** | Fabrik-Produktion Level 10 | 12.000 Aerps | Fabrik-Basisproduktion $\times 2$ |
| **Tier 4: Industrielle Revolution 4.0** | Fabrik-Produktion Level 25 | 60.000 Aerps | Fabrik-Basisproduktion $\times 3$ |
| **Tier 5: Antimaterie-Reaktor** | Fabrik-Produktion Level 50 | 300.000 Aerps | Fabrik-Basisproduktion $\times 5$ |

---

### C. Synergie-Upgrades (Kombiniert)

| Name | Freischaltung | Kosten | Effekt |
| :--- | :--- | :--- | :--- |
| **Fabrikations-Klicks** | Auto-Aerper & Fabrik aktiv | 10.000 Aerps | Jeder Fabrik-Upgrade-Level erhöht die Klick-Stärke des Auto-Aerpers um $+5\%$ |

---

## 3. Technische Architektur & Datenmodell

Damit das Hinzufügen weiterer Upgrades nicht zu unübersichtlichen Einzelvariablen führt, strukturieren wir das Upgrade-System modular um:

### Data Models
- **`BuildingUpgrade` Data Class**:
  ```kotlin
  data class BuildingUpgrade(
      val id: String,
      val nameResId: Int,
      val descriptionResId: Int,
      val cost: Int,
      val buildingType: BuildingType,
      val requiredLevel: Int,
      val multiplierBonus: Double = 1.0,
      val isUnlocked: Boolean = false,
      val isPurchased: Boolean = false
  )
  ```
- **DataStore-Erweiterung (`GameStateKeys`)**:
  - Speicherung der erworbenen Upgrade-IDs als Set or Boolean Flags.

### UI & Shop-Anpassung
- Dynamische Filterung & Visualisierung im `ShopMenu` bzw. `BuildingsMenu`.
- Anzeige von Freischalt-Bedingungen (z. B. *"Benötigt Fabrik Level 5"*), wenn ein Tier-Upgrade noch gesperrt ist.
- Tooltips oder hervorgehobene Badges für neu freigeschaltete Tier-Upgrades.

---

## 4. Schritt-für-Schritt Umsetzungsplan

### Schritt### 1. Datenstruktur & Modellierung
- [x] Erstellen der `BuildingUpgrade`-Klasse und Aufzählung aller Tier-Upgrades.
- [x] Hinzufügen der String-Ressourcen in `strings.xml` (Deutsch, Englisch etc.).

### Schritt 2: ViewModel Logik & DataStore
- [x] Hinzufügen der Kauf- und Multiplikatorberechnungs-Logik in `GameViewModel`.
- [x] Speichern / Laden der gekauften Tier-Upgrades in DataStore.

### Schritt 3: UI-Integration im Shop
...
### Schritt 3: UI-Integration im Shop
- Erweiterung der `ShopItem`-Komponente um Tier-Icons, Level-Anforderungen und "Gesperrt / Freigeschaltet" Status.
- Dynamische Sortierung: Freigeschaltete Tier-Upgrades oben im Shop anzeigen.

---

## 5. Verifikationsplan

### Automatische Tests
- **`ExampleUnitTest.kt`**: Unittests für die Ertragsberechnung mit aktiven Tier-Upgrades und Synergien.

### Manuelle Tests
- Testen des Freischaltverhaltens: Erreichen von Level 5 schaltet das entsprechende Tier 2 Upgrade frei.
- Kauf verifizieren: Punkte werden korrekt abgezogen, Multiplikator wird angewendet, Status wird in DataStore gespeichert und nach Neustart geladen.
