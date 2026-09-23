# Session Summary

## Actions Taken

### 1. Refactoring & Naming
- Renamed factory-related variables in `GameViewModel.kt` (e.g., `factoryUpgradeLevel` -> `factoryProductionUpgradeLevel`) to clearly distinguish between base buildings and production upgrades.
- Updated associated cost and bonus variables to match the new naming convention.

### 2. New Mechanics
- Introduced `specialBonusAmount` and `specialBonusLevelInterval` in `GameViewModel.kt` to support a new special bonus mechanic triggered every X levels.

### 3. UI & Data Structure Updates
- Refactored `ShopItemData` in `UI.kt` to be more flexible and accommodate multiple types of upgrades (Click Boost, Auto-Clicker, Factory Production, Factory Interval).
- Implemented dynamic mapping of `upgradeItems` in `UI.kt` to reflect the new data structure.
- Corrected import paths for `UpgradeData` and `BuildingType` in `UI.kt`.

### 4. Project Maintenance
- Performed `gradle_sync` to ensure everything is consistent.
