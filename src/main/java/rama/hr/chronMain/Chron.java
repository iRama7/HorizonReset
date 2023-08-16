package rama.hr.chronMain;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import rama.hr.worldManager.HorizonWorld;
import rama.hr.worldManager.WorldManager;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static rama.hr.HorizonReset.*;

public class Chron {

    int day;
    int hour;
    int minutes;
    HorizonWorld chronWorld;
    WorldManager worldManager = new WorldManager();


    public Chron(int day,
                 int hour,
                 int minutes,
                 HorizonWorld chronWorld){

        this.day = day;
        this.hour = hour;
        this.minutes = minutes;
        this.chronWorld = chronWorld;
    }

    public void start(){
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, () -> {

            //Restart world
            worldManager.restartWorld(chronWorld);
            //Restart chron
            start();

        }, ((getTimeUntil(DayOfWeek.of(day), hour, minutes)) / 1000) * 20);
        if(debug) {
            consoleLogger("&eStarting chron for &f" + chronWorld.getWorldName() + "&e with time &f" + timeFormatted((getTimeUntil(DayOfWeek.of(day), hour, minutes))), false);
        }
    }

    public Long nextExec(){
        return getTimeUntil(DayOfWeek.of(day), hour, minutes);
    }

    public Long getTimeUntil(DayOfWeek day, int hour, int minutes){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = now.with(DayOfWeek.from(day)).withHour(hour).withMinute(minutes).withSecond(0).withNano(0);

        if(targetDateTime.isBefore(now)){
            targetDateTime = targetDateTime.plusWeeks(1); //Sumar una semana si el día ya paso
        }

        long timeUntil = ChronoUnit.MILLIS.between(now, targetDateTime);
        return timeUntil + 1000;
    }


}
