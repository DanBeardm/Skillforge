package dev.olliesbrother.persistence;

import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkillforgeState extends PersistentState {

    private static final String SAVE_NAME = "skillforge_player_data";

    private final Map<UUID, PlayerSkillData> players = new HashMap<>();

    private final Map<String, Set<Long>> playerPlacedBlocks =
            new HashMap<>();

    public static final Type<SkillforgeState> TYPE =
            new Type<>(
                    SkillforgeState::new,
                    SkillforgeState::fromNbt,
                    null
            );

    public PlayerSkillData getPlayerData(UUID uuid) {

        PlayerSkillData existing = players.get(uuid);

        if (existing != null) {
            return existing;
        }

        PlayerSkillData created = new PlayerSkillData();
        players.put(uuid, created);

        markDirty();

        return created;
    }

    public static SkillforgeState get(MinecraftServer server) {

        ServerWorld overworld =
                server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException(
                    "Cannot access Skillforge data: overworld is not loaded."
            );
        }

        return overworld
                .getPersistentStateManager()
                .getOrCreate(TYPE, SAVE_NAME);
    }

    @Override
    public NbtCompound writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registryLookup
    ) {

        NbtCompound playersNbt = new NbtCompound();

        for (Map.Entry<UUID, PlayerSkillData> playerEntry
                : players.entrySet()) {

            NbtCompound playerNbt = new NbtCompound();
            NbtCompound skillsNbt = new NbtCompound();

            for (Map.Entry<Skill, SkillInstance> skillEntry
                    : playerEntry.getValue().getSkills().entrySet()) {

                Skill skill = skillEntry.getKey();
                SkillInstance instance = skillEntry.getValue();

                NbtCompound skillNbt = new NbtCompound();

                skillNbt.putInt(
                        "Level",
                        instance.getLevel()
                );

                skillNbt.putDouble(
                        "Experience",
                        instance.getExperience()
                );

                skillsNbt.put(
                        skill.getId().toString(),
                        skillNbt
                );
            }

            playerNbt.put("Skills", skillsNbt);

            playersNbt.put(
                    playerEntry.getKey().toString(),
                    playerNbt
            );
        }

        nbt.put("Players", playersNbt);

        NbtCompound placedBlocksNbt =
                new NbtCompound();

        for (Map.Entry<String, Set<Long>> entry
                : playerPlacedBlocks.entrySet()) {

            long[] positions =
                    entry.getValue()
                            .stream()
                            .mapToLong(Long::longValue)
                            .toArray();

            placedBlocksNbt.putLongArray(
                    entry.getKey(),
                    positions
            );
        }

        nbt.put(
                "PlayerPlacedBlocks",
                placedBlocksNbt
        );

        return nbt;
    }

    private static SkillforgeState fromNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registryLookup
    ) {

        SkillforgeState state = new SkillforgeState();

        NbtCompound playersNbt =
                nbt.getCompound("Players");

        for (String uuidString : playersNbt.getKeys()) {

            UUID uuid;

            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            NbtCompound playerNbt =
                    playersNbt.getCompound(uuidString);

            NbtCompound skillsNbt =
                    playerNbt.getCompound("Skills");

            PlayerSkillData playerData =
                    new PlayerSkillData();

            for (String skillIdString : skillsNbt.getKeys()) {

                Identifier skillId =
                        Identifier.tryParse(skillIdString);

                if (skillId == null) {
                    continue;
                }

                Skill skill =
                        SkillRegistry.get(skillId);

                if (skill == null) {
                    continue;
                }

                NbtCompound skillNbt =
                        skillsNbt.getCompound(skillIdString);

                int level =
                        Math.max(
                                1,
                                skillNbt.getInt("Level")
                        );

                double experience =
                        skillNbt.getDouble("Experience");

                playerData.setSkill(
                        skill,
                        new SkillInstance(
                                level,
                                experience
                        )
                );
            }

            state.players.put(uuid, playerData);
        }

        NbtCompound placedBlocksNbt =
                nbt.getCompound("PlayerPlacedBlocks");

        for (String dimension
                : placedBlocksNbt.getKeys()) {

            long[] positions =
                    placedBlocksNbt.getLongArray(
                            dimension
                    );

            Set<Long> positionSet =
                    new HashSet<>();

            for (long position : positions) {
                positionSet.add(position);
            }

            state.playerPlacedBlocks.put(
                    dimension,
                    positionSet
            );
        }

        return state;
    }
    public void markPlayerPlaced(
            ServerWorld world,
            BlockPos pos
    ) {

        String dimension =
                world.getRegistryKey()
                        .getValue()
                        .toString();

        playerPlacedBlocks
                .computeIfAbsent(
                        dimension,
                        ignored -> new HashSet<>()
                )
                .add(pos.asLong());

        markDirty();
    }

    public boolean removePlayerPlaced(
            ServerWorld world,
            BlockPos pos
    ) {

        String dimension =
                world.getRegistryKey()
                        .getValue()
                        .toString();

        Set<Long> positions =
                playerPlacedBlocks.get(dimension);

        if (positions == null) {
            return false;
        }

        boolean removed =
                positions.remove(pos.asLong());

        if (removed) {

            if (positions.isEmpty()) {
                playerPlacedBlocks.remove(dimension);
            }

            markDirty();
        }

        return removed;
    }
    public boolean isPlayerPlaced(
            ServerWorld world,
            BlockPos pos
    ) {

        String dimension =
                world.getRegistryKey()
                        .getValue()
                        .toString();

        Set<Long> positions =
                playerPlacedBlocks.get(dimension);

        if (positions == null) {
            return false;
        }

        return positions.contains(
                pos.asLong()
        );
    }
}