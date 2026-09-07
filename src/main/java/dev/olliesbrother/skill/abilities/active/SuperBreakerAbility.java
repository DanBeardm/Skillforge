package dev.olliesbrother.skill.abilities.active;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
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

public final class SuperBreakerAbility {

    private static final Identifier MINING_ID =
            Identifier.of("skillforge", "mining");

    /*
     * Time at which Super Breaker stops being active.
     */
    private static final Map<UUID, Long> ACTIVE_UNTIL =
            new HashMap<>();

    /*
     * Time at which Super Breaker may next be activated.
     */
    private static final Map<UUID, Long> COOLDOWN_UNTIL =
            new HashMap<>();

    private static final Identifier SPEED_MODIFIER_ID =
            Identifier.of(
                    "skillforge",
                    "super_breaker_speed"
            );

    /*
     * Stores the face the player most recently attacked.
     *
     * This lets us determine whether the 3x3 should be:
     *
     * XY - wall
     * YZ - wall
     * XZ - floor/ceiling
     */
    private static final Map<UUID, Direction> LAST_MINED_FACE =
            new HashMap<>();

    /*
     * Prevents the extra eight blocks from recursively
     * triggering another 3x3.
     */
    private static final Set<UUID> PROCESSING =
            new HashSet<>();

    private SuperBreakerAbility() {
    }

