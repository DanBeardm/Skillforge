package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BleedPassive {

    private static final Identifier SWORDS_ID =
            Identifier.of(
                    "skillforge",
                    "swords"
            );

    /*
     * One active Bleed per target.
     */
    private static final Map<UUID, BleedInstance>
            ACTIVE_BLEEDS =
            new HashMap<>();

    /*
     * Prevent Bleed's own damage from triggering
     * another Bleed roll.
     */
    private static final Set<UUID>
            PROCESSING_DAMAGE =
            new HashSet<>();

    private BleedPassive() {
    }

    public static void register() {

        /*
         * Detect successful sword hits.
         */
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity,
                 source,
                 baseDamageTaken,
                 damageTaken,
                 blocked) -> {

                    if (PROCESSING_DAMAGE.contains(
                            entity.getUuid()
                    )) {
                        return;
                    }

                    if (blocked
                            || damageTaken <= 0) {
                        return;
                    }

                    /*
                     * Only actual player melee attacks.
                     *
                     * This prevents things like arrows
                     * fired while holding a sword from
                     * triggering Bleed.
                     */
                    if (!source.isOf(
                            DamageTypes.PLAYER_ATTACK
                    )) {
                        return;
                    }

                    if (!(source.getAttacker()
                            instanceof ServerPlayerEntity player)) {
                        return;
                    }

                    if (!(entity.getWorld()
                            instanceof ServerWorld world)) {
                        return;
                    }

                    if (player.isCreative()) {
                        return;
                    }

                    var swordsConfig =
                            ConfigManager.get()
                                    .swords;

                    if (!swordsConfig.enabled) {
                        return;
                    }

                    var bleedConfig =
                            swordsConfig
                                    .passives
                                    .bleed;

                    if (!bleedConfig.enabled) {
                        return;
                    }

                    if (entity
                            instanceof ArmorStandEntity) {
                        return;
                    }

                    if (entity
                            instanceof ServerPlayerEntity
                            && !swordsConfig
                            .allowPlayerTargets) {

                        return;
                    }

                    if (!player
                            .getMainHandStack()
                            .isIn(ItemTags.SWORDS)) {

                        return;
                    }

                    Skill swords =
                            SkillRegistry.get(
                                    SWORDS_ID
                            );

                    if (swords == null) {
                        return;
                    }

                    SkillInstance swordsData =
                            PlayerDataManager
                                    .get(player)
                                    .getSkill(swords);

                    int level =
                            swordsData.getLevel();

                    double chance =
                            getBleedChance(level);

                    if (world.getRandom()
                            .nextDouble() >= chance) {

                        return;
                    }

                    applyBleed(
                            world,
                            entity,
                            player
                    );
                }
        );

        /*
         * Process active Bleeds.
         */
        ServerTickEvents.END_SERVER_TICK.register(
                BleedPassive::tick
        );
    }

    private static void applyBleed(
            ServerWorld world,
            LivingEntity target,
            ServerPlayerEntity attacker
    ) {

        var config =
                ConfigManager.get()
                        .swords
                        .passives
                        .bleed;

        UUID targetId =
                target.getUuid();

        if (ACTIVE_BLEEDS.containsKey(targetId)
                && !config.refreshOnProc) {

            return;
        }

        int applications =
                Math.max(
                        1,
                        config.damageApplications
                );

        int interval =
                Math.max(
                        1,
                        config.intervalTicks
                );

        /*
         * Replaces the existing entry if the target
         * was already bleeding, effectively refreshing it.
         */
        ACTIVE_BLEEDS.put(
                targetId,
                new BleedInstance(
                        attacker.getUuid(),
                        world.getRegistryKey(),
                        applications,
                        interval
                )
        );

        attacker.sendMessage(
                Text.literal(
                        "§cBleed!"
                ),
                true
        );
    }

    private static void tick(
            MinecraftServer server
    ) {

        Iterator<Map.Entry<UUID, BleedInstance>>
                iterator =
                ACTIVE_BLEEDS
                        .entrySet()
                        .iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, BleedInstance> entry =
                    iterator.next();

            UUID targetId =
                    entry.getKey();

            BleedInstance bleed =
                    entry.getValue();

            ServerWorld world =
                    server.getWorld(
                            bleed.worldKey
                    );

            if (world == null) {
                iterator.remove();
                continue;
            }

            Entity rawTarget =
                    world.getEntity(
                            targetId
                    );

            if (!(rawTarget
                    instanceof LivingEntity target)
                    || !target.isAlive()) {

                iterator.remove();
                continue;
            }

            ServerPlayerEntity attacker =
                    server
                            .getPlayerManager()
                            .getPlayer(
                                    bleed.attackerId
                            );

            /*
             * Don't keep dealing attributed damage
             * after the attacker has left the server.
             */
            if (attacker == null) {
                iterator.remove();
                continue;
            }

            bleed.ticksUntilNextDamage--;

            if (bleed.ticksUntilNextDamage > 0) {
                continue;
            }

            var config =
                    ConfigManager.get()
                            .swords
                            .passives
                            .bleed;

            float damage =
                    Math.max(
                            0.0f,
                            config.damagePerTick
                    );

            if (damage > 0) {

                PROCESSING_DAMAGE.add(
                        targetId
                );

                try {

                    /*
                     * Attribute the damage to the player.
                     *
                     * The PROCESSING_DAMAGE set prevents
                     * this from recursively rolling Bleed.
                     */
                    target.damage(
                            world
                                    .getDamageSources()
                                    .playerAttack(
                                            attacker
                                    ),
                            damage
                    );

                } finally {

                    PROCESSING_DAMAGE.remove(
                            targetId
                    );
                }
            }

            bleed.remainingApplications--;

            if (bleed.remainingApplications <= 0
                    || !target.isAlive()) {

                iterator.remove();
                continue;
            }

            bleed.ticksUntilNextDamage =
                    Math.max(
                            1,
                            config.intervalTicks
                    );
        }
    }

    public static double getBleedChance(
            int swordsLevel
    ) {

        var config =
                ConfigManager.get()
                        .swords
                        .passives
                        .bleed;

        int level =
                Math.max(
                        0,
                        swordsLevel
                );

        double chance =
                config.baseChance
                        + (
                        level
                                * config.chancePerLevel
                );

        chance =
                Math.min(
                        chance,
                        config.maxChance
                );

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        chance
                )
        );
    }

    private static final class BleedInstance {

        private final UUID attackerId;

        private final RegistryKey<World> worldKey;

        private int remainingApplications;

        private int ticksUntilNextDamage;

        private BleedInstance(
                UUID attackerId,
                RegistryKey<World> worldKey,
                int remainingApplications,
                int ticksUntilNextDamage
        ) {

            this.attackerId =
                    attackerId;

            this.worldKey =
                    worldKey;

            this.remainingApplications =
                    remainingApplications;

            this.ticksUntilNextDamage =
                    ticksUntilNextDamage;
        }
    }
}