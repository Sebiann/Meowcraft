package moe.sebiann.meowcraft;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.*;
import net.minecraft.text.*;

import org.slf4j.*;

import java.util.Arrays;
import java.util.List;

public class Meowcraft implements ModInitializer {
    public static final String MOD_ID = "meowcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // List of valid options
    private static final List<String> VALID_OPTIONS = Arrays.asList("cat_ears", "dog_ears", "fox_ears", "cool_glasses", "heart_glasses", "orchid_crown", "heart_crown");

    @Override
    public void onInitialize() {
        LOGGER.info("This is Meowcraft!");

        // Register the ping command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("sebping")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        source.sendFeedback(() -> Text.literal("Pong!"), true);
                        return 1;
                    })
            );
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("reskin")
                    .requires(source -> source.hasPermissionLevel(0)) // Allow any player to use this command
                    .then(CommandManager.argument("option", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                // Provide suggestions for valid options
                                VALID_OPTIONS.forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(Meowcraft::runReskinCommand))
            );
        });
    }

    private static int runReskinCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        // Get the name of the player executing the command
        String playerName = source.getName();

        // Get the option argument
        String option = StringArgumentType.getString(context, "option");

        // Validate the option
        if (!VALID_OPTIONS.contains(option)) {
            throw new SimpleCommandExceptionType(Text.literal("Invalid option: " + option)).create();
        }

        // Construct the /item modify command
        String command = String.format("item modify entity %s armor.head {function:\"minecraft:set_components\", components: {item_model:\"cookie:%s\",equippable:{slot: \"head\"}}}", playerName, option);

        try {
            // Execute the /item modify command
            source.getServer().getCommandManager().executeWithPrefix(source, command);
            source.sendFeedback(() -> Text.literal("Successfully applied reskin: " + option), false);
        } catch (Exception e) {
            source.sendError(Text.literal("Error running reskin command: " + e.getMessage()));
        }

        return 1; // Command executed successfully
    }
}