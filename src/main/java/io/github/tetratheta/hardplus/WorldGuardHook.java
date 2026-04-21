package io.github.tetratheta.hardplus;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.tetratheta.hardplus.util.Perm;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WorldGuardHook {
  public static String WILDCARD_FLAG = "hardplus-all";
  private final Map<String, StateFlag> worldGuardFlags = new HashMap<>();

  public WorldGuardHook() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    try {
      StateFlag flag = new StateFlag(WILDCARD_FLAG, false);
      registry.register(flag);
      worldGuardFlags.put(WILDCARD_FLAG, flag);

      for (Perm perm : Perm.values()) {
        flag = new StateFlag(perm.flagName(), false);
        registry.register(flag);
        worldGuardFlags.put(perm.flagName(), flag);
      }
    } catch (Exception ignored) {
      Hardplus.logger.info(Component.text("Failed to register WorldGuard flags."));
    }
  }

  public boolean checkFlag(Player player, String flagName) {
    LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();

    boolean allow = query.testState(localPlayer.getLocation(), localPlayer, worldGuardFlags.get(flagName));

    StateFlag.State wildcardState =
        query.queryState(localPlayer.getLocation(), localPlayer, worldGuardFlags.get(WILDCARD_FLAG));
    if (wildcardState != null) {
      switch (wildcardState) {
        case ALLOW -> allow = true;
        case DENY -> {
          return false;
        }
      }
    }

    return allow;
  }
}
