package rama.hr.worldManager;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import rama.hr.chronMain.Chron;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rama.hr.HorizonReset.*;


public class WorldManager {


    List<HorizonWorld> horizonWorlds = new ArrayList<>();

    Long startTime;
    Long finishTime;

    BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

    int a;
    int b;
    int c;
    int d;
    int e;
    int f;
    int g;


    public void loadWorlds(FileConfiguration config) throws IOException {

        consoleLogger("&eLoading worlds...", false);

        int count = 0;
        for (String world : config.getConfigurationSection("worlds").getKeys(false)) {

            World resetWorld = Bukkit.getWorld(config.getString("worlds." + world + ".world"));
            if (resetWorld == null) {
                consoleLogger("&4[ERROR] &c'&f" + config.getString("worlds." + world + ".world") + "&c' is null, skipping configuration &f" + world + "&c.", false);
                continue;
            }

            String backupWorld = config.getString("worlds." + world + ".backup");
            if (!(new File("plugins/HorizonReset/backups/" + backupWorld).exists())) {
                consoleLogger("&4[ERROR] &c'&f" + backupWorld + "&c' folder does not exist, skipping configuration &f" + world + "&c.", false);
                continue;
            }

            List<String> prevCommands = config.getStringList("worlds." + world + ".prev_reset_commands");
            List<String> afterCommands = config.getStringList("worlds." + world + ".after_reset_commands");

            String warnings = config.getString("worlds." + world + ".warnings");

            String warningsMessage = config.getString("worlds." + world + ".warnings_message");

            HashMap<Integer, String> times = new HashMap<>();
            for (String day : config.getConfigurationSection("worlds." + world + ".times").getKeys(false)) {
                String hour = config.getString("worlds." + world + ".times." + day + ".hour");
                times.put(Integer.valueOf(day), hour);
            }
            if (times.isEmpty()) {
                consoleLogger("&4[ERROR] &cTimes is empty, skipping configuration &f" + world + "&c.", false);
                continue;
            }
            HorizonWorld horizonWorld = new HorizonWorld(resetWorld, backupWorld, prevCommands, afterCommands, warnings, warningsMessage, times);
            horizonWorlds.add(horizonWorld);
            horizonWorld.buildChrons();
            count += 1;
        }

        consoleLogger("&aSuccessful loaded " + count + " worlds!", false);

    }

    public void restartWorld(HorizonWorld horizonWorld) {
        consoleLogger("&3------------[&6World Reset&3]------------", false);
        FileConfiguration config = plugin.getConfig();
        startTime = System.currentTimeMillis();
        int delay = config.getInt("config.time-between-actions");
        BukkitScheduler s = Bukkit.getScheduler();
        String world_name = horizonWorld.resetWorld.getName();

        consoleLogger("&eResetting world &f" + world_name, false);
        consoleLogger(" ", false);
        consoleLogger("&eProgress: [&f----------&e] &f0%", false);

        //Execute prev commands
        a = s.runTaskLater(plugin, () -> {
            executeCommands(horizonWorld.prevCommands, world_name, true);
        }, 0).getTaskId();


        //Unload the world
        b = s.runTaskLater(plugin, () -> {
            unloadWorld(world_name);
        }, delay).getTaskId();


        //Delete world folder

        c = s.runTaskLater(plugin, () -> {
            try {
                deleteWorld(world_name);
            } catch (IOException e) {
                consoleLogger("&4[ERROR] &cWorld folder " + world_name + " couldn't be deleted, is it still loaded?", false);
                throw new RuntimeException(e);
                }
            }, delay * 2L).getTaskId();


        //Copy backup

        d = s.runTaskLater(plugin, () -> {
            try {
                copyBackupFolder(horizonWorld.backupWorld, world_name);
            } catch (IOException e) {
                throw new RuntimeException(e);
                }
            }, delay * 3L).getTaskId();


        //Create world

        e = s.runTaskLater(plugin, () -> {
            createWorld(world_name, horizonWorld.resetWorld.getEnvironment());
            }, delay * 4L).getTaskId();

        //Execute after commands
        g = s.runTaskLater(plugin, () -> {
            executeCommands(horizonWorld.afterCommands, world_name, false);
        }, delay * 5L).getTaskId();


        f = s.runTaskLater(plugin, () -> {
            consoleLogger("&e&3------------[&6World Reset&3]------------", false);
            },  delay * 5L).getTaskId();
    }

