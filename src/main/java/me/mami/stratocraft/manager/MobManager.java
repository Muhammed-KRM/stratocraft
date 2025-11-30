package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

public class MobManager {

    public void spawnHellDragon(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        dragon.setCustomName("§4Cehennem Ejderi");
        dragon.setSize(20);
        if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
        }
        dragon.setHealth(200.0);
    }

    public void spawnTerrorWorm(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        Silverfish worm = (Silverfish) loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        worm.setCustomName("§8Toprak Solucanı");
        if (worm.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            worm.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        }
        worm.setHealth(100.0);
    }

    public void spawnWarBear(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.PolarBear bear = (org.bukkit.entity.PolarBear) loc.getWorld().spawnEntity(loc,
                EntityType.POLAR_BEAR);
        bear.setCustomName("§7Savaş Ayısı");
        if (bear.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
        }
        bear.setHealth(150.0);
        // PolarBear tamed edilemez, sadece custom name ile işaretliyoruz
    }

    public void spawnShadowPanther(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Cat panther = (org.bukkit.entity.Cat) loc.getWorld().spawnEntity(loc, EntityType.CAT);
        panther.setCustomName("§8Gölge Panteri");
        if (panther.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            panther.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
        }
        panther.setHealth(80.0);
        if (owner != null) {
            panther.setTamed(true);
            panther.setOwner(owner);
        }
    }

    public void spawnWyvern(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        Phantom wyvern = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        wyvern.setCustomName("§bWyvern");
        wyvern.setSize(15);
        if (wyvern.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            wyvern.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(250.0);
        }
        wyvern.setHealth(250.0);
    }

    public void spawnFireAmphiptere(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Blaze amphiptere = (org.bukkit.entity.Blaze) loc.getWorld().spawnEntity(loc,
                EntityType.BLAZE);
        amphiptere.setCustomName("§6Ateş Amfibiterü");
        if (amphiptere.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            amphiptere.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(120.0);
        }
        amphiptere.setHealth(120.0);
        if (amphiptere.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            amphiptere.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
        }
    }

    // ========== SIK GELEN CANAVARLAR (10 tane) ==========

    public void spawnGoblin(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Zombie goblin = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        goblin.setCustomName("§2Goblin");
        goblin.setBaby(true); // Küçük boyut
        if (goblin.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            goblin.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
        }
        if (goblin.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            goblin.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35); // Hızlı
        }
        goblin.setHealth(30.0);
    }

    public void spawnOrk(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Zombie ork = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        ork.setCustomName("§cOrk");
        if (ork.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            ork.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
        }
        if (ork.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            ork.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8.0); // Güçlü
        }
        ork.setHealth(80.0);
        // Zırh ekle
        if (ork.getEquipment() != null) {
            org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(
                    org.bukkit.Material.LEATHER_HELMET);
            ork.getEquipment().setHelmet(helmet);
        }
    }

    public void spawnTroll(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Zombie troll = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        troll.setCustomName("§5Troll");
        if (troll.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            troll.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(120.0);
        }
        if (troll.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            troll.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2); // Yavaş
        }
        if (troll.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            troll.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
        }
        troll.setHealth(120.0);
        troll.setAdult();
    }

    public void spawnSkeletonKnight(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Skeleton knight = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(loc,
                EntityType.SKELETON);
        knight.setCustomName("§7İskelet Şövalye");
        if (knight.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            knight.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60.0);
        }
        knight.setHealth(60.0);
        // Tam zırh
        if (knight.getEquipment() != null) {
            knight.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
            knight.getEquipment()
                    .setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
            knight.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_LEGGINGS));
            knight.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_BOOTS));
            knight.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD));
        }
    }

    public void spawnDarkMage(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Witch mage = (org.bukkit.entity.Witch) loc.getWorld().spawnEntity(loc, EntityType.WITCH);
        mage.setCustomName("§5Karanlık Büyücü");
        if (mage.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            mage.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(50.0);
        }
        mage.setHealth(50.0);
    }

    public void spawnWerewolf(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Wolf wolf = (org.bukkit.entity.Wolf) loc.getWorld().spawnEntity(loc, EntityType.WOLF);
        wolf.setCustomName("§cKurt Adam");
        wolf.setAngry(true);
        if (wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(70.0);
        }
        if (wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(6.0);
        }
        wolf.setHealth(70.0);
    }

    public void spawnGiantSpider(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Spider spider = (org.bukkit.entity.Spider) loc.getWorld().spawnEntity(loc, EntityType.SPIDER);
        spider.setCustomName("§8Dev Örümcek");
        if (spider.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            spider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60.0);
        }
        if (spider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            spider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
        }
        spider.setHealth(60.0);
        spider.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0, false, false));
    }

    public void spawnMinotaur(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Cow minotaur = (org.bukkit.entity.Cow) loc.getWorld().spawnEntity(loc, EntityType.COW);
        minotaur.setCustomName("§6Minotaur");
        if (minotaur.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            minotaur.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        }
        if (minotaur.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            minotaur.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(9.0);
        }
        minotaur.setHealth(100.0);
        // Boyut artır (scale attribute yoksa büyük model kullan)
    }

    public void spawnHarpy(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Parrot harpy = (org.bukkit.entity.Parrot) loc.getWorld().spawnEntity(loc, EntityType.PARROT);
        harpy.setCustomName("§eHarpy");
        if (harpy.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            harpy.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        }
        harpy.setHealth(40.0);
        harpy.setAI(true);
    }

    public void spawnBasilisk(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Silverfish basilisk = (org.bukkit.entity.Silverfish) loc.getWorld().spawnEntity(loc,
                EntityType.SILVERFISH);
        basilisk.setCustomName("§2Basilisk");
        if (basilisk.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            basilisk.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(90.0);
        }
        basilisk.setHealth(90.0);
        basilisk.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));
    }

    // ========== NADİR CANAVARLAR (10 tane) ==========

    public void spawnDragon(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        dragon.setCustomName("§4§lEJDERHA");
        dragon.setSize(25); // Çok büyük
        if (dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        }
        dragon.setHealth(500.0);
    }

    public void spawnTRex(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Ravager trex = (org.bukkit.entity.Ravager) loc.getWorld().spawnEntity(loc,
                EntityType.RAVAGER);
        trex.setCustomName("§c§lT-REX");
        if (trex.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            trex.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400.0);
        }
        if (trex.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            trex.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
        }
        trex.setHealth(400.0);
    }

    public void spawnCyclops(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Giant cyclops = (org.bukkit.entity.Giant) loc.getWorld().spawnEntity(loc, EntityType.GIANT);
        cyclops.setCustomName("§6§lTEK GÖZLÜ DEV");
        if (cyclops.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            cyclops.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        }
        if (cyclops.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            cyclops.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(12.0);
        }
        cyclops.setHealth(300.0);
        // GENERIC_SCALE 1.20.4'te mevcut değil, 1.21+ için gerekli
    }

    public void spawnGriffin(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        Phantom griffin = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        griffin.setCustomName("§e§lGriffin");
        griffin.setSize(20);
        if (griffin.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            griffin.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(350.0);
        }
        griffin.setHealth(350.0);
    }

    public void spawnWraith(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Vex wraith = (org.bukkit.entity.Vex) loc.getWorld().spawnEntity(loc, EntityType.VEX);
        wraith.setCustomName("§8§lHayalet");
        if (wraith.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            wraith.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
        }
        wraith.setHealth(150.0);
        wraith.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
    }

    public void spawnLich(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Skeleton lich = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(loc,
                EntityType.SKELETON);
        lich.setCustomName("§5§lLich");
        if (lich.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            lich.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(250.0);
        }
        lich.setHealth(250.0);
        if (lich.getEquipment() != null) {
            lich.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_HELMET));
            lich.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW));
        }
    }

    public void spawnKraken(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Squid kraken = (org.bukkit.entity.Squid) loc.getWorld().spawnEntity(loc, EntityType.SQUID);
        kraken.setCustomName("§1§lKraken");
        if (kraken.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            kraken.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400.0);
        }
        kraken.setHealth(400.0);
    }

    public void spawnPhoenix(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Blaze phoenix = (org.bukkit.entity.Blaze) loc.getWorld().spawnEntity(loc, EntityType.BLAZE);
        phoenix.setCustomName("§c§lPhoenix");
        if (phoenix.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            phoenix.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        }
        phoenix.setHealth(300.0);
        phoenix.setFireTicks(Integer.MAX_VALUE); // Sürekli yanıyor
    }

    public void spawnHydra(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        // EnderDragon normal dünyada spawn edilemez, bu yüzden Phantom kullanıyoruz
        Phantom hydra = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        hydra.setCustomName("§5§lHydra");
        hydra.setSize(30); // Çok büyük
        if (hydra.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            hydra.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(600.0);
        }
        hydra.setHealth(600.0);
    }

    public void spawnBehemoth(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.Ravager behemoth = (org.bukkit.entity.Ravager) loc.getWorld().spawnEntity(loc,
                EntityType.RAVAGER);
        behemoth.setCustomName("§4§lBehemoth");
        if (behemoth.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            behemoth.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        }
        if (behemoth.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            behemoth.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20.0);
        }
        behemoth.setHealth(500.0);
    }

    // TİTAN GOLEM (Yürüyen Dağ) - Dev boyutlu, etrafına toprak fırlatan golem
    public void spawnTitanGolem(Location loc, Player owner) {
        if (loc == null || loc.getWorld() == null)
            return;
        org.bukkit.entity.IronGolem golem = (org.bukkit.entity.IronGolem) loc.getWorld().spawnEntity(loc,
                EntityType.IRON_GOLEM);
        golem.setCustomName("§6§lTitan Golem");

        // 4 kat büyüt (GENERIC_SCALE 1.20.4'te mevcut değil, bu yüzden görsel olarak
        // büyük görünmesi için health ve damage artırıyoruz)
        if (golem.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400.0); // Çok dayanıklı
        }
        if (golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(25.0); // Çok güçlü
        }
        golem.setHealth(400.0);

        // Her 5 saniyede bir özel yetenekler kullanma görevi
        org.bukkit.scheduler.BukkitRunnable abilityTask = new org.bukkit.scheduler.BukkitRunnable() {
            private int abilityCounter = 0; // Hangi yeteneği kullanacağını belirle

            @Override
            public void run() {
                // Golem hala hayattaysa ve dünyada varsa
                if (golem == null || !golem.isValid() || golem.isDead()) {
                    cancel();
                    return;
                }

                abilityCounter++;
                int abilityType = abilityCounter % 3; // 3 yetenek: 0=Toprak Fırlatma, 1=Zıplama, 2=Şok Dalgası

                switch (abilityType) {
                    case 0: // Toprak Fırlatma
                        // Etrafındaki düşmanları bul
                        for (org.bukkit.entity.Entity nearby : golem.getNearbyEntities(15, 15, 15)) {
                            if (nearby instanceof org.bukkit.entity.LivingEntity && nearby != golem) {
                                // Toprak bloğu fırlat (FallingBlock)
                                org.bukkit.Location throwLoc = golem.getLocation().add(0, 2, 0);
                                org.bukkit.entity.FallingBlock fallingBlock = golem.getWorld().spawnFallingBlock(
                                        throwLoc,
                                        org.bukkit.Material.DIRT.createBlockData());

                                // Düşmana doğru fırlat
                                org.bukkit.util.Vector direction = nearby.getLocation().toVector()
                                        .subtract(throwLoc.toVector()).normalize().multiply(1.5);
                                fallingBlock.setVelocity(direction);
                                fallingBlock.setHurtEntities(true);
                                fallingBlock.setDropItem(false);

                                // Sadece bir düşmana fırlat
                                break;
                            }
                        }
                        break;

                    case 1: // Zıplama
                        // En yakın düşmanı bul
                        org.bukkit.entity.LivingEntity nearest = null;
                        double nearestDist = Double.MAX_VALUE;
                        for (org.bukkit.entity.Entity nearby : golem.getNearbyEntities(10, 10, 10)) {
                            if (nearby instanceof org.bukkit.entity.LivingEntity && nearby != golem) {
                                double dist = golem.getLocation().distance(nearby.getLocation());
                                if (dist < nearestDist) {
                                    nearestDist = dist;
                                    nearest = (org.bukkit.entity.LivingEntity) nearby;
                                }
                            }
                        }

                        if (nearest != null) {
                            // Düşmana doğru zıpla
                            org.bukkit.util.Vector jumpDir = nearest.getLocation().toVector()
                                    .subtract(golem.getLocation().toVector()).normalize();
                            jumpDir.setY(0.8); // Yüksek zıplama
                            jumpDir.multiply(1.2);
                            golem.setVelocity(jumpDir);

                            // Zıplama efekti
                            golem.getWorld().spawnParticle(
                                    org.bukkit.Particle.EXPLOSION_LARGE,
                                    golem.getLocation(),
                                    3);
                            golem.getWorld().playSound(
                                    golem.getLocation(),
                                    org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK,
                                    1.0f,
                                    0.5f);
                        }
                        break;

                    case 2: // Şok Dalgası
                        // Etrafındaki tüm düşmanlara hasar ver
                        for (org.bukkit.entity.Entity nearby : golem.getNearbyEntities(8, 8, 8)) {
                            if (nearby instanceof org.bukkit.entity.LivingEntity && nearby != golem) {
                                org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) nearby;

                                // Hasar ver (5 kalp)
                                target.damage(10.0, golem);

                                // Geriye it
                                org.bukkit.util.Vector knockback = target.getLocation().toVector()
                                        .subtract(golem.getLocation().toVector()).normalize();
                                knockback.setY(0.3);
                                knockback.multiply(1.5);
                                target.setVelocity(knockback);

                                // Şok efekti
                                target.getWorld().spawnParticle(
                                        org.bukkit.Particle.ELECTRIC_SPARK,
                                        target.getLocation().add(0, 1, 0),
                                        10);
                            }
                        }

                        // Şok dalgası görsel efekti
                        golem.getWorld().spawnParticle(
                                org.bukkit.Particle.EXPLOSION_LARGE,
                                golem.getLocation(),
                                5);
                        golem.getWorld().playSound(
                                golem.getLocation(),
                                org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                                1.0f,
                                0.8f);
                        break;
                }
            }
        };

        // Her 5 saniyede bir çalıştır (100 tick = 5 saniye)
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("Stratocraft");
        if (plugin != null && plugin instanceof org.bukkit.plugin.java.JavaPlugin) {
            abilityTask.runTaskTimer((org.bukkit.plugin.java.JavaPlugin) plugin, 0L, 100L);
        }
    }

    public void handleRiding(Player p) {
        Entity vehicle = p.getVehicle();
        if (vehicle == null)
            return;

        // TOPRAK SOLUCANI KONTROLÜ
        if (vehicle.getType() == EntityType.SILVERFISH && vehicle.getCustomName() != null
                && vehicle.getCustomName().contains("Solucan")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(0.5));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 5, false, false));

            ItemStack helmet = p.getInventory().getHelmet();
            if (!ItemManager.isCustomItem(helmet, "SONAR_GOGGLES")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                p.sendMessage("§cÖnünü göremiyorsun! Sonar Gözlüğü lazım.");
            }
        }

        // EJDERHA KONTROLÜ
        if (vehicle.getType() == EntityType.PHANTOM && vehicle.getCustomName() != null
                && vehicle.getCustomName().contains("Ejder")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(1.5));
        }

        // SAVAŞ AYISI KONTROLÜ
        if (vehicle.getType() == EntityType.POLAR_BEAR && vehicle.getCustomName() != null
                && vehicle.getCustomName().contains("Ayısı")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(0.3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 2, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
        }

        // GÖLGE PANTERİ KONTROLÜ
        if (vehicle.getType() == EntityType.CAT && vehicle.getCustomName() != null
                && vehicle.getCustomName().contains("Panter")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(1.2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
            // Gece görünmezlik
            if (p.getWorld().getTime() > 13000 || p.getWorld().getTime() < 23000) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
            }
        }

        // WYVERN KONTROLÜ (Kızıl Elmas besleme gerekli)
        if (vehicle.getType() == EntityType.PHANTOM && vehicle.getCustomName() != null
                && vehicle.getCustomName().contains("Wyvern")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(2.0));
            // Her 30 saniyede bir Kızıl Elmas kontrolü yapılmalı (MobRideTask'ta)
        }
    }
}
