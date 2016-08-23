package raccotaco.spawner;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DragonSpawner extends JavaPlugin implements Listener
{
    public static DragonSpawner plugin;
    public World world;

    @Override
    public void onDisable()
    {
        Bukkit.getScheduler().cancelAllTasks();
    }

    @Override
    public void onEnable()
    {
        plugin = this;
        
	if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();
	saveConfig();
        
        if (!getConfig().getString("respawn").toLowerCase().matches("none"))
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            try 
            {
                Date date = dateFormat.parse(getConfig().getString("respawn"));
                long delay = TimeUnit.MILLISECONDS.toSeconds(date.getTime() - new Date().getTime()) * 20;
                Bukkit.getScheduler().runTaskLater(plugin, new RemoveWorld(plugin), delay);
            } 
            catch (ParseException ex) 
            {
                Logger.getLogger(DragonSpawner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e)
    {
        if (e.getEntity() instanceof EnderDragon)
        {
            e.setDroppedExp(e.getDroppedExp()/36);
            this.world = Bukkit.getWorld(getConfig().getString("world").trim());
            if ((e.getEntity().getWorld() == world))
            {
                Entity entity = e.getEntity();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "broadcast " + entity.getCustomName()+" has been defeated!");
                int min = getConfig().getInt("min-time-in-days");
                int max = getConfig().getInt("max-time-in-days");

                min = min * 86400;
                max = max * 86400;
                int num = new Random().nextInt(max-min) + min;

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(new Date().getTime() + (num * 1000)));

                getConfig().set("respawn", fullDate);
                saveConfig();

                try 
                {
                    Date date = dateFormat.parse(fullDate);
                    long delay = TimeUnit.MILLISECONDS.toSeconds(date.getTime() - new Date().getTime()) * 20;

                    Bukkit.getScheduler().runTaskLater(plugin, new RemoveWorld(plugin), delay);
                } 
                catch (ParseException ex) 
                {
                    Logger.getLogger(DragonSpawner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @EventHandler
    public void Endermen(final EntityDamageEvent event)
    {
        if (!plugin.getConfig().getBoolean("op-endermen"))
            return;
        if (event.getEntity().getWorld() == world && event.getEntity() instanceof Enderman)
        {
            event.setDamage(event.getDamage() * 2);
        }
    }
    
    @EventHandler
    public void onEnderDragonSpawn (CreatureSpawnEvent event) 
    {
        world = Bukkit.getWorld(getConfig().getString("world").trim());
        if (event.getEntity().getWorld() == world && (event.getEntity() instanceof EnderDragon))
        {
            EnderDragon dragon = (EnderDragon) event.getEntity();
            double health = new Random().nextInt(this.plugin.getConfig().getInt("dragon-max-health")-this.plugin.getConfig().getInt("dragon-min-health")) + this.plugin.getConfig().getInt("dragon-min-health");
            dragon.setMaxHealth(health);
            dragon.setHealth(dragon.getMaxHealth());
            if (plugin.getConfig().getBoolean("op-dragon"))
            {
                int dragType = new Random().nextInt(4);
                int fHealth = (int) dragon.getHealth();
                switch (dragType)
                {
                    case 0:
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2000000000, 4));
                        dragon.setCustomName("Cloud Dragon [" + fHealth +"]");
                        break;
                    case 1:
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2000000000, 4));
                        dragon.setCustomName("Mountain Dragon [" + fHealth +"]");
                        break;
                    case 2:
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2000000000, 4));
                        dragon.setCustomName("Fire Dragon [" + fHealth +"]");
                        break;
                    default:
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2000000000, 4));
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2000000000, 4));
                        dragon.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2000000000, 4));
                        dragon.setCustomName("Elder Dragon [" + fHealth +"]");
                        break;
                }
                dragon.setCustomNameVisible(true);
                getLogger().info(dragon.getCustomName() + " has spawned.");
            }
        }
    }
}
