package dev.aerodymeus.aerpclicker.models

import androidx.annotation.StringRes

/**
 * Represents a type of building in the game.
 */
enum class BuildingType {
    AUTO_AERPER,
    AERP_FACTORY
}

/**
 * Types of synergies that upgrades can provide.
 */
enum class SynergyBonusType {
    /**
     * Increases Auto-Aerper production based on Factory level.
     */
    AUTO_AERPER_PRODUCTION_BOOST,
    /**
     * Increases Factory production based on Auto-Aerper level.
     */
    FACTORY_CLICK_BOOST
}

/**
 * Represents a tiered upgrade for a building.
 *
 * @property id Unique identifier for the upgrade.
 * @property nameResId Resource ID for the upgrade's name.
 * @property descriptionResId Resource ID for the upgrade's description.
 * @property cost The cost in Aerps to purchase this upgrade.
 * @property buildingType The type of building this upgrade applies to.
 * @property requiredLevel The building level required to unlock this upgrade.
 * @property multiplierBonus The multiplicative bonus this upgrade provides (e.g., 2.0 for 2x).
 * @property isUnlocked Whether the upgrade is currently visible/available to the player.
 * @property isPurchased Whether the player has already purchased this upgrade.
 * @property synergyBonusType An optional synergy effect this upgrade provides.
 */
data class BuildingUpgrade(
    val id: String,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    val cost: Int,
    val buildingType: BuildingType,
    val requiredLevel: Int,
    val multiplierBonus: Double = 1.0,
    val speedMultiplier: Double = 1.0,
    val manualMultiplierBonus: Double = 0.0,
    val isUnlocked: Boolean = false,
    val isPurchased: Boolean = false,
    val synergyBonusType: SynergyBonusType? = null
)
