package dev.aerodymeus.aerpclicker.models

import dev.aerodymeus.aerpclicker.R
import androidx.annotation.StringRes

/**
 * Static data for all building upgrades in the game.
 */
object UpgradeData {
    val upgrades = listOf(
        // Auto-Aerper Upgrades
        BuildingUpgrade(
            id = "auto_aerper_tier_1",
            nameResId = R.string.shop_item_auto_aerper_tier_1_name,
            descriptionResId = R.string.shop_item_auto_aerper_tier_1_desc,
            cost = 250,
            buildingType = BuildingType.AUTO_AERPER,
            requiredLevel = 0,
            speedMultiplier = 1.5,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "auto_aerper_tier_2",
            nameResId = R.string.shop_item_auto_aerper_tier_2_name,
            descriptionResId = R.string.shop_item_auto_aerper_tier_2_desc,
            cost = 1000,
            buildingType = BuildingType.AUTO_AERPER,
            requiredLevel = 5,
            multiplierBonus = 2.0,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "auto_aerper_tier_3",
            nameResId = R.string.shop_item_auto_aerper_tier_3_name,
            descriptionResId = R.string.shop_item_auto_aerper_tier_3_desc,
            cost = 5000,
            buildingType = BuildingType.AUTO_AERPER,
            requiredLevel = 10,
            multiplierBonus = 1.2,
            manualMultiplierBonus = 0.2,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "auto_aerper_tier_4",
            nameResId = R.string.shop_item_auto_aerper_tier_4_name,
            descriptionResId = R.string.shop_item_auto_aerper_tier_4_desc,
            cost = 25000,
            buildingType = BuildingType.AUTO_AERPER,
            requiredLevel = 25,
            multiplierBonus = 2.0,
            speedMultiplier = 0.9,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "auto_aerper_tier_5",
            nameResId = R.string.shop_item_auto_aerper_tier_5_name,
            descriptionResId = R.string.shop_item_auto_aerper_tier_5_desc,
            cost = 100000,
            buildingType = BuildingType.AUTO_AERPER,
            requiredLevel = 50,
            multiplierBonus = 3.0,
            isUnlocked = true
        ),

        // Aerp-Fabrik Upgrades
        BuildingUpgrade(
            id = "factory_tier_1",
            nameResId = R.string.shop_item_factory_tier_1_name,
            descriptionResId = R.string.shop_item_factory_tier_1_desc,
            cost = 500,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 0,
            multiplierBonus = 2.0,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "factory_tier_2",
            nameResId = R.string.shop_item_factory_tier_2_name,
            descriptionResId = R.string.shop_item_factory_tier_2_desc,
            cost = 2500,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 5,
            multiplierBonus = 2.0,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "factory_tier_3",
            nameResId = R.string.shop_item_factory_tier_3_name,
            descriptionResId = R.string.shop_item_factory_tier_3_desc,
            cost = 12000,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 10,
            multiplierBonus = 2.0,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "factory_tier_4",
            nameResId = R.string.shop_item_factory_tier_4_name,
            descriptionResId = R.string.shop_item_factory_tier_4_desc,
            cost = 60000,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 25,
            multiplierBonus = 3.0,
            isUnlocked = true
        ),
        BuildingUpgrade(
            id = "factory_tier_5",
            nameResId = R.string.shop_item_factory_tier_5_name,
            descriptionResId = R.string.shop_item_factory_tier_5_desc,
            cost = 300000,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 50,
            multiplierBonus = 5.0,
            isUnlocked = true
        ),

        // Synergies
        BuildingUpgrade(
            id = "synergy_fab_clicks",
            nameResId = R.string.shop_item_synergy_fab_clicks_name,
            descriptionResId = R.string.shop_item_synergy_fab_clicks_desc,
            cost = 10000,
            buildingType = BuildingType.AERP_FACTORY,
            requiredLevel = 0,
            multiplierBonus = 1.05,
            isUnlocked = true
        )
    )
}
