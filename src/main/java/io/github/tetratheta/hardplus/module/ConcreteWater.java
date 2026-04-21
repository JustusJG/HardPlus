package io.github.tetratheta.hardplus.module;

import io.github.tetratheta.hardplus.util.Perm;
import io.github.tetratheta.hardplus.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConcreteWater implements Listener {
  final float minimumFallDistance;
  final double minimumVelocity;
  final double velocityDamageMultiplier;

  final Map<UUID, Float> lastFallDistance = new HashMap<>();

  public ConcreteWater(float minimumFallDistance, double minimumVelocity, double velocityDamageMultiplier) {
    this.minimumFallDistance = minimumFallDistance;
    this.minimumVelocity = minimumVelocity;
    this.velocityDamageMultiplier = velocityDamageMultiplier;
  }

  @EventHandler
  public void fallOnWater(PlayerMoveEvent event) {
    Player player = event.getPlayer();

    if (!PlayerUtil.checkPermGameMode(player, Perm.CONCRETE_WATER))
      return;
    float fallDistance = player.getFallDistance();
    if (fallDistance != 0)
      lastFallDistance.put(player.getUniqueId(), fallDistance);
    if (!player.getLocation().getBlock().getType().equals(Material.WATER))
      return;
    double velocity = Math.abs(player.getVelocity().getY());
    if (velocity < minimumVelocity)
      return;
    if (lastFallDistance.getOrDefault(player.getUniqueId(), 0f) < minimumFallDistance)
      return;

    double fallDamage = velocity * velocityDamageMultiplier;
    player.damage(fallDamage,
        DamageSource.builder(DamageType.FALL).withDamageLocation(player.getLocation()).build());
  }
}