    private void executeCommands(List<String> commands, String world_name, Boolean b) {
        if(b){
            consoleLogger("&eProgress: [&f#---------&e] &f10%", false);
        }
        for (String cmd : commands) {
            if (cmd.contains("[BROADCAST]")) {
                cmd = cmd.replace("[BROADCAST] ", "");
                broadcast(cmd);
            }else if(cmd.contains("[PLAYER]")){
                cmd = cmd.replace("[PLAYER] ", "");
                for(Player p : Bukkit.getWorld(world_name).getPlayers()){
                    p.chat("/"+cmd);
                }
            }else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
        if(b) {
            consoleLogger("&eProgress: [&f##--------&e] &f20%", false);
        }
    }

    private void unloadWorld(String world) {
        consoleLogger("&eProgress: [&f###-------&e] &f30%", false);
        if(!Bukkit.getWorld(world).getPlayers().isEmpty()){
            consoleLogger("&e[WARNING] &eThere are still players in the world &f" + world + "&e.", false);
        }
        boolean bool = Bukkit.unloadWorld(Bukkit.getWorld(world), false);
        if (bool) {
            consoleLogger("&eProgress: [&f####------&e] &f40%", false);
        } else {
            consoleLogger("&4[ERROR] &cWorld " + world + " couldn't be unloaded. Cancelling all tasks.", false);
            bukkitScheduler.cancelTask(a);
            bukkitScheduler.cancelTask(b);
            bukkitScheduler.cancelTask(c);
            bukkitScheduler.cancelTask(d);
            bukkitScheduler.cancelTask(e);
            bukkitScheduler.cancelTask(f);
            bukkitScheduler.cancelTask(g);
        }
    }

    private void deleteWorld(String world) throws IOException {
        consoleLogger("&eProgress: [&f#####-----&e] &f50%", false);
        File world_folder = new File(Bukkit.getWorldContainer() + "/" + world);
        boolean b = deleteFolder(world_folder);
        if(b) {
            consoleLogger("&eProgress: [&f######----&e] &f60%", false);
        }else{
            consoleLogger("&4[ERROR] &cWorld folder " + world + " couldn't be deleted.", false);
        }

    }

    private Boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        return folder.delete();
    }

    private void copyBackupFolder(String backup_name, String world_name) throws IOException {
        consoleLogger("&eProgress: [&f#######---&e] &f70%", false);
        FileUtils.copyDirectory(new File("plugins/HorizonReset/backups/" + backup_name), new File(world_name));
        consoleLogger("&eProgress: [&f########--&e] &f80%", false);
    }

    private void createWorld(String world_name, World.Environment environment) {
        consoleLogger("&eProgress: [&f#########-&e] &f90%", false);
        World world = Bukkit.createWorld(new WorldCreator(world_name).environment(environment));
        world.setKeepSpawnInMemory(false);
        consoleLogger("&eProgress: [&f##########&e] &f100%", false);
        finishTime = System.currentTimeMillis();
        consoleLogger(" ", false);
        consoleLogger("&aWorld reset done " + msToS(finishTime - startTime), false);
    }

    private String msToS(long ms) {
        double seconds = ms / 1000.0;
        return String.format("(%.3fs)!", seconds);
    }

    public List<HorizonWorld> getHorizonWorlds(){
        return horizonWorlds;
    }

    public Boolean startChrons(){
        boolean b = false;
        for(HorizonWorld hw : horizonWorlds){
            consoleLogger(colorized("&eStarting chrons for world &f" + hw.getWorldName()), false);
            consoleLogger(" ", false);
            for(Chron c : hw.getChrons()){
                c.start();
            }
            if(debug) {
                consoleLogger(" ", false);
            }
            b = true;
        }
        return b;
    }
}
