package dev.olliesbrother.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillforgeConfig {

    public MiningConfig mining = new MiningConfig();

    public WoodcuttingConfig woodcutting = new WoodcuttingConfig();

    public ExcavationConfig excavation = new ExcavationConfig();

    public HerbalismConfig herbalism = new HerbalismConfig();

    public FishingConfig fishing = new FishingConfig();

    public SwordsConfig swords = new SwordsConfig();

    public AxesConfig axes = new AxesConfig();

    public ArcheryConfig archery = new ArcheryConfig();

    public UnarmedConfig unarmed = new UnarmedConfig();

    public AcrobaticsConfig acrobatics = new AcrobaticsConfig();

    public static class MiningConfig {

        /*
         * Enables/disables the Mining skill entirely.
         */
        public boolean enabled = true;

        /*
         * If true, players only gain Mining XP when using
         * a tool suitable for the block being mined.
         */
        public boolean requireCorrectTool = true;

        /*
         * XP awarded for breaking configured blocks.
         */
        public Map<String, Double> experience =
                new LinkedHashMap<>();

        /*
         * Passive Mining abilities.
         */
        public MiningPassivesConfig passives =
                new MiningPassivesConfig();

        /*
         * Active Mining abilities.
         */
        public MiningAbilitiesConfig abilities =
                new MiningAbilitiesConfig();


        public MiningConfig() {

            /*
             * Basic blocks
             */
            experience.put(
                    "minecraft:stone",
                    1.0
            );

            experience.put(
                    "minecraft:deepslate",
                    1.0
            );

            /*
             * Coal
             */
            experience.put(
                    "minecraft:coal_ore",
                    5.0
            );

            experience.put(
                    "minecraft:deepslate_coal_ore",
                    5.0
            );

            /*
             * Copper
             */
            experience.put(
                    "minecraft:copper_ore",
                    6.0
            );

            experience.put(
                    "minecraft:deepslate_copper_ore",
                    6.0
            );

            /*
             * Iron
             */
            experience.put(
                    "minecraft:iron_ore",
                    8.0
            );

            experience.put(
                    "minecraft:deepslate_iron_ore",
                    8.0
            );

            /*
             * Redstone
             */
            experience.put(
                    "minecraft:redstone_ore",
                    10.0
            );

            experience.put(
                    "minecraft:deepslate_redstone_ore",
                    10.0
            );

            /*
             * Lapis
             */
            experience.put(
                    "minecraft:lapis_ore",
                    10.0
            );

            experience.put(
                    "minecraft:deepslate_lapis_ore",
                    10.0
            );

            /*
             * Gold
             */
            experience.put(
                    "minecraft:gold_ore",
                    12.0
            );

            experience.put(
                    "minecraft:deepslate_gold_ore",
                    12.0
            );

            experience.put(
                    "minecraft:nether_gold_ore",
                    10.0
            );

            /*
             * Emerald
             */
            experience.put(
                    "minecraft:emerald_ore",
                    20.0
            );

            experience.put(
                    "minecraft:deepslate_emerald_ore",
                    20.0
            );

            /*
             * Diamond
             */
            experience.put(
                    "minecraft:diamond_ore",
                    25.0
            );

            experience.put(
                    "minecraft:deepslate_diamond_ore",
                    25.0
            );

            /*
             * Nether Quartz
             */
            experience.put(
                    "minecraft:nether_quartz_ore",
                    8.0
            );

            /*
             * Ancient Debris
             */
            experience.put(
                    "minecraft:ancient_debris",
                    50.0
            );
        }
    }

    public static class MiningPassivesConfig {

        public ProspectorConfig prospector =
                new ProspectorConfig();
    }

    public static class ProspectorConfig {

        /*
         * Enables/disables Prospector.
         */
        public boolean enabled = true;

        /*
         * Chance gained per Mining level.
         *
         * 0.001 = 0.1% per level
         *
         * Level 10   = 1%
         * Level 100  = 10%
         * Level 500  = 50%
         * Level 1000 = 100%
         */
        public double chancePerLevel = 0.001;

        /*
         * Maximum possible Prospector chance.
         *
         * 1.0 = 100%
         */
        public double maxChance = 1.0;
    }

    public static class MiningAbilitiesConfig {

        public SuperBreakerConfig superBreaker =
                new SuperBreakerConfig();
    }

    public static class SuperBreakerConfig {

        /*
         * Enables/disables Super Breaker.
         */
        public boolean enabled = true;

        /*
         * Minimum Mining level required.
         */
        public int minimumLevel = 1;

        /*
         * Starting duration.
         */
        public int baseDurationSeconds = 5;

        /*
         * Gain one extra second for every
         * X Mining levels.
         */
        public int levelsPerBonusSecond = 100;

        /*
         * Maximum possible duration.
         */
        public int maxDurationSeconds = 15;

        /*
         * Additional block breaking speed while
         * Super Breaker is active.
         *
         * 0.25 = 25% increase.
         */
        public double miningSpeedBonus = 0.25;

        /*
         * Extra exhaustion added for each additional
         * block broken by Super Breaker.
         */
        public float exhaustionPerExtraBlock = 0.25f;

        /*
         * Time between activations.
         */
        public int cooldownSeconds = 60;
    }

    public static class WoodcuttingConfig {

        public boolean enabled = true;

        public boolean requireCorrectTool = true;

        public Map<String, Double> experience =
                new LinkedHashMap<>();

        public WoodcuttingAbilitiesConfig abilities =
                new WoodcuttingAbilitiesConfig();

        public WoodcuttingConfig() {

            experience.put(
                    "#minecraft:logs",
                    5.0
            );
        }
    }

    public static class WoodcuttingAbilitiesConfig {

        public TreeFellerConfig treeFeller =
                new TreeFellerConfig();
    }

    public static class TreeFellerConfig {

        public boolean enabled = true;

        public int minimumLevel = 1;

        /*
         * How long the player has after activation
         * to begin chopping a tree.
         */
        public int activationWindowSeconds = 10;

        /*
         * Base active duration.
         */
        public int baseDurationSeconds = 10;

        /*
         * Gain one extra second every X
         * Woodcutting levels.
         */
        public int levelsPerBonusSecond = 100;

        /*
         * Maximum active duration.
         */
        public int maxDurationSeconds = 20;

        /*
         * Extra exhaustion for every automatically
         * broken log.
         */
        public float exhaustionPerExtraLog = 0.25f;

        public int cooldownSeconds = 60;

        /*
         * Accelerates decay of unsupported leaves
         * after Tree Feller removes a tree.
         */
        public boolean forceLeafDecay = true;

        /*
         * How far around felled logs Skillforge
         * looks for leaves.
         */
        public int leafDecayRadius = 6;

        /*
         * Delay before removing unsupported leaves.
         * 20 ticks = 1 second.
         */
        public int leafDecayDelayTicks = 20;

        /*
         * Emergency safety limit for the number of
         * connected logs Tree Feller can process.
         *
         * This is not intended as a gameplay limit.
         */
        public int safetyLogLimit = 1024;
    }

    public static class ExcavationConfig {

        public boolean enabled = true;

        public boolean requireCorrectTool = true;

        public Map<String, Double> experience =
                new LinkedHashMap<>();

        public ExcavationPassivesConfig passives =
                new ExcavationPassivesConfig();

        public ExcavationAbilitiesConfig abilities =
                new ExcavationAbilitiesConfig();

        public ExcavationConfig() {

            experience.put("minecraft:dirt", 1.0);
            experience.put("minecraft:grass_block", 1.0);
            experience.put("minecraft:coarse_dirt", 1.5);
            experience.put("minecraft:rooted_dirt", 1.5);

            experience.put("minecraft:sand", 2.0);
            experience.put("minecraft:red_sand", 2.0);
            experience.put("minecraft:gravel", 2.0);

            experience.put("minecraft:clay", 4.0);
            experience.put("minecraft:mud", 2.0);
            experience.put("minecraft:muddy_mangrove_roots", 3.0);

            experience.put("minecraft:soul_sand", 3.0);
            experience.put("minecraft:soul_soil", 3.0);

            experience.put("minecraft:snow", 1.0);
            experience.put("minecraft:snow_block", 2.0);
        }
    }

    public static class ExcavationPassivesConfig {

        public TreasureHunterConfig treasureHunter =
                new TreasureHunterConfig();
    }

    public static class TreasureHunterConfig {

        public boolean enabled = true;

        /*
         * Starting chance of finding any treasure.
         *
         * 0.05 = 5%
         */
        public double baseChance = 0.05;

        /*
         * Additional chance per Excavation level.
         *
         * 0.0002 = 0.02% per level
         */
        public double chancePerLevel = 0.0002;

        /*
         * Maximum chance of finding treasure.
         *
         * 0.25 = 25%
         */
        public double maxChance = 0.25;

        public List<TreasureRewardConfig> rewards =
                new ArrayList<>();

        public TreasureHunterConfig() {

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:coal",
                            1,
                            40,
                            1
                    )
            );

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:iron_nugget",
                            10,
                            25,
                            1
                    )
            );

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:gold_nugget",
                            25,
                            15,
                            1
                    )
            );

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:lapis_lazuli",
                            40,
                            10,
                            1
                    )
            );

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:emerald",
                            75,
                            5,
                            1
                    )
            );

            rewards.add(
                    new TreasureRewardConfig(
                            "minecraft:diamond",
                            100,
                            5,
                            1
                    )
            );
        }
    }

    public static class TreasureRewardConfig {

        public String item;

        /*
         * Excavation level required before this
         * reward enters the treasure pool.
         */
        public int minimumLevel;

        /*
         * Relative weight within the unlocked
         * treasure pool.
         */
        public double weight;

        public int amount;

        public TreasureRewardConfig() {
        }

        public TreasureRewardConfig(
                String item,
                int minimumLevel,
                double weight,
                int amount
        ) {
            this.item = item;
            this.minimumLevel = minimumLevel;
            this.weight = weight;
            this.amount = amount;
        }
    }

    public static class ExcavationAbilitiesConfig {

        public GigaDrillConfig gigaDrill =
                new GigaDrillConfig();
    }

    public static class GigaDrillConfig {

        public boolean enabled = true;

        public int minimumLevel = 1;

        public int baseDurationSeconds = 5;

        /*
         * +1 second every 25 Excavation levels.
         */
        public int levelsPerBonusSecond = 25;

        public int maxDurationSeconds = 15;

        /*
         * +20% digging speed.
         */
        public double diggingSpeedBonus = 0.20;

        public float exhaustionPerExtraBlock = 0.25f;

        public int cooldownSeconds = 60;
    }

    public static class HerbalismConfig {

        public boolean enabled = true;

        public Map<String, Double> experience =
                new LinkedHashMap<>();

        public HerbalismAbilitiesConfig abilities =
                new HerbalismAbilitiesConfig();

        public HerbalismConfig() {

            /*
             * Crops - XP is only awarded when mature.
             */
            experience.put("minecraft:wheat", 4.0);
            experience.put("minecraft:carrots", 4.0);
            experience.put("minecraft:potatoes", 4.0);
            experience.put("minecraft:beetroots", 4.0);

            experience.put("minecraft:nether_wart", 5.0);
            experience.put("minecraft:cocoa", 5.0);

            /*
             * Harvestable fruits.
             *
             * Player-placed melon/pumpkin blocks are
             * protected by our placed-block tracking.
             */
            experience.put("minecraft:melon", 6.0);
            experience.put("minecraft:pumpkin", 6.0);
        }
    }

    public static class HerbalismAbilitiesConfig {

        public GreenThumbConfig greenThumb =
                new GreenThumbConfig();
    }

    public static class GreenThumbConfig {

        public boolean enabled = true;

        /*
         * We'll implement this next.
         */
        public double baseReplantChance = 0.25;

        public double chancePerLevel = 0.00075;

        public double maxReplantChance = 1.0;
    }

    public static class FishingConfig {

        public boolean enabled = true;

        public FishingExperienceConfig experience =
                new FishingExperienceConfig();

        public List<String> treasureItems =
                new ArrayList<>();

        public FishingPassivesConfig passives =
                new FishingPassivesConfig();

        public FishingConfig() {

            treasureItems.add("minecraft:bow");
            treasureItems.add("minecraft:enchanted_book");
            treasureItems.add("minecraft:fishing_rod");
            treasureItems.add("minecraft:name_tag");
            treasureItems.add("minecraft:nautilus_shell");
            treasureItems.add("minecraft:saddle");
        }
    }

    public static class FishingExperienceConfig {

        public double fish = 10.0;

        public double junk = 5.0;

        public double treasure = 25.0;
    }

    public static class FishingPassivesConfig {

        public AnglersFortuneConfig anglersFortune =
                new AnglersFortuneConfig();
    }

    public static class AnglersFortuneConfig {

        public boolean enabled = true;

        /*
         * Junk -> Fish
         */
        public double junkToFishBaseChance = 0.05;
        public double junkToFishChancePerLevel = 0.00015;
        public double junkToFishMaxChance = 0.20;

        /*
         * Fish -> Treasure
         */
        public double fishToTreasureBaseChance = 0.005;
        public double fishToTreasureChancePerLevel = 0.000045;
        public double fishToTreasureMaxChance = 0.05;

        /*
         * Preserve vanilla's open-water requirement
         * for treasure.
         */
        public boolean requireOpenWaterForTreasure = true;
    }

    public static class SwordsConfig {

        public boolean enabled = true;

        public double experiencePerHealth = 1.0;
        public double minimumExperience = 2.0;
        public double maximumExperience = 100.0;

        public boolean allowPlayerTargets = false;

        public SwordsPassivesConfig passives =
                new SwordsPassivesConfig();
    }

    public static class SwordsPassivesConfig {

        public BleedConfig bleed =
                new BleedConfig();
    }

    public static class BleedConfig {

        public boolean enabled = true;

        /*
         * Chance to inflict Bleed.
         */
        public double baseChance = 0.05;
        public double chancePerLevel = 0.00025;
        public double maxChance = 0.30;

        /*
         * 1.0 damage = half a heart.
         */
        public float damagePerTick = 1.0f;

        /*
         * Number of Bleed damage applications.
         */
        public int damageApplications = 3;

        /*
         * 20 ticks = one second.
         */
        public int intervalTicks = 20;

        /*
         * A new successful Bleed proc resets the timer
         * rather than stacking another Bleed instance.
         */
        public boolean refreshOnProc = true;
    }

    public static class AxesConfig {

        public boolean enabled = true;

        public double experiencePerHealth = 1.0;
        public double minimumExperience = 2.0;
        public double maximumExperience = 100.0;

        public boolean allowPlayerTargets = false;

        public AxesPassivesConfig passives =
                new AxesPassivesConfig();
    }

    public static class AxesPassivesConfig {

        public CriticalStrikeConfig criticalStrike =
                new CriticalStrikeConfig();
    }

    public static class CriticalStrikeConfig {

        public boolean enabled = true;

        /*
         * Level 1     ~5%
         * Level 100    7.5%
         * Level 500   17.5%
         * Level 1000  30%
         */
        public double baseChance = 0.05;
        public double chancePerLevel = 0.00025;
        public double maxChance = 0.30;

        /*
         * 0.50 = 50% additional damage.
         *
         * An incoming 10 damage axe hit becomes 15.
         */
        public double bonusDamageMultiplier = 0.50;
    }

    public static class ArcheryConfig {

        public boolean enabled = true;

        public double experiencePerHealth = 1.0;
        public double minimumExperience = 2.0;
        public double maximumExperience = 100.0;

        public boolean allowPlayerTargets = false;

        public ArcheryPassivesConfig passives =
                new ArcheryPassivesConfig();
    }

    public static class ArcheryPassivesConfig {

        public PiercingShotConfig piercingShot =
                new PiercingShotConfig();
    }

    public static class PiercingShotConfig {

        public boolean enabled = true;

        /*
         * Level 1      ~5%
         * Level 100     7.5%
         * Level 500    17.5%
         * Level 1000   30%
         */
        public double baseChance = 0.05;

        public double chancePerLevel = 0.00025;

        public double maxChance = 0.30;

        /*
         * Percentage of the armor reduction that
         * Piercing Shot ignores.
         *
         * 0.35 = 35%.
         */
        public double armorIgnoreFraction = 0.35;
    }

    public static class UnarmedConfig {

        public boolean enabled = true;

        public double experiencePerHealth = 1.0;
        public double minimumExperience = 2.0;
        public double maximumExperience = 100.0;

        public boolean allowPlayerTargets = false;

        public UnarmedPassivesConfig passives =
                new UnarmedPassivesConfig();
    }

    public static class UnarmedPassivesConfig {

        public IronFistsConfig ironFists =
                new IronFistsConfig();
    }

    public static class IronFistsConfig {

        public boolean enabled = true;

        /*
         * Extra raw attack damage per Unarmed level.
         *
         * 100  → +0.5
         * 500  → +2.5
         * 1000 → +5.0
         */
        public double bonusDamagePerLevel = 0.005;

        /*
         * Prevent extremely high levels from scaling
         * fist damage forever.
         */
        public double maximumBonusDamage = 5.0;
    }

    public static class AcrobaticsConfig {

        public boolean enabled = true;

        public double experiencePerDamage = 2.0;
        public double minimumDamageForExperience = 2.0;
        public double maximumExperiencePerFall = 100.0;

        public AcrobaticsPassivesConfig passives =
                new AcrobaticsPassivesConfig();
    }

    public static class AcrobaticsPassivesConfig {

        public RollConfig roll =
                new RollConfig();
    }

    public static class RollConfig {

        public boolean enabled = true;

        /*
         * Level 1      ~5%
         * Level 100     7.5%
         * Level 500    17.5%
         * Level 1000   30%
         */
        public double baseChance = 0.05;

        public double chancePerLevel = 0.00025;

        public double maxChance = 0.30;

        /*
         * 0.50 means a successful Roll removes
         * 50% of the incoming fall damage.
         */
        public double damageReductionFraction = 0.50;

        /*
         * Don't trigger Roll for tiny falls.
         *
         * 2 damage = 1 heart.
         */
        public double minimumDamageToRoll = 2.0;
    }

}