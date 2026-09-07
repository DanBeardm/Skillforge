package dev.olliesbrother.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.LevelCurve;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.abilities.active.GigaDrillAbility;
import dev.olliesbrother.skill.abilities.active.TreeFellerAbility;
import dev.olliesbrother.skill.abilities.passive.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public final class SkillsCommand {

    private SkillsCommand() {
    }

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            CommandManager.literal("skills")

                                    /*
                                     * /skills
                                     */
                                    .executes(context -> {

                                        ServerPlayerEntity player =
                                                context.getSource()
                                                        .getPlayerOrThrow();

                                        showSkillList(player);

                                        return 1;
                                    })

                                    /*
                                     * /skills <skill>
                                     */
                                    .then(
                                            CommandManager.argument(
                                                            "skill",
                                                            StringArgumentType.word()
                                                    )

                                                    /*
                                                     * Dynamically suggest every
                                                     * registered skill.
                                                     */
                                                    .suggests(
                                                            (context, builder) -> {

                                                                List<String> skills =
                                                                        SkillRegistry
                                                                                .getAll()
                                                                                .stream()
                                                                                .map(
                                                                                        skill ->
                                                                                                skill.getId()
                                                                                                        .getPath()
                                                                                )
                                                                                .toList();

                                                                return CommandSource
                                                                        .suggestMatching(
                                                                                skills,
                                                                                builder
                                                                        );
                                                            }
                                                    )

                                                    .executes(context -> {

                                                        ServerPlayerEntity player =
                                                                context.getSource()
                                                                        .getPlayerOrThrow();

                                                        String skillName =
                                                                StringArgumentType
                                                                        .getString(
                                                                                context,
                                                                                "skill"
                                                                        );

                                                        return showSkill(
                                                                player,
                                                                skillName
                                                        );
                                                    })
                                    )
                    );
                }
        );
    }

    /*
     * =========================
     * /skills
     * =========================
     */

    private static void showSkillList(
            ServerPlayerEntity player
    ) {

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        player.sendMessage(
                Text.literal(
                        "§6§lSkillforge Skills"
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        for (Skill skill :
                SkillRegistry.getAll()) {

            SkillInstance instance =
                    playerData.getSkill(skill);

            double requiredExperience =
                    LevelCurve.getRequiredExperience(
                            instance.getLevel()
                    );

            player.sendMessage(
                    Text.literal(
                            "§e"
                                    + skill.getDisplayName()
                                    + " §7- Level §f"
                                    + instance.getLevel()
                                    + " §7("
                                    + formatNumber(
                                    instance.getExperience()
                            )
                                    + " / "
                                    + formatNumber(
                                    requiredExperience
                            )
                                    + " XP)"
                    ),
                    false
            );
        }

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Use §f/skills <skill> §7for more information."
                ),
                false
        );
    }

    /*
     * =========================
     * /skills <skill>
     * =========================
     */

    private static int showSkill(
            ServerPlayerEntity player,
            String skillName
    ) {

        Identifier skillId =
                parseSkillId(skillName);

        if (skillId == null) {

            player.sendMessage(
                    Text.literal(
                            "§cInvalid skill."
                    ),
                    false
            );

            return 0;
        }

        Skill skill =
                SkillRegistry.get(skillId);

        if (skill == null) {

            player.sendMessage(
                    Text.literal(
                            "§cUnknown skill: "
                                    + skillName
                    ),
                    false
            );

            return 0;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance instance =
                playerData.getSkill(skill);

        double requiredExperience =
                LevelCurve.getRequiredExperience(
                        instance.getLevel()
                );

        /*
         * Header
         */
        player.sendMessage(
                Text.literal(
                        "§6§l"
                                + skill.getDisplayName()
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Level: §f"
                                + instance.getLevel()
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7XP: §f"
                                + formatNumber(
                                instance.getExperience()
                        )
                                + " §7/ §f"
                                + formatNumber(
                                requiredExperience
                        )
                ),
                false
        );

        /*
         * Skill-specific information.
         */
        switch (skill.getId().getPath()) {

            case "mining" ->
                    showMiningDetails(
                            player,
                            instance
                    );

            case "woodcutting" ->
                    showWoodcuttingDetails(
                            player,
                            instance
                    );

            case "excavation" ->
                    showExcavationDetails(
                            player,
                            instance
                    );

            case "herbalism" ->
                    showHerbalismDetails(
                            player,
                            instance
                    );

            case "fishing" ->
                    showFishingDetails(
                            player,
                            instance
                    );

            case "swords" ->
                    showSwordsDetails(
                            player,
                            instance
                    );

            case "axes" ->
                    showAxesDetails(
                            player,
                            instance
                    );

            case "archery" ->
                    showArcheryDetails(
                            player,
                            instance
                    );

            case "unarmed" ->
                    showUnarmedDetails(
                            player,
                            instance
                    );

            case "acrobatics" ->
                    showAcrobaticsDetails(
                            player,
                            instance
                    );

            default -> {

                player.sendMessage(
                        Text.literal(" "),
                        false
                );

                player.sendMessage(
                        Text.literal(
                                "§7No additional information available."
                        ),
                        false
                );
            }
        }

        return 1;
    }

    /*
     * =========================
     * Mining
     * =========================
     */

    private static void showMiningDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var mining =
                ConfigManager.get().mining;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        /*
         * Prospector
         */
        player.sendMessage(
                Text.literal(
                        "§6§lPassives"
                ),
                false
        );

        if (mining.passives.prospector.enabled) {

            double chance =
                    instance.getLevel()
                            * mining.passives
                            .prospector
                            .chancePerLevel;

            chance =
                    Math.min(
                            chance,
                            mining.passives
                                    .prospector
                                    .maxChance
                    );

            chance =
                    Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    chance
                            )
                    );

            player.sendMessage(
                    Text.literal(
                            "§eProspector"
                    ),
                    false
            );

            player.sendMessage(
                    Text.literal(
                            "§7Bonus drop chance: §f"
                                    + formatPercent(chance)
                    ),
                    false
            );

        } else {

            player.sendMessage(
                    Text.literal(
                            "§7Prospector: Disabled"
                    ),
                    false
            );
        }

        /*
         * Super Breaker
         */
        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var superBreaker =
                mining.abilities.superBreaker;

        if (!superBreaker.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Super Breaker: Disabled"
                    ),
                    false
            );

            return;
        }

        int levelsPerBonusSecond =
                Math.max(
                        1,
                        superBreaker.levelsPerBonusSecond
                );

        int duration =
                Math.max(
                        1,
                        superBreaker.baseDurationSeconds
                )
                        + (
                        instance.getLevel()
                                / levelsPerBonusSecond
                );

        duration =
                Math.min(
                        duration,
                        superBreaker.maxDurationSeconds
                );

        int miningSpeedPercent =
                (int) Math.round(
                        superBreaker.miningSpeedBonus
                                * 100
                );

        player.sendMessage(
                Text.literal(
                        "§eSuper Breaker"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Duration: §f"
                                + duration
                                + " seconds"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Mining speed: §f+"
                                + miningSpeedPercent
                                + "%"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Area: §f3x3"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Cooldown: §f"
                                + superBreaker.cooldownSeconds
                                + " seconds"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fSneak + Right-click with a pickaxe"
                ),
                false
        );
    }

    /*
     * =========================
     * Woodcutting
     * =========================
     */

    private static void showWoodcuttingDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var treeFeller =
                ConfigManager.get()
                        .woodcutting
                        .abilities
                        .treeFeller;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        if (!treeFeller.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Tree Feller: Disabled"
                    ),
                    false
            );

            return;
        }


        player.sendMessage(
                Text.literal(
                        "§eTree Feller"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Active duration: §f"
                                + treeFeller
                                .activationWindowSeconds
                                + " seconds"
                ),
                false
        );

        int levelsPerBonusSecond =
                Math.max(
                        1,
                        treeFeller.levelsPerBonusSecond
                );

        int activeDuration =
                TreeFellerAbility.getDurationSeconds(
                        instance.getLevel()
                );

        player.sendMessage(
                Text.literal(
                        "§7Active duration: §f"
                                + activeDuration
                                + " seconds"
                ),
                false
        );

        if (treeFeller.forceLeafDecay) {

            player.sendMessage(
                    Text.literal(
                            "§7Leaf decay: §fAccelerated"
                    ),
                    false
            );
        }

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fSneak + Right-click with an axe"
                ),
                false
        );
    }

    private static void showExcavationDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var excavation =
                ConfigManager.get()
                        .excavation;

        /*
         * =========================
         * Treasure Hunter
         * =========================
         */

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lPassives"
                ),
                false
        );

        var treasureHunter =
                excavation
                        .passives
                        .treasureHunter;

        if (!treasureHunter.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Treasure Hunter: Disabled"
                    ),
                    false
            );

        } else {

            player.sendMessage(
                    Text.literal(
                            "§eTreasure Hunter"
                    ),
                    false
            );

            player.sendMessage(
                    Text.literal(
                            "§7Available treasures:"
                    ),
                    false
            );

            treasureHunter.rewards
                    .stream()
                    .filter(
                            reward ->
                                    instance.getLevel()
                                            >= reward.minimumLevel
                    )
                    .forEach(reward -> {

                        Identifier itemId =
                                Identifier.tryParse(
                                        reward.item
                                );

                        String itemName =
                                reward.item;

                        if (itemId != null
                                && Registries.ITEM.containsId(itemId)) {

                            itemName =
                                    new ItemStack(
                                            Registries.ITEM.get(itemId)
                                    )
                                            .getName()
                                            .getString();
                        }

                        double treasureChance =
                                TreasureHunterPassive.getTreasureChance(
                                        instance.getLevel()
                                );

                        player.sendMessage(
                                Text.literal(
                                        "§7Treasure find chance: §f"
                                                + formatPercent(
                                                treasureChance
                                        )
                                ),
                                false
                        );
                    });
        }

        /*
         * =========================
         * Giga Drill
         * =========================
         */

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var gigaDrill =
                excavation
                        .abilities
                        .gigaDrill;

        if (!gigaDrill.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Giga Drill: Disabled"
                    ),
                    false
            );

            return;
        }

        int duration =
                GigaDrillAbility
                        .getDurationSeconds(
                                instance.getLevel()
                        );

        int speedPercent =
                (int) Math.round(
                        gigaDrill.diggingSpeedBonus
                                * 100
                );

        player.sendMessage(
                Text.literal(
                        "§eGiga Drill"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Duration: §f"
                                + duration
                                + " seconds"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Digging speed: §f+"
                                + speedPercent
                                + "%"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Area: §f3x3"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Cooldown: §f"
                                + gigaDrill.cooldownSeconds
                                + " seconds"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fSneak + Right-click with a shovel"
                ),
                false
        );
    }

    /*
     * Allows:
     *
     * /skills mining
     *
     * and also:
     *
     * /skills skillforge:mining
     */
    private static Identifier parseSkillId(
            String input
    ) {

        if (input.contains(":")) {
            return Identifier.tryParse(input);
        }

        return Identifier.tryParse(
                "skillforge:" + input
        );
    }

    /*
     * Avoid displaying things like:
     *
     * 25.0 XP
     *
     * when:
     *
     * 25 XP
     *
     * looks cleaner.
     */
    private static String formatNumber(
            double number
    ) {

        if (number
                == Math.floor(number)) {

            return String.valueOf(
                    (long) number
            );
        }

        return String.format(
                "%.1f",
                number
        );
    }

    private static String formatPercent(
            double chance
    ) {

        double percentage =
                chance * 100.0;

        if (percentage
                == Math.floor(percentage)) {

            return String.format(
                    "%.0f%%",
                    percentage
            );
        }

        return String.format(
                "%.1f%%",
                percentage
        );
    }

    private static void showHerbalismDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var herbalism =
                ConfigManager.get()
                        .herbalism;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var greenThumb =
                herbalism
                        .abilities
                        .greenThumb;

        if (!greenThumb.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Green Thumb: Disabled"
                    ),
                    false
            );

            return;
        }

        double replantChance =
                GreenThumbPassive
                        .getReplantChance(
                                instance.getLevel()
                        );

        player.sendMessage(
                Text.literal(
                        "§eGreen Thumb"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Replant chance: §f"
                                + formatPercent(
                                replantChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Automatically replants mature crops when harvested."
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Eligible crops:"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fWheat"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fCarrots"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fPotatoes"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fBeetroot"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fNether Wart"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §fCocoa"
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }

    private static void showFishingDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var fishing =
                ConfigManager.get()
                        .fishing;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var anglersFortune =
                fishing
                        .passives
                        .anglersFortune;

        if (!anglersFortune.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Angler's Fortune: Disabled"
                    ),
                    false
            );

            return;
        }

        int level =
                instance.getLevel();

        double junkToFishChance =
                AnglersFortunePassive
                        .getJunkToFishChance(
                                level
                        );

        double fishToTreasureChance =
                AnglersFortunePassive
                        .getFishToTreasureChance(
                                level
                        );

        player.sendMessage(
                Text.literal(
                        "§eAngler's Fortune"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Junk → Fish: §f"
                                + formatPercent(
                                junkToFishChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Fish → Treasure: §f"
                                + formatPercent(
                                fishToTreasureChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Treasure requires open water: §f"
                                + (
                                anglersFortune
                                        .requireOpenWaterForTreasure
                                        ? "Yes"
                                        : "No"
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Catch XP:"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §7Fish: §f"
                                + formatNumber(
                                fishing
                                        .experience
                                        .fish
                        )
                                + " XP"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §7Junk: §f"
                                + formatNumber(
                                fishing
                                        .experience
                                        .junk
                        )
                                + " XP"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "  §7Treasure: §f"
                                + formatNumber(
                                fishing
                                        .experience
                                        .treasure
                        )
                                + " XP"
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }

    private static void showSwordsDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var swords =
                ConfigManager.get()
                        .swords;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var bleed =
                swords
                        .passives
                        .bleed;

        if (!bleed.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Bleed: Disabled"
                    ),
                    false
            );

            return;
        }

        double bleedChance =
                BleedPassive.getBleedChance(
                        instance.getLevel()
                );

        double totalDamage =
                bleed.damagePerTick
                        * bleed.damageApplications;

        double durationSeconds =
                (
                        bleed.intervalTicks
                                * bleed.damageApplications
                ) / 20.0;

        player.sendMessage(
                Text.literal(
                        "§cBleed"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Proc chance: §f"
                                + formatPercent(
                                bleedChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Damage per tick: §f"
                                + formatNumber(
                                bleed.damagePerTick
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Damage applications: §f"
                                + bleed.damageApplications
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Total damage: §f"
                                + formatNumber(
                                totalDamage
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Duration: §f"
                                + formatNumber(
                                durationSeconds
                        )
                                + "s"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Refreshes on proc: §f"
                                + (
                                bleed.refreshOnProc
                                        ? "Yes"
                                        : "No"
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }

    private static void showAxesDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var axes =
                ConfigManager.get()
                        .axes;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var criticalStrike =
                axes
                        .passives
                        .criticalStrike;

        if (!criticalStrike.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Critical Strike: Disabled"
                    ),
                    false
            );

            return;
        }

        double criticalChance =
                CriticalStrikePassive
                        .getCriticalStrikeChance(
                                instance.getLevel()
                        );

        double bonusDamage =
                Math.max(
                        0.0,
                        criticalStrike
                                .bonusDamageMultiplier
                );

        player.sendMessage(
                Text.literal(
                        "§6Critical Strike"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Proc chance: §f"
                                + formatPercent(
                                criticalChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Bonus damage: §f+"
                                + formatPercent(
                                bonusDamage
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Total hit multiplier: §f"
                                + formatNumber(
                                CriticalStrikePassive
                                        .getDamageMultiplier()
                        )
                                + "x"
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }

    private static void showArcheryDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var archery =
                ConfigManager.get()
                        .archery;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var piercingShot =
                archery
                        .passives
                        .piercingShot;

        if (!piercingShot.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Piercing Shot: Disabled"
                    ),
                    false
            );

            return;
        }

        double piercingChance =
                PiercingShotPassive
                        .getPiercingShotChance(
                                instance.getLevel()
                        );

        double armorIgnore =
                PiercingShotPassive
                        .getArmorIgnoreFraction();

        player.sendMessage(
                Text.literal(
                        "§bPiercing Shot"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Proc chance: §f"
                                + formatPercent(
                                piercingChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Armor ignored: §f"
                                + formatPercent(
                                armorIgnore
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Allows arrows to ignore part of the target's armor."
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }


    private static void showUnarmedDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var unarmed =
                ConfigManager.get()
                        .unarmed;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var ironFists =
                unarmed
                        .passives
                        .ironFists;

        if (!ironFists.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Iron Fists: Disabled"
                    ),
                    false
            );

            return;
        }

        double bonusDamage =
                IronFistsPassive
                        .getBonusDamage(
                                instance.getLevel()
                        );

        player.sendMessage(
                Text.literal(
                        "§eIron Fists"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Bonus fist damage: §f+"
                                + formatNumber(
                                bonusDamage
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Maximum bonus damage: §f+"
                                + formatNumber(
                                ironFists
                                        .maximumBonusDamage
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Improves damage dealt while your main hand is empty."
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }

    private static void showAcrobaticsDetails(
            ServerPlayerEntity player,
            SkillInstance instance
    ) {

        var acrobatics =
                ConfigManager.get()
                        .acrobatics;

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§6§lAbilities"
                ),
                false
        );

        var roll =
                acrobatics
                        .passives
                        .roll;

        if (!roll.enabled) {

            player.sendMessage(
                    Text.literal(
                            "§7Roll: Disabled"
                    ),
                    false
            );

            return;
        }

        double rollChance =
                RollPassive.getRollChance(
                        instance.getLevel()
                );

        double damageReduction =
                RollPassive
                        .getDamageReductionFraction();

        player.sendMessage(
                Text.literal(
                        "§aRoll"
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Proc chance: §f"
                                + formatPercent(
                                rollChance
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Fall damage reduction: §f"
                                + formatPercent(
                                damageReduction
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Minimum fall damage to trigger: §f"
                                + formatNumber(
                                roll.minimumDamageToRoll
                        )
                ),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Reduces incoming fall damage when successfully triggered."
                ),
                false
        );

        player.sendMessage(
                Text.literal(" "),
                false
        );

        player.sendMessage(
                Text.literal(
                        "§7Activation: §fPassive"
                ),
                false
        );
    }
}