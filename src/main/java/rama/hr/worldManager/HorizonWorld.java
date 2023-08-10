package rama.hr.worldManager;

import org.bukkit.World;

import java.util.HashMap;
import java.util.List;

public class HorizonWorld {

    World resetWorld;
    String backupWorld;
    List<String> prevCommands;
    List<String> afterCommands;
    String warnings;
    String warningsMessage;
    HashMap<Integer, String> times;

    public HorizonWorld(World resetWorld,
                        String backupWorld,
                        List<String> prevCommands,
                        List<String> afterCommands,
                        String warnings,
                        String warningsMessage,
                        HashMap<Integer, String> times){

        this.resetWorld = resetWorld;
        this.backupWorld = backupWorld;
        this.prevCommands = prevCommands;
        this.afterCommands = afterCommands;
        this.warnings = warnings;
        this.warningsMessage = warningsMessage;
        this.times = times;
    }

    public String getWorldName(){
        return resetWorld.getName();
    }



}
