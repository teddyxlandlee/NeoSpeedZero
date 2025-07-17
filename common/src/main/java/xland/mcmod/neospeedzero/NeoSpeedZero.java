package xland.mcmod.neospeedzero;

import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.command.NeoSpeedCommands;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;
import xland.mcmod.neospeedzero.view.ViewPackets;

public final class NeoSpeedZero {
    private NeoSpeedZero() {}

    public static final String MOD_ID = "neospeedzero";

    public static void init() {
        // Events
        NeoSpeedLifecycle.register();
        // Resources
        SpeedrunGoalManager.registerEvents();
        // Game rules - load class
        NeoSpeedGameRules.LOGGER.debug("Registering game rules for {}", MOD_ID);
        // Network
        ViewPackets.register();
        // Speedrun Difficulties
        SpeedrunDifficulties.registerBuiltins();
        // Commands
        NeoSpeedCommands.register();
    }
}
