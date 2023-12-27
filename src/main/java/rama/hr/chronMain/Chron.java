package rama.hr.chronMain;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import rama.hr.worldManager.HorizonWorld;
import rama.hr.worldManager.WorldManager;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import static rama.hr.HorizonReset.*;

public class Chron {

    Cron cron;
    HorizonWorld chronWorld;
    WorldManager worldManager = new WorldManager();
    BukkitTask task;
    TimeZone timeZone;


    public Chron(String cronSyntax,
                 HorizonWorld chronWorld,
                 TimeZone timeZone){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        this.cron = parser.parse(cronSyntax);
        this.chronWorld = chronWorld;
        this.timeZone = timeZone;
    }

    public void start(){
        chronWorld.startWarnings(this);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        task = scheduler.runTaskLater(plugin, () -> {

            //Restart world
            worldManager.restartWorld(chronWorld);
            //Restart chron
            start();

        }, ((getTimeUntil() / 1000) * 20));
        if(debug) {
            consoleLogger("&eStarting chron for &f" + chronWorld.getWorldName() + "&e with time &f" + timeFormatted((getTimeUntil())), false);
        }
    }

    public Long nextExec(){
        return getTimeUntil();
    }

    private Long getTimeUntil(){

        ZonedDateTime now = convertToTimeZone(LocalDateTime.now(), timeZone.toZoneId());

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime nextExecution = (executionTime.nextExecution(now).get());


        long timeUntil = ChronoUnit.MILLIS.between(now, nextExecution);
        return timeUntil + 1000;
    }

    public void cancel(){
        task.cancel();
    }

    private ZonedDateTime convertToTimeZone(LocalDateTime localDateTime, ZoneId targetZoneId) {
        ZoneId sourceZoneId = ZoneId.systemDefault();
        ZonedDateTime sourceZonedDateTime = localDateTime.atZone(sourceZoneId);
        return sourceZonedDateTime.withZoneSameInstant(targetZoneId);
    }



}
