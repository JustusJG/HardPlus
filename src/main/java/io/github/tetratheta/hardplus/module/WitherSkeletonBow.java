package io.github.tetratheta.hardplus.module;

import io.github.tetratheta.hardplus.util.Perm;
import io.github.tetratheta.hardplus.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Random;

@SuppressWarnings("unused")
public class WitherSkeletonBow implements Listener {
  final int arrowWitherLevel;
  final int bowDamageLevel;
  final int bowKnockbackLevel;
  final double bowWSSpawnChance;
  final NamespacedKey witherSkeletonKey = new NamespacedKey("hardplus", "wither-skeleton");
  final NamespacedKey witherSkeletonArrowKey = new NamespacedKey("hardplus", "wither-skeleton-arrow");
  final Random random = new Random();

  public WitherSkeletonBow(double bowSpawnChance, int arrowDamageLevel, int arrowKnockbackLevel,
                           int arrowWitherLevel) {
    this.bowWSSpawnChance = bowSpawnChance;
    this.bowDamageLevel = arrowDamageLevel;
    this.bowKnockbackLevel = arrowKnockbackLevel;

    this.arrowWitherLevel = arrowWitherLevel;
  }

  // 1. First we need to spawn an HP-WitherSkeleton
  @EventHandler
  public void onWitherSkeletonSpawn(CreatureSpawnEvent e) {
    if (!(e.getEntity() instanceof WitherSkeleton witherSkeleton)) return;
    if (random.nextDouble() * 100 >= bowWSSpawnChance) return;

    witherSkeleton.getPersistentDataContainer().set(witherSkeletonKey, PersistentDataType.BOOLEAN, true);
    EntityEquipment mobInventory = witherSkeleton.getEquipment();
    ItemStack bow = new ItemStack(Material.BOW);
    ItemMeta bowMeta = bow.getItemMeta();
    bowMeta.addEnchant(Enchantment.POWER, bowDamageLevel, true);
    bowMeta.addEnchant(Enchantment.FLAME, 1, true);
    bowMeta.addEnchant(Enchantment.PUNCH, bowKnockbackLevel, true);
    bow.setItemMeta(bowMeta);
    mobInventory.setItemInMainHand(bow);
    mobInventory.setItemInMainHandDropChance(0);
  }

  // 2. The HP-WitherSkeleton is "friendly" to non-HP players
  //  and changes it's weapon accordingly
  @EventHandler
  public void onTargetChange(EntityTargetLivingEntityEvent e) {
    if (!(e.getEntity() instanceof WitherSkeleton witherSkeleton)) return;
    Boolean isHPWitherSkeleton =
        witherSkeleton.getPersistentDataContainer().get(witherSkeletonKey, PersistentDataType.BOOLEAN);
    if (Boolean.FALSE.equals(isHPWitherSkeleton)) return;
    if (witherSkeleton.getTarget() == null || !(witherSkeleton.getTarget() instanceof Player targetedPlayer)) return;

    if (PlayerUtil.checkPermGameMode(targetedPlayer, Perm.WITHER_SKELETON_BOW)) {
      giveHpEquipment(witherSkeleton);
    } else {
      giveFriendlyEquipment(witherSkeleton);
    }
  }

  // 3. If an HP-WitherSkeleton shots an arrow we'll add the wither effect,
  //  also we'll track it this will be helpful to save non-HP players from "ricochet"
  @EventHandler
  public void onWitherSkeletonShoot(EntityShootBowEvent e) {
    if (!(e.getEntity() instanceof WitherSkeleton witherSkeleton)) return;
    Boolean isHPWitherSkeleton =
        witherSkeleton.getPersistentDataContainer().get(witherSkeletonKey, PersistentDataType.BOOLEAN);
    if (Boolean.FALSE.equals(isHPWitherSkeleton)) return;
    if (!(e.getProjectile() instanceof Arrow arrow)) return;
    arrow.getPersistentDataContainer().set(witherSkeletonArrowKey, PersistentDataType.BOOLEAN, true);
    arrow.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 800, arrowWitherLevel), true);
    e.setProjectile(arrow);
  }

  // 4. Save non-HP players from "ricochet"
  @EventHandler
  public void onNonHPPlayerHit(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Arrow arrow)) return;
    Boolean isHPWitherArrow =
        arrow.getPersistentDataContainer().get(witherSkeletonKey, PersistentDataType.BOOLEAN);
    if (Boolean.FALSE.equals(isHPWitherArrow)) return;
    if (!(e.getEntity() instanceof Player player)) return;
    if (PlayerUtil.checkPermGameMode(player, Perm.WITHER_SKELETON_BOW)) return;
    // Remove Wither effect
    arrow.clearCustomEffects();
    // Decrease increased damage with Power
    double originalDamage = e.getDamage();
    double newDamage = originalDamage - (originalDamage * 0.25 * (bowDamageLevel + 1));
    e.setDamage(newDamage);
    // Set the fire back to whatever it was when the arrow hit
    int playerFire = player.getFireTicks();
    System.out.println(playerFire);
  }

  private void giveHpEquipment(WitherSkeleton witherSkeleton) {
    EntityEquipment mobInventory = witherSkeleton.getEquipment();
    ItemStack bow = new ItemStack(Material.BOW);
    ItemMeta bowMeta = bow.getItemMeta();
    bowMeta.addEnchant(Enchantment.POWER, bowDamageLevel, true);
    bowMeta.addEnchant(Enchantment.FLAME, 1, true);
    bowMeta.addEnchant(Enchantment.PUNCH, bowKnockbackLevel, true);
    bow.setItemMeta(bowMeta);
    mobInventory.setItemInMainHand(bow);
    mobInventory.setItemInMainHandDropChance(0);
  }

  private void giveFriendlyEquipment(WitherSkeleton witherSkeleton) {
    EntityEquipment mobInventory = witherSkeleton.getEquipment();
    ItemStack stoneSword = new ItemStack(Material.STONE_SWORD);
    mobInventory.setItemInMainHand(stoneSword);
    mobInventory.setItemInMainHandDropChance(0);
  }
}
