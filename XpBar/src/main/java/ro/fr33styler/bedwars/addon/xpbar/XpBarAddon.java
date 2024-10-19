package ro.fr33styler.bedwars.addon.xpbar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import ro.fr33styler.bedwars.api.game.gamer.BedwarsStatistics;
import ro.fr33styler.gameengine.api.addon.Addon;
import ro.fr33styler.gameengine.api.addon.annotation.Description;
import ro.fr33styler.gameengine.api.config.annotation.Setting;
import ro.fr33styler.gameengine.api.config.annotation.bounded.BoundedInteger;
import ro.fr33styler.gameengine.api.config.serialize.Configuration;
import ro.fr33styler.gameengine.api.game.GameManager;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Description(id = "XpBar", author = "Fr33styler", version = "1.02")
public class XpBarAddon extends Addon implements Configuration {

    private BukkitTask task;
    private GameManager manager;

    @Setting("refresh-rate")
    @BoundedInteger(min = 1)
    private int refreshRate = 1;

    @Setting("blacklist")
    private Set<String> blacklist = new HashSet<>(List.of("world_name"));

    @Override
    public void onLoad() {
        getEngine().getConfigManager().tryLoad(this, new File(getDataFolder(), "config.yml"));
        manager = getEngine().getGameManagerRegistry().get("bedwars");
        if (manager != null) {
            Plugin plugin = getEngine().getPlugin();
            task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::onRun, 0, refreshRate * 20L);
        }
    }

    private void onRun() {
        getEngine().getUsers().forEachUser(user -> {
            Player player = user.toPlayer();
            if (!user.isInGame() && !blacklist.contains(player.getWorld().getName())) {
                BedwarsStatistics statistics = (BedwarsStatistics) manager.getData(player.getUniqueId());
                if (statistics == null) return;

                float experience = statistics.getExperience();
                float requiredExperience = statistics.getRequiredExperience();
                player.setLevel(statistics.getLevels());
                player.setExp(Math.min(1, experience / requiredExperience));
            }
        });
    }

    @Override
    public void onUnload() {
        blacklist.clear();
        task.cancel();
    }

}
