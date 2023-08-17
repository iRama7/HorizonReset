package rama.hr.worldManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import rama.hr.chronMain.Chron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static rama.hr.HorizonReset.*;

public class HorizonWorld {

    World resetWorld;
    String backupWorld;
    List<String> prevCommands;
    List<String> afterCommands;
    String warnings;
    String warningsMessage;
    HashMap<Integer, String> times;
    List<Chron> chrons = new ArrayList<>();

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

    public void buildChrons(){
        int count = 0;
        for(int i : times.keySet()){
            int day = i;
            String time[] = times.get(i).split(":");
            int hour = Integer.parseInt(time[0]);
            int minutes = Integer.parseInt(time[1]);
            Chron chron = new Chron(day, hour, minutes, this);
            chrons.add(chron);
            count+=1;
        }
        consoleLogger("&eAdding &f" + count + "&e chronometers for world &f" + resetWorld.getName(), false);
    }

    public List<Chron> getChrons(){
        return chrons;
    }

    public Long nextRestart(){
        Long nr = 0L;

        for(Chron c : chrons){
            if(nr == 0L){
                nr = c.nextExec();
            }else{
                if(c.nextExec() < nr){
                    nr = c.nextExec();
                }
            }
        }

        return nr;

    }

    public void startWarnings(Chron chron){

        String second = colorized(plugin.getConfig().getString("config.times-language.second"));
        String seconds = colorized(plugin.getConfig().getString("config.times-language.seconds"));
        String minute = colorized(plugin.getConfig().getString("config.times-language.minute"));
        String minutes = colorized(plugin.getConfig().getString("config.times-language.minutes"));
        String hour = colorized(plugin.getConfig().getString("config.times-language.hour"));
        String hours = colorized(plugin.getConfig().getString("config.times-language.hours"));
        String day = colorized(plugin.getConfig().getString("config.times-language.day"));
        String days = colorized(plugin.getConfig().getString("config.times-language.days"));

        String[] warningList = warnings.split(", ");
        BukkitScheduler scheduler = Bukkit.getScheduler();
        for(String warning : warningList){
            int time = Integer.parseInt(warning.replaceAll("[^0-9]", ""));
            String timeUnit = warning.replaceAll("[^A-Za-z]", "");
            Long delay;
            String timeString = null;
            switch (timeUnit){
                case "s":

                    if(time == 1){
                        timeString = 1 + " " + second;
                    }else{
                        timeString = time + " " + seconds;
                    }
                    delay = (long) time;
                    break;
                case "m":
                    if(time == 1){
                        timeString = 1 + " " + minute;
                    }else{
                        timeString = time + " " + minutes;
                    }
                    delay = (time * 60L);
                    break;
                case "h":
                    if(time == 1){
                        timeString = 1 + " " + hour;
                    }else{
                        timeString = time + " " + hours;
                    }
                    delay = time * 60L * 60;
                    break;
                case "d":
                    if(time == 1){
                        timeString = 1 + " " + day;
                    }else{
                        timeString = time + " " + days;
                    }
                    delay = time * 60L * 60 * 24;
                    break;
                default:
                    delay = 0L;
            }

            delay *= 1000;

            Bukkit.getConsoleSender().sendMessage(timeFormatted(chron.nextExec() - delay));

            String finalTimeString = timeString;
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                broadcast(colorized(this.warningsMessage.replace("$time", finalTimeString)));
            }, ((chron.nextExec() - delay) / 50));
        }
    }

}
