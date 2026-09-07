package dev.olliesbrother.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.olliesbrother.Skillforge;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path CONFIG_DIRECTORY =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("Skillforge");

    private static final Path CONFIG_FILE =
            CONFIG_DIRECTORY.resolve("skillforge.json");

    private static SkillforgeConfig config;

    private ConfigManager() {
    }

    public static void load() {

        try {
            Files.createDirectories(CONFIG_DIRECTORY);

            if (!Files.exists(CONFIG_FILE)) {
                config = new SkillforgeConfig();
                save();

                Skillforge.LOGGER.info(
                        "Created default Skillforge config."
                );

                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                config = GSON.fromJson(
                        reader,
                        SkillforgeConfig.class
                );
            }

            if (config == null) {
                config = new SkillforgeConfig();
            }

            Skillforge.LOGGER.info(
                    "Loaded Skillforge config."
            );

        } catch (IOException exception) {

            Skillforge.LOGGER.error(
                    "Failed to load Skillforge config.",
                    exception
            );

            config = new SkillforgeConfig();
        }
    }

    public static void save() {

        try {
            Files.createDirectories(CONFIG_DIRECTORY);

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }

        } catch (IOException exception) {

            Skillforge.LOGGER.error(
                    "Failed to save Skillforge config.",
                    exception
            );
        }
    }

    public static boolean reload() {

        if (!Files.exists(CONFIG_FILE)) {
            Skillforge.LOGGER.error(
                    "Cannot reload Skillforge config because the config file does not exist."
            );
            return false;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {

            SkillforgeConfig newConfig =
                    GSON.fromJson(
                            reader,
                            SkillforgeConfig.class
                    );

            if (newConfig == null) {
                Skillforge.LOGGER.error(
                        "Cannot reload Skillforge config because the file is empty."
                );
                return false;
            }

            config = newConfig;

            Skillforge.LOGGER.info(
                    "Reloaded Skillforge config."
            );

            return true;

        } catch (Exception exception) {

            Skillforge.LOGGER.error(
                    "Failed to reload Skillforge config.",
                    exception
            );

            return false;
        }
    }

    public static SkillforgeConfig get() {
        return config;
    }
}