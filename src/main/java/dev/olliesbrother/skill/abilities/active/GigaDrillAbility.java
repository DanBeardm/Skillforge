package dev.olliesbrother.skill.abilities.active;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GigaDrillAbility {

    private static final Identifier EXCAVATION_ID =
            Identifier.of(
                    "skillforge",
                    "excavation"
            );

    /*
     * Unique attribute modifier used for the
     * temporary digging-speed increase.
     */
    private static final Identifier SPEED_MODIFIER_ID =
            Identifier.of(
                    "skillforge",
                    "giga_drill_speed"
            );

    /*
     * Time until Giga Drill stops being active.
     */
    private static final Map<UUID, Long> ACTIVE_UNTIL =
            new HashMap<>();

    /*
     * Time until the player may activate Giga Drill again.
     */
    private static final Map<UUID, Long> COOLDOWN_UNTIL =
            new HashMap<>();

    /*
     * Stores the face of the most recently attacked block
     * so the 3x3 plane can rotate correctly.
     */
    private static final Map<UUID, Direction> LAST_MINED_FACE =
            new HashMap<>();

    /*
     * Prevents automatically broken blocks from
     * recursively starting another 3x3.
     */
    private static final Set<UUID> PROCESSING =
            new HashSet<>();

    private GigaDrillAbility() {
    }

    public static void register() {

        /*
         * Sneak + right-click with a shovel
         * activates Giga Drill.
         */
        UseBlockCallback.EVENT.register(
                (player, world, hand, hitResult) -> {

                    if (!(world instanceof ServerWorld)) {
                        return ActionResult.PASS;
                    }

                    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return ActionResult.PASS;
                    }

                    if (hand != Hand.MAIN_HAND) {
                        return ActionResult.PASS;
                    }

                    if (!serverPlayer.isSneaking()) {
                        return ActionResult.PASS;
                    }

                    if (serverPlayer.isCreative()) {
                        return ActionResult.PASS;
                    }

                    ItemStack tool =
                            serverPlayer.getMainHandStack();

                    if (!tool.isIn(ItemTags.SHOVELS)) {
                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get()
                            .excavation
                            .enabled) {

                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get()
                            .excavation
                            .abilities
                            .gigaDrill
                            .enabled) {

                        return ActionResult.PASS;
                    }

                    activate(serverPlayer);

                    /*
                     * Prevent normal shovel right-click
                     * behaviour such as creating a path.
                     */
                    return ActionResult.SUCCESS;
                }
        );

        /*
         * Remember which face the player is digging.
         *
         * This determines whether the 3x3 is:
         *
         * XY = wall
         * YZ = wall
         * XZ = floor/ceiling
         */
        AttackBlockCallback.EVENT.register(
                (player, world, hand, pos, direction) -> {

                    if (!(world instanceof ServerWorld)) {
                        return ActionResult.PASS;
                    }

                    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return ActionResult.PASS;
                    }

                    if (hand != Hand.MAIN_HAND) {
                        return ActionResult.PASS;
                    }

                    LAST_MINED_FACE.put(
                            serverPlayer.getUuid(),
                            direction
                    );

                    return ActionResult.PASS;
                }
        );

        /*
         * Remove the speed modifier when the
         * ability expires.
         */
        ServerTickEvents.END_SERVER_TICK.register(
                server -> {

                    long now =
                            System.currentTimeMillis();

                    ACTIVE_UNTIL
                            .entrySet()
                            .removeIf(entry -> {

                                if (now < entry.getValue()) {
                                    return false;
                                }

                                ServerPlayerEntity player =
                                        server.getPlayerManager()
                                                .getPlayer(
                                                        entry.getKey()
                                                );

                                if (player != null) {

                                    removeDiggingSpeedBonus(
                                            player
                                    );

                                    player.sendMessage(
                                            Text.literal(
                                                    "§7Giga Drill has ended."
                                            ),
                                            true
                                    );
                                }

                                return true;
                            });

                    /*
                     * Prevent cooldown map entries from
                     * hanging around forever.
                     */
                    COOLDOWN_UNTIL
                            .entrySet()
                            .removeIf(
                                    entry ->
                                            now >= entry.getValue()
                            );
                }
        );
    }

    private static void activate(
            ServerPlayerEntity player
    ) {

        UUID uuid =
                player.getUuid();

        long now =
                System.currentTimeMillis();

        if (isActive(player)) {

            player.sendMessage(
                    Text.literal(
                            "§eGiga Drill is already active!"
                    ),
                    true
            );

            return;
        }

        long cooldownUntil =
                COOLDOWN_UNTIL.getOrDefault(
                        uuid,
                        0L
                );

        if (now < cooldownUntil) {

            long remainingMilliseconds =
                    cooldownUntil - now;

            long remainingSeconds =
                    (remainingMilliseconds + 999)
                            / 1000;

            player.sendMessage(
                    Text.literal(
                            "§cGiga Drill is on cooldown for "
                                    + remainingSeconds
                                    + "s."
                    ),
                    true
            );

            return;
        }

        Skill excavation =
                SkillRegistry.get(
                        EXCAVATION_ID
                );

        if (excavation == null) {
            return;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance excavationData =
                playerData.getSkill(
                        excavation
                );

        var config =
                ConfigManager.get()
                        .excavation
                        .abilities
                        .gigaDrill;

        if (excavationData.getLevel()
                < config.minimumLevel) {

            player.sendMessage(
                    Text.literal(
                            "§cYou need Excavation level "
                                    + config.minimumLevel
                                    + " to use Giga Drill."
                    ),
                    true
            );

            return;
        }

        int durationSeconds =
                getDurationSeconds(
                        excavationData.getLevel()
                );

        int cooldownSeconds =
                Math.max(
                        0,
                        config.cooldownSeconds
                );

        long activeUntil =
                now
                        + durationSeconds * 1000L;

        ACTIVE_UNTIL.put(
                uuid,
                activeUntil
        );

        /*
         * Cooldown starts when Giga Drill ends.
         */
        COOLDOWN_UNTIL.put(
                uuid,
                activeUntil
                        + cooldownSeconds * 1000L
        );

        applyDiggingSpeedBonus(
                player
        );

        player.sendMessage(
                Text.literal(
                        "§6§lGIGA DRILL §eactivated for "
                                + durationSeconds
                                + " seconds!"
                ),
                false
        );
    }

    /*
     * Shared duration calculation.
     *
     * Use this later in /skills excavation as well.
     */
    public static int getDurationSeconds(
            int excavationLevel
    ) {

        var config =
                ConfigManager.get()
                        .excavation
                        .abilities
                        .gigaDrill;

        int baseDuration =
                Math.max(
                        1,
                        config.baseDurationSeconds
                );

        int levelsPerBonusSecond =
                Math.max(
                        1,
                        config.levelsPerBonusSecond
                );

        int bonusSeconds =
                Math.max(
                        0,
                        excavationLevel
                )
                        / levelsPerBonusSecond;

        int duration =
                baseDuration
                        + bonusSeconds;

        return Math.min(
                duration,
                Math.max(
                        baseDuration,
                        config.maxDurationSeconds
                )
        );
    }

    public static boolean isActive(
            ServerPlayerEntity player
    ) {

        UUID uuid =
                player.getUuid();

        Long activeUntil =
                ACTIVE_UNTIL.get(uuid);

        if (activeUntil == null) {
            return false;
        }

        if (System.currentTimeMillis()
                >= activeUntil) {

            ACTIVE_UNTIL.remove(uuid);

            removeDiggingSpeedBonus(
                    player
            );

            return false;
        }

        return true;
    }

    public static boolean isProcessing(
            ServerPlayerEntity player
    ) {

        return PROCESSING.contains(
                player.getUuid()
        );
    }

    public static void breakArea(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos centre
    ) {

        if (!isActive(player)) {
            return;
        }

        if (isProcessing(player)) {
            return;
        }

        ItemStack tool =
                player.getMainHandStack();

        if (!tool.isIn(ItemTags.SHOVELS)) {
            return;
        }

        Direction face =
                LAST_MINED_FACE.get(
                        player.getUuid()
                );

        if (face == null) {
            face =
                    player.getHorizontalFacing();
        }

        UUID uuid =
                player.getUuid();

        PROCESSING.add(uuid);

        try {

            for (int first = -1;
                 first <= 1;
                 first++) {

                for (int second = -1;
                     second <= 1;
                     second++) {

                    /*
                     * Centre was already manually broken.
                     */
                    if (first == 0
                            && second == 0) {

                        continue;
                    }

                    BlockPos target =
                            getTargetPosition(
                                    centre,
                                    face,
                                    first,
                                    second
                            );

                    BlockState targetState =
                            world.getBlockState(
                                    target
                            );

                    if (targetState.isAir()) {
                        continue;
                    }

                    /*
                     * Giga Drill only automatically
                     * breaks shovel-mineable blocks.
                     */
                    if (!targetState.isIn(
                            BlockTags.SHOVEL_MINEABLE
                    )) {
                        continue;
                    }

                    /*
                     * Don't automatically destroy blocks
                     * with block entities.
                     */
                    if (world.getBlockEntity(target)
                            != null) {

                        continue;
                    }

                    /*
                     * Re-read the shovel because it may
                     * have broken while digging earlier blocks.
                     */
                    ItemStack currentTool =
                            player.getMainHandStack();

                    if (!currentTool.isIn(
                            ItemTags.SHOVELS
                    )) {

                        return;
                    }

                    /*
                     * Ensure Minecraft considers this shovel
                     * suitable for the target.
                     */
                    if (!currentTool.isSuitableFor(
                            targetState
                    )) {

                        continue;
                    }

                    /*
                     * Use Minecraft's normal block-breaking
                     * pipeline.
                     *
                     * ExcavationEvents will fire for this
                     * block as normal.
                     */
                    boolean broken =
                            player.interactionManager
                                    .tryBreakBlock(
                                            target
                                    );

                    if (!broken) {
                        continue;
                    }

                    float exhaustion =
                            Math.max(
                                    0.0f,
                                    ConfigManager.get()
                                            .excavation
                                            .abilities
                                            .gigaDrill
                                            .exhaustionPerExtraBlock
                            );

                    player.addExhaustion(
                            exhaustion
                    );
                }
            }

        } finally {

            PROCESSING.remove(uuid);
        }
    }

    private static BlockPos getTargetPosition(
            BlockPos centre,
            Direction face,
            int first,
            int second
    ) {

        return switch (
                face.getAxis()
                ) {

            /*
             * Floor / ceiling.
             *
             * XZ plane.
             */
            case Y ->
                    centre.add(
                            first,
                            0,
                            second
                    );

            /*
             * East / west facing wall.
             *
             * YZ plane.
             */
            case X ->
                    centre.add(
                            0,
                            first,
                            second
                    );

            /*
             * North / south facing wall.
             *
             * XY plane.
             */
            case Z ->
                    centre.add(
                            first,
                            second,
                            0
                    );
        };
    }

    private static void applyDiggingSpeedBonus(
            ServerPlayerEntity player
    ) {

        EntityAttributeInstance attribute =
                player.getAttributeInstance(
                        EntityAttributes.PLAYER_BLOCK_BREAK_SPEED
                );

        if (attribute == null) {
            return;
        }

        /*
         * Remove any stale modifier first.
         */
        attribute.removeModifier(
                SPEED_MODIFIER_ID
        );

        double bonus =
                Math.max(
                        0.0,
                        ConfigManager.get()
                                .excavation
                                .abilities
                                .gigaDrill
                                .diggingSpeedBonus
                );

        if (bonus <= 0) {
            return;
        }

        attribute.addTemporaryModifier(
                new EntityAttributeModifier(
                        SPEED_MODIFIER_ID,
                        bonus,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
        );
    }

    private static void removeDiggingSpeedBonus(
            ServerPlayerEntity player
    ) {

        EntityAttributeInstance attribute =
                player.getAttributeInstance(
                        EntityAttributes.PLAYER_BLOCK_BREAK_SPEED
                );

        if (attribute == null) {
            return;
        }

        attribute.removeModifier(
                SPEED_MODIFIER_ID
        );
    }
}