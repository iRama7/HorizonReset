package rama.hr;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rama.hr.worldManager.WorldManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HorizonReset extends JavaPlugin {

    public static Plugin plugin;

    public static WorldManager worldManager;

    public static boolean debug = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        //initialize debug mode
        debug = getConfig().getBoolean("config.debug-mode");
        plugin = this;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(colorized("&3------------[&b&lHORIZON &bReset&3]------------"));
        console.sendMessage(colorized(" "));
        consoleLogger("&eLoading the plugin...", false);
        console.sendMessage(colorized(" "));
        if(createBackupsFolder()){
            consoleLogger("&aCreated backups folder", false);
            console.sendMessage(colorized(" "));
        }

        worldManager = new WorldManager();

        try {
            worldManager.loadWorlds(this.getConfig());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        console.sendMessage(colorized(" "));

        worldManager.startChrons();

        enablePaPi();

        console.sendMessage(colorized(" "));

        consoleLogger("&eRegistering commands...", false);
        console.sendMessage(colorized(" "));
        registerCommand(this);

        console.sendMessage(colorized("&3------------[&b&lHORIZON &bReset&3]------------"));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void consoleLogger(String message, Boolean b){
        String prefix = ChatColor.translateAlternateColorCodes('&', "&3[&b&lHORIZON &bReset&3] &r");
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        if(b){
            console.sendMessage(prefix + colorized(message));
        }else{
            console.sendMessage(colorized(message));
        }
    }

    public static void playerLogger(CommandSender sender, String message){
        String prefix = ChatColor.translateAlternateColorCodes('&', "&b&lHORIZON &r");
        sender.sendMessage(prefix + colorized(message));
    }

    public static void broadcast(String message){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(colorized(message));
        }
    }

    public static String colorized(String s) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String hexCode = s.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            s = s.replace(hexCode, builder.toString());
            matcher = pattern.matcher(s);
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public boolean createBackupsFolder(){
        File backupsFolder = new File(plugin.getDataFolder(), "backups");
        if(!backupsFolder.exists()) {
            return backupsFolder.mkdirs();
        }else{
            return false;
        }
    }

    public void registerCommand(JavaPlugin plugin){
        TabExecutor command = new Commands();
        plugin.getCommand("hr").setExecutor(command);
        plugin.getCommand("hr").setTabCompleter(command);
    }

    public static String timeFormatted(Long ms){
        long days = TimeUnit.MILLISECONDS.toDays(ms);
        ms -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        ms -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        ms -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
        String time = colorized(plugin.getConfig().getString("config.time-format"));
        return String.format(time, days, hours, minutes, seconds);
    }

    public void enablePaPi(){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            consoleLogger("&aEnabling PlaceholderAPI support.", false);
            //PlaceHolder register
            new Placeholders().register();
        }else{
            consoleLogger("&ePlaceholderAPI not found, placeholders won't work.", false);
        }
    }
}
