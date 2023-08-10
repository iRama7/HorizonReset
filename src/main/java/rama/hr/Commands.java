package rama.hr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import rama.hr.worldManager.HorizonWorld;

import java.util.ArrayList;
import java.util.List;

import static rama.hr.HorizonReset.playerLogger;
import static rama.hr.HorizonReset.worldManager;

public class Commands implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 2){
            if (args[0].equalsIgnoreCase("reset")){
                String horizonWorldName = args[1];
                if(!worldManager.getHorizonWorlds().isEmpty()){
                    for(HorizonWorld horizonWorld : worldManager.getHorizonWorlds()){
                        if(horizonWorld.getWorldName().equalsIgnoreCase(horizonWorldName)){
                            playerLogger(sender, "&eTrying to reset the world " + horizonWorldName);
                            worldManager.restartWorld(horizonWorld);
                        }
                    }
                }else{
                    playerLogger( sender, "&cThere is no worlds loaded!");
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        if(sender.hasPermission("hr.admin")){
            if(args.length == 1){
                commands.add("reset");
                StringUtil.copyPartialMatches(args[0], commands, completions);
                return completions;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
                for(HorizonWorld horizonWorld : worldManager.getHorizonWorlds()){
                    commands.add(horizonWorld.getWorldName());
                    StringUtil.copyPartialMatches(args[1], commands, completions);
                    return completions;
                }
            }
        }
        return null;
    }
}
