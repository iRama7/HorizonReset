package rama.hr;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rama.hr.worldManager.HorizonWorld;

import static rama.hr.HorizonReset.timeFormatted;
import static rama.hr.HorizonReset.worldManager;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "HorizonReset";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ImRama";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }


    @Override
    public @Nullable String onPlaceholderRequest(Player player, String params){
        // %HorizonReset_<world name>%
        for(HorizonWorld world : worldManager.getHorizonWorlds()){
            if(params.equals(world.getWorldName())){
                return timeFormatted(world.nextRestart());
            }
        }
        return null;
    }
}
