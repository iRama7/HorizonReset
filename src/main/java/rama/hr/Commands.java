package rama.hr;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import rama.hr.chronMain.Chron;
import rama.hr.worldManager.HorizonWorld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static rama.hr.HorizonReset.*;




public class Commands implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!sender.hasPermission("hr.admin")){
            return false;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("setTeleport")){
                Location l = ((Player) sender).getLocation();
                plugin.getConfig().set("config.reset_teleport.location", l);
                plugin.saveConfig();
                plugin.reloadConfig();
                playerLogger(sender, "&aSuccessfully set the teleport location.");
            }
            if(args[0].equalsIgnoreCase("reload")){
                playerLogger(sender, "&eReloading HorizonReset...");
                plugin.reloadConfig();
                plugin.saveDefaultConfig();
                for(HorizonWorld horizonWorld : worldManager.getHorizonWorlds()){
                    for(Chron chron : horizonWorld.getChrons()){
                        chron.cancel();
                    }
                    horizonWorld.cancelWarnings();
                }
                try {
                    worldManager.loadWorlds(plugin.getConfig());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                worldManager.startChrons();
            }
        }

        if(args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                String horizonWorldName = args[1];
                if (!worldManager.getHorizonWorlds().isEmpty()) {
                    for (HorizonWorld horizonWorld : worldManager.getHorizonWorlds()) {
                        if (horizonWorld.getWorldName().equalsIgnoreCase(horizonWorldName)) {
                            playerLogger(sender, "&eTrying to reset the world " + horizonWorldName);
                            worldManager.restartWorld(horizonWorld);
                        }
                    }
                } else {
                    playerLogger(sender, "&cThere is no worlds loaded!");
                }
            }

            if (args[0].equalsIgnoreCase("nextExecution")) {
                String horizonWorldName = args[1];
                if (!worldManager.getHorizonWorlds().isEmpty()) {
                    for (HorizonWorld horizonWorld : worldManager.getHorizonWorlds()) {
                        if (horizonWorld.getWorldName().equalsIgnoreCase(horizonWorldName)) {
                            playerLogger(sender, "&eNext execution for &f" + horizonWorldName + "&e:");
                            playerLogger(sender, timeFormatted(horizonWorld.nextRestart()));
                        }
                    }
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
                commands.add("reload");
                commands.add("nextExecution");
                commands.add("reset");
                commands.add("setTeleport");
                StringUtil.copyPartialMatches(args[0], commands, completions);
                return completions;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
                for(HorizonWorld horizonWorld : worldManager.getHorizonWorlds()){
                    commands.add(horizonWorld.getWorldName());
                }
                StringUtil.copyPartialMatches(args[1], commands, completions);
                return completions;
            }

            if(args.length == 2 && args[0].equalsIgnoreCase("nextExecution")){
                for(HorizonWorld horizonWorld : worldManager.getHorizonWorlds()){
                    commands.add(horizonWorld.getWorldName());
                }
                StringUtil.copyPartialMatches(args[1], commands, completions);
                return completions;
            }

        }
        return null;
    }
}
