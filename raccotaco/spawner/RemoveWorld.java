/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raccotaco.spawner;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Difficulty;
import org.bukkit.World;
        
class RemoveWorld implements Runnable 
{
    DragonSpawner plugin;
    String worldname; 
    
    public RemoveWorld(DragonSpawner plugin) 
    {
        this.plugin = plugin;
    }

    @Override
    public void run() 
    {
        worldname = plugin.getConfig().getString("world").trim();
        MultiverseCore mvc = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        MultiverseWorld world = mvc.getMVWorldManager().getMVWorld(worldname);

        world.setDifficulty(Difficulty.HARD);
        world.setEnvironment(World.Environment.THE_END);
        world.setPVPMode(true);
        world.setAllowFlight(false);
        world.setPlayerLimit(-1);
        
        if (mvc.regenWorld(worldname, true, true, null))
            getLogger().info(worldname + " has regenerated!");
        else
            getLogger().info(worldname + " did not regenerate.");
        
        plugin.getConfig().set("respawn", "NONE");
        plugin.saveConfig();
    }
    
}
