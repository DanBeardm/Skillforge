package dev.olliesbrother.commands;

import dev.olliesbrother.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public final class SkillforgeCommand {

    private SkillforgeCommand() {
    }

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            CommandManager.literal("skillforge")

                                    .then(
                                            CommandManager.literal("reload")

                                                    .requires(
                                                            source ->
                                                                    source.hasPermissionLevel(3)
                                                    )

                                                    .executes(context -> {

                                                        boolean success =
                                                                ConfigManager.reload();

                                                        if (success) {

                                                            context.getSource()
                                                                    .sendFeedback(
                                                                            () -> Text.literal(
                                                                                    "§aSkillforge configuration reloaded."
                                                                            ),
                                                                            false
                                                                    );

                                                            return 1;
                                                        }

                                                        context.getSource()
                                                                .sendError(
                                                                        Text.literal(
                                                                                "§cFailed to reload Skillforge configuration. Check the server console."
                                                                        )
                                                                );

                                                        return 0;
                                                    })
                                    )
                    );
                }
        );
    }
}