package rama.hr.worldManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import rama.hr.chronMain.Chron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static rama.hr.HorizonReset.consoleLogger;

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

}
