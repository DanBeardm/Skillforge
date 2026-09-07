package dev.olliesbrother.player;

import dev.olliesbrother.persistence.SkillforgeState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerDataManager {

    private PlayerDataManager() {
    }

    public static PlayerSkillData get(
            ServerPlayerEntity player
    ) {

        MinecraftServer server = player.getServer();

        if (server == null) {
            throw new IllegalStateException(
                    "Cannot access Skillforge player data without a server."
            );
        }

        SkillforgeState state =
                SkillforgeState.get(server);

        return state.getPlayerData(
                player.getUuid()
        );
    }

    public static void markDirty(
            ServerPlayerEntity player
    ) {

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        SkillforgeState
                .get(server)
                .markDirty();
    }
}