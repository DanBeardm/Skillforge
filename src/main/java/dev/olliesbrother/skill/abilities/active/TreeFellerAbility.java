package dev.olliesbrother.skill.abilities.active;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.persistence.SkillforgeState;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public final class TreeFellerAbility {

    private static final Identifier WOODCUTTING_ID =
            Identifier.of(
                    "skillforge",
                    "woodcutting"
            );

    /*
     * Time until which Tree Feller remains active.
     *
     * The player can fell multiple trees during
     * this window.
     */
    private static final Map<UUID, Long> ACTIVE_UNTIL =
            new HashMap<>();

    /*
     * Time until which Tree Feller cannot be
     * activated again.
     */
    private static final Map<UUID, Long> COOLDOWN_UNTIL =
            new HashMap<>();

    /*
     * Prevents logs automatically broken by Tree Feller
     * from recursively triggering Tree Feller again.
     */
    private static final Set<UUID> PROCESSING =
            new HashSet<>();

    /*
     * Leaves waiting to be checked after their supporting
     * logs have been removed.
     */
    private static final List<PendingLeafDecay> PENDING_LEAF_DECAY =
            new ArrayList<>();

    private TreeFellerAbility() {
    }

    /*
     * Stores one delayed leaf-decay job.
     */
    private record PendingLeafDecay(
            ServerWorld world,
            Set<BlockPos> leaves,
            long executeAt
    ) {
    }

    public static void register() {

        /*
         * Sneak + right-click with an axe activates
         * Tree Feller.
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

                    if (!tool.isIn(ItemTags.AXES)) {
                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get()
                            .woodcutting
                            .enabled) {

                        return ActionResult.PASS;
                    }

                    if (!ConfigManager.get()
                            .woodcutting
                            .abilities
                            .treeFeller
                            .enabled) {

                        return ActionResult.PASS;
                    }

                    activate(serverPlayer);

                    /*
                     * Prevent normal axe interaction such as
                     * stripping the clicked log when using
                     * sneak + right-click to activate.
                     */
                    return ActionResult.SUCCESS;
                }
        );

        /*
         * Process pending leaf-decay jobs at the
         * end of each world's tick.
         */
        ServerTickEvents.END_WORLD_TICK.register(
                world -> processPendingLeafDecay(world)
        );

        /*
         * Clean expired active/cooldown entries.
         */
        ServerTickEvents.END_SERVER_TICK.register(
                server -> {

                    long now =
                            System.currentTimeMillis();

                    Iterator<Map.Entry<UUID, Long>>
                            activeIterator =
                            ACTIVE_UNTIL
                                    .entrySet()
                                    .iterator();

                    while (activeIterator.hasNext()) {

                        Map.Entry<UUID, Long> entry =
                                activeIterator.next();

                        if (now < entry.getValue()) {
                            continue;
                        }

                        ServerPlayerEntity player =
                                server.getPlayerManager()
                                        .getPlayer(entry.getKey());

                        if (player != null) {

                            player.sendMessage(
                                    Text.literal(
                                            "§7Tree Feller has ended."
                                    ),
                                    true
                            );
                        }

                        activeIterator.remove();
                    }

                    /*
                     * Remove expired cooldown entries so the map
                     * doesn't retain UUIDs indefinitely.
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

        /*
         * Don't reactivate while the ability is
         * already running.
         */
        if (isActive(player)) {

            player.sendMessage(
                    Text.literal(
                            "§eTree Feller is already active!"
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
                    (remainingMilliseconds + 999)
                            / 1000;

            player.sendMessage(
                    Text.literal(
                            "§cTree Feller is on cooldown for "
                                    + remainingSeconds
                                    + "s."
                    ),
                    true
            );

            return;
        }

        /*
         * Get Woodcutting skill data.
         */
        Skill woodcutting =
                SkillRegistry.get(
                        WOODCUTTING_ID
                );

        if (woodcutting == null) {
            return;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance woodcuttingData =
                playerData.getSkill(
                        woodcutting
                );

        var config =
                ConfigManager.get()
                        .woodcutting
                        .abilities
                        .treeFeller;

        /*
         * Minimum Woodcutting level.
         */
        if (woodcuttingData.getLevel()
                < config.minimumLevel) {

            player.sendMessage(
                    Text.literal(
                            "§cYou need Woodcutting level "
                                    + config.minimumLevel
                                    + " to use Tree Feller."
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
                woodcuttingData.getLevel()
                        / levelsPerBonusSecond;

        int activeSeconds =
                getDurationSeconds(
                        woodcuttingData.getLevel()
                );

        activeSeconds =
                Math.min(
                        activeSeconds,
                        config.maxDurationSeconds
                );

        int cooldownSeconds =
                Math.max(
                        0,
                        config.cooldownSeconds
                );

        long activeUntil =
                now
                        + (
                        activeSeconds
                                * 1000L
                );

        /*
         * Tree Feller stays active for the whole
         * configured window.
         */
        ACTIVE_UNTIL.put(
                uuid,
                activeUntil
        );

        /*
         * Cooldown begins once the active period ends.
         */
        COOLDOWN_UNTIL.put(
                uuid,
                activeUntil
                        + (
                        cooldownSeconds
                                * 1000L
                )
        );

        player.sendMessage(
                Text.literal(
                        "§6§lTREE FELLER §eactive for "
                                + activeSeconds
                                + " seconds!"
                ),
                false
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

    public static void tryFellTree(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos originalPos,
            BlockState originalState
    ) {

        /*
         * Tree Feller must currently be active.
         */
        if (!isActive(player)) {
            return;
        }

        /*
         * Prevent recursive Tree Feller calls caused
         * by automatically broken logs.
         */
        if (isProcessing(player)) {
            return;
        }

        /*
         * Only logs can start Tree Feller.
         */
        if (!originalState.isIn(BlockTags.LOGS)) {
            return;
        }

        /*
         * Player-placed logs cannot start Tree Feller.
         */
        if (SkillforgeState
                .get(world.getServer())
                .isPlayerPlaced(
                        world,
                        originalPos
                )) {

            return;
        }

        ItemStack tool =
                player.getMainHandStack();

        if (!tool.isIn(ItemTags.AXES)) {
            return;
        }

        if (!tool.isSuitableFor(originalState)) {
            return;
        }

        Skill woodcutting =
                SkillRegistry.get(
                        WOODCUTTING_ID
                );

        if (woodcutting == null) {
            return;
        }

        SkillInstance woodcuttingData =
                PlayerDataManager
                        .get(player)
                        .getSkill(
                                woodcutting
                        );

        var config =
                ConfigManager.get()
                        .woodcutting
                        .abilities
                        .treeFeller;



        
        

        Block originalBlock =
                originalState.getBlock();

        /*
         * Find connected natural logs of the same type.
         */
        Set<BlockPos> logs =
                findConnectedLogs(
                        world,
                        originalPos,
                        originalBlock
                );

        if (logs.isEmpty()) {
            return;
        }

        UUID uuid =
                player.getUuid();

        PROCESSING.add(uuid);

        int brokenLogs = 0;

        /*
         * Track every removed log so we know where
         * to look for unsupported leaves afterwards.
         */
        Set<BlockPos> felledLogs =
                new HashSet<>();

        felledLogs.add(
                originalPos.toImmutable()
        );

        try {

            for (BlockPos target : logs) {

                /*
                 * The axe may have broken while processing
                 * earlier logs.
                 */
                ItemStack currentTool =
                        player.getMainHandStack();

                if (!currentTool.isIn(ItemTags.AXES)) {
                    break;
                }

                BlockState targetState =
                        world.getBlockState(
                                target
                        );

                if (!targetState.isIn(BlockTags.LOGS)) {
                    continue;
                }

                /*
                 * Don't remove player-placed logs.
                 */
                if (SkillforgeState
                        .get(world.getServer())
                        .isPlayerPlaced(
                                world,
                                target
                        )) {

                    continue;
                }

                if (!currentTool.isSuitableFor(
                        targetState
                )) {
                    continue;
                }

                /*
                 * Use Minecraft's normal server-side
                 * block-breaking system.
                 *
                 * This keeps:
                 * - normal drops
                 * - axe durability
                 * - Skillforge Woodcutting events
                 * - protection/plugin hooks where applicable
                 */
                boolean broken =
                        player.interactionManager
                                .tryBreakBlock(
                                        target
                                );

                if (!broken) {
                    continue;
                }

                brokenLogs++;

                felledLogs.add(
                        target.toImmutable()
                );

                /*
                 * Additional hunger cost.
                 */
                float exhaustion =
                        Math.max(
                                0.0f,
                                config.exhaustionPerExtraLog
                        );

                player.addExhaustion(
                        exhaustion
                );
            }

        } finally {

            PROCESSING.remove(uuid);
        }

        /*
         * Accelerate decay of leaves that were supported
         * by the tree we just removed.
         */
        if (!felledLogs.isEmpty()) {

            scheduleLeafDecay(
                    world,
                    felledLogs
            );
        }

        if (brokenLogs > 0) {

            player.sendMessage(
                    Text.literal(
                            "§6Tree Feller! §e"
                                    + (brokenLogs + 1)
                                    + " logs felled."
                    ),
                    true
            );
        }
    }

    private static Set<BlockPos> findConnectedLogs(
            ServerWorld world,
            BlockPos origin,
            Block originalBlock
    ) {

        Set<BlockPos> results =
                new HashSet<>();

        Set<Long> visited =
                new HashSet<>();

        Queue<BlockPos> queue =
                new ArrayDeque<>();

        /*
         * The original block is already gone because
         * Tree Feller runs from the AFTER break event.
         *
         * Start searching from its neighbours.
         */
        addNeighbours(
                origin,
                queue
        );

        int safetyLogLimit =
                Math.max(
                        1,
                        ConfigManager.get()
                                .woodcutting
                                .abilities
                                .treeFeller
                                .safetyLogLimit
                );

        while (!queue.isEmpty()
                && results.size() < safetyLogLimit) {

            BlockPos current =
                    queue.remove();

            if (!visited.add(
                    current.asLong()
            )) {
                continue;
            }

            BlockState state =
                    world.getBlockState(
                            current
                    );

            if (!state.isIn(BlockTags.LOGS)) {
                continue;
            }

            /*
             * Only follow the same log type as the
             * original tree.
             *
             * This reduces accidental chaining between
             * different neighbouring tree species.
             */
            if (state.getBlock()
                    != originalBlock) {

                continue;
            }

            /*
             * Player-placed logs aren't felled and also
             * aren't allowed to act as bridges to other logs.
             */
            if (SkillforgeState
                    .get(world.getServer())
                    .isPlayerPlaced(
                            world,
                            current
                    )) {

                continue;
            }

            results.add(
                    current.toImmutable()
            );

            /*
             * Continue traversing the tree.
             *
             * All 26 neighbouring positions are checked so
             * diagonal branches are discovered as well.
             */
            addNeighbours(
                    current,
                    queue
            );
        }

        return results;
    }

    private static void addNeighbours(
            BlockPos centre,
            Queue<BlockPos> queue
    ) {

        for (int x = -1; x <= 1; x++) {

            for (int y = -1; y <= 1; y++) {

                for (int z = -1; z <= 1; z++) {

                    if (x == 0
                            && y == 0
                            && z == 0) {

                        continue;
                    }

                    queue.add(
                            centre.add(
                                    x,
                                    y,
                                    z
                            )
                    );
                }
            }
        }
    }

    /*
     * Finds nearby non-persistent leaves and schedules
     * Minecraft to recalculate their distance from logs.
     *
     * We scan one bounding box around the entire felled
     * tree instead of scanning a radius around every log.
     */
    private static void scheduleLeafDecay(
            ServerWorld world,
            Set<BlockPos> felledLogs
    ) {

        var config =
                ConfigManager.get()
                        .woodcutting
                        .abilities
                        .treeFeller;

        if (!config.forceLeafDecay) {
            return;
        }

        if (felledLogs.isEmpty()) {
            return;
        }

        int radius =
                Math.max(
                        1,
                        config.leafDecayRadius
                );

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos logPos : felledLogs) {

            minX =
                    Math.min(
                            minX,
                            logPos.getX()
                    );

            minY =
                    Math.min(
                            minY,
                            logPos.getY()
                    );

            minZ =
                    Math.min(
                            minZ,
                            logPos.getZ()
                    );

            maxX =
                    Math.max(
                            maxX,
                            logPos.getX()
                    );

            maxY =
                    Math.max(
                            maxY,
                            logPos.getY()
                    );

            maxZ =
                    Math.max(
                            maxZ,
                            logPos.getZ()
                    );
        }

        BlockPos minimum =
                new BlockPos(
                        minX - radius,
                        minY - radius,
                        minZ - radius
                );

        BlockPos maximum =
                new BlockPos(
                        maxX + radius,
                        maxY + radius,
                        maxZ + radius
                );

        Set<BlockPos> leaves =
                new HashSet<>();

        for (BlockPos mutablePos :
                BlockPos.iterate(
                        minimum,
                        maximum
                )) {

            BlockState state =
                    world.getBlockState(
                            mutablePos
                    );

            /*
             * We specifically need LeavesBlock because
             * it exposes DISTANCE and PERSISTENT.
             */
            if (!(state.getBlock()
                    instanceof LeavesBlock)) {

                continue;
            }

            /*
             * Player-placed leaves are persistent and
             * must never be force-decayed.
             */
            if (state.get(
                    LeavesBlock.PERSISTENT
            )) {
                continue;
            }

            BlockPos leafPos =
                    mutablePos.toImmutable();

            leaves.add(
                    leafPos
            );

            /*
             * Ask vanilla to recalculate the leaf's
             * distance from supporting logs.
             */
            world.scheduleBlockTick(
                    leafPos,
                    state.getBlock(),
                    1
            );
        }

        if (leaves.isEmpty()) {
            return;
        }

        int delay =
                Math.max(
                        1,
                        config.leafDecayDelayTicks
                );

        PENDING_LEAF_DECAY.add(
                new PendingLeafDecay(
                        world,
                        leaves,
                        world.getTime()
                                + delay
                )
        );
    }

    private static void processPendingLeafDecay(
            ServerWorld world
    ) {

        if (PENDING_LEAF_DECAY.isEmpty()) {
            return;
        }

        long currentTick =
                world.getTime();

        Iterator<PendingLeafDecay> iterator =
                PENDING_LEAF_DECAY.iterator();

        while (iterator.hasNext()) {

            PendingLeafDecay pending =
                    iterator.next();

            /*
             * This decay job belongs to another dimension.
             */
            if (pending.world() != world) {
                continue;
            }

            if (currentTick
                    < pending.executeAt()) {

                continue;
            }

            processLeafDecay(
                    world,
                    pending.leaves()
            );

            iterator.remove();
        }
    }

    private static void processLeafDecay(
            ServerWorld world,
            Set<BlockPos> leaves
    ) {

        for (BlockPos pos : leaves) {

            BlockState state =
                    world.getBlockState(
                            pos
                    );

            /*
             * The block may already have naturally
             * decayed during the delay.
             */
            if (!(state.getBlock()
                    instanceof LeavesBlock)) {

                continue;
            }

            /*
             * Never remove persistent/player-placed leaves.
             */
            if (state.get(
                    LeavesBlock.PERSISTENT
            )) {
                continue;
            }

            /*
             * Vanilla has recalculated this leaf and
             * determined that it is still supported by
             * another nearby log.
             */
            if (state.get(
                    LeavesBlock.DISTANCE
            ) < LeavesBlock.MAX_DISTANCE) {

                continue;
            }

            /*
             * Remove with drops enabled so normal leaf
             * loot such as saplings, sticks and apples
             * can still be generated.
             */
            world.breakBlock(
                    pos,
                    true
            );
        }
    }

    public static int getDurationSeconds(
            int woodcuttingLevel
    ) {

        var config =
                ConfigManager.get()
                        .woodcutting
                        .abilities
                        .treeFeller;

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
                        woodcuttingLevel
                )
                        / levelsPerBonusSecond;

        int duration =
                baseDuration
                        + bonusSeconds;

        return Math.min(
                duration,
                config.maxDurationSeconds
        );
    }
}