    public static void register() {

        /*
         * Sneak + right-click a block with a pickaxe
         * to activate Super Breaker.
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

                    if (!tool.isIn(ItemTags.PICKAXES)) {
                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get().mining.enabled) {
                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get()
                            .mining
                            .abilities
                            .superBreaker
                            .enabled) {

                        return ActionResult.PASS;
                    }

                    activate(serverPlayer);

                    /*
                     * PASS means Skillforge doesn't prevent the
                     * normal Minecraft right-click behaviour.
                     */
                    return ActionResult.PASS;
                }
        );

        /*
         * Record which face the player is mining.
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
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            long now =
                    System.currentTimeMillis();

            ACTIVE_UNTIL.entrySet()
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

                            removeMiningSpeedBonus(
                                    player
                            );

                            player.sendMessage(
                                    Text.literal(
                                            "§7Super Breaker has ended."
                                    ),
                                    true
                            );
                        }

                        return true;
                    });
        });

    }
    private static void applyMiningSpeedBonus(
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
         * Remove any stale Skillforge modifier first.
         */
        attribute.removeModifier(
                SPEED_MODIFIER_ID
        );

        double bonus =
                Math.max(
                        0.0,
                        ConfigManager.get()
                                .mining
                                .abilities
                                .superBreaker
                                .miningSpeedBonus
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

    private static void removeMiningSpeedBonus(
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

    private static void activate(
            ServerPlayerEntity player
    ) {

        UUID uuid = player.getUuid();

        long now = System.currentTimeMillis();

        /*
         * Don't reactivate while already active.
         */
        if (isActive(player)) {

            player.sendMessage(
                    Text.literal(
                            "§eSuper Breaker is already active!"
                    ),
                    true
            );

            return;
        }

        /*
         * Check cooldown.
         */
        long cooldownUntil =
                COOLDOWN_UNTIL.getOrDefault(
                        uuid,
                        0L
                );

        if (now < cooldownUntil) {

            long remainingMilliseconds =
                    cooldownUntil - now;

            long remainingSeconds =
                    (remainingMilliseconds + 999) / 1000;

            player.sendMessage(
                    Text.literal(
                            "§cSuper Breaker is on cooldown for "
                                    + remainingSeconds
                                    + "s."
                    ),
                    true
            );

            return;
        }

        /*
         * Check Mining level.
         */
        Skill mining =
                SkillRegistry.get(MINING_ID);

        if (mining == null) {
            return;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance miningData =
                playerData.getSkill(mining);

        var config =
                ConfigManager.get()
                        .mining
                        .abilities
                        .superBreaker;

        if (miningData.getLevel()
                < config.minimumLevel) {

            player.sendMessage(
                    Text.literal(
                            "§cYou need Mining level "
                                    + config.minimumLevel
                                    + " to use Super Breaker."
                    ),
                    true
            );

            return;
        }

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
                miningData.getLevel()
                        / levelsPerBonusSecond;

        int durationSeconds =
                baseDuration + bonusSeconds;

        durationSeconds =
                Math.min(
                        durationSeconds,
                        config.maxDurationSeconds
                );

        int cooldownSeconds =
                Math.max(
                        durationSeconds,
                        config.cooldownSeconds
                );

        long activeUntil =
                now + (durationSeconds * 1000L);

        long nextActivation =
                now + (cooldownSeconds * 1000L);

        ACTIVE_UNTIL.put(
                uuid,
                activeUntil
        );

        COOLDOWN_UNTIL.put(
                uuid,
                nextActivation
        );

        applyMiningSpeedBonus(player);

        player.sendMessage(
                Text.literal(
                        "§6§lSUPER BREAKER §eactivated for "
                                + durationSeconds
                                + " seconds!"
                ),
                false
        );
    }

    public static boolean isActive(
            ServerPlayerEntity player
    ) {

        UUID uuid = player.getUuid();

        Long activeUntil =
                ACTIVE_UNTIL.get(uuid);

        if (activeUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() >= activeUntil) {

            ACTIVE_UNTIL.remove(uuid);

            removeMiningSpeedBonus(player);

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

        if (!tool.isIn(ItemTags.PICKAXES)) {
            return;
        }

        Direction face =
                LAST_MINED_FACE.get(
                        player.getUuid()
                );

        if (face == null) {
            face = player.getHorizontalFacing();
        }

        UUID uuid = player.getUuid();

        PROCESSING.add(uuid);

        try {

            for (int first = -1; first <= 1; first++) {

                for (int second = -1; second <= 1; second++) {

                    /*
                     * Don't break the centre again.
                     */
                    if (first == 0 && second == 0) {
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
                            world.getBlockState(target);

                    if (targetState.isAir()) {
                        continue;
                    }

                    /*
                     * Don't automatically destroy blocks containing
                     * inventories or other block entities.
                     *
                     * This protects things such as furnaces,
                     * chests, spawners, etc.
                     */
                    if (world.getBlockEntity(target) != null) {
                        continue;
                    }

                    /*
                     * Retrieve the tool again because it may have
                     * lost durability or broken while processing
                     * previous blocks.
                     */
                    ItemStack currentTool =
                            player.getMainHandStack();

                    if (!currentTool.isIn(ItemTags.PICKAXES)) {
                        return;
                    }

                    /*
                     * Only break blocks the pickaxe is actually
                     * suitable for.
                     */
                    if (!currentTool.isSuitableFor(targetState)) {
                        continue;
                    }

                    /*
                     * Let Minecraft perform the actual block break.
                     */
                    boolean broken =
                            player.interactionManager
                                    .tryBreakBlock(target);

                    if (broken) {

                        float exhaustion =
                                Math.max(
                                        0.0f,
                                        ConfigManager.get()
                                                .mining
                                                .abilities
                                                .superBreaker
                                                .exhaustionPerExtraBlock
                                );

                        player.addExhaustion(exhaustion);
                    }
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

        return switch (face.getAxis()) {

            /*
             * Mining floor or ceiling.
             *
             * X Z
             */
            case Y ->
                    centre.add(
                            first,
                            0,
                            second
                    );

            /*
             * Mining east/west wall.
             *
             * Y Z
             */
            case X ->
                    centre.add(
                            0,
                            first,
                            second
                    );

            /*
             * Mining north/south wall.
             *
             * X Y
             */
            case Z ->
                    centre.add(
                            first,
                            second,
                            0
                    );
        };
    }


}