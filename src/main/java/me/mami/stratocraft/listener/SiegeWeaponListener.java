package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.SiegeWeaponManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Savaş Alanı Yapıları Event Handler'ları
 * 
 * Tüm event handler'lar burada, mantık SiegeWeaponManager'da
 */
public class SiegeWeaponListener implements Listener {
    private final SiegeWeaponManager manager;

    public SiegeWeaponListener(SiegeWeaponManager manager) {
        this.manager = manager;
    }

    // ========== MANCINIK (CATAPULT) ==========

    @EventHandler(priority = EventPriority.HIGH)
    public void onCatapultInteract(PlayerInteractEvent event) {
        // Çift el kontrolü (BatteryListener mantığı)
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Mancınık kontrolü: Basamak (STONE_BRICK_STAIRS) veya Merdiven
        if (block.getType() != Material.STONE_BRICK_STAIRS &&
                block.getType() != Material.COBBLESTONE_STAIRS) {
            return;
        }

        // Yapı bütünlüğü kontrolü (3x3 Taban)
        if (!manager.isCatapultStructure(block)) {
            return;
        }

        Player player = event.getPlayer();

        // Eğer zaten biniliyse: Shift ile in, Normal Right-Click ile ateş et
        if (manager.isMounted(player)) {
            if (player.isSneaking()) {
                // Shift + Sağ Tık = İn
                manager.dismountCatapult(player);
                event.setCancelled(true);
                return;
            } else {
                // Normal Sağ Tık = Ateş et (biniliyken)
                // Cooldown kontrolü
                if (!manager.canFireCatapult(player)) {
                    long remaining = manager.getCatapultCooldownRemaining(player);
                    player.sendMessage("§cMancınık hazır değil! " + remaining + " saniye kaldı.");
                    event.setCancelled(true);
                    return;
                }

                manager.fireCatapult(player, block);
                event.setCancelled(true);
                return;
            }
        }

        // Binili değilse: Boş El + Sağ Tık = Bin
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            manager.mountCatapult(player, block);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onCatapultFireMounted(PlayerInteractEvent event) {
        // Sadece Sol Tık ve Havaya/Bloğa
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        // Oyuncu mancınıkta mı?
        if (!manager.isMounted(player))
            return;

        // Binek (ArmorStand) üzerinden mancınık bloğunu bulmamız lazım
        // Ancak basitlik için, oyuncunun bindiği ArmorStand'ın olduğu yerdeki mancınığı
        // bulalım
        org.bukkit.entity.Entity vehicle = player.getVehicle();
        if (vehicle == null)
            return;

        Block catapultBlock = vehicle.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
        // Eğer tam blokta değilse biraz aşağı bak
        if (catapultBlock.getType() != Material.STONE_BRICK_STAIRS
                && catapultBlock.getType() != Material.COBBLESTONE_STAIRS) {
            catapultBlock = vehicle.getLocation().add(0, -0.5, 0).getBlock();
        }

        if (catapultBlock.getType() != Material.STONE_BRICK_STAIRS
                && catapultBlock.getType() != Material.COBBLESTONE_STAIRS) {
            return; // Mancınık bulunamadı
        }

        // Cooldown kontrolü
        if (!manager.canFireCatapult(player)) {
            long remaining = manager.getCatapultCooldownRemaining(player);
            player.sendMessage("§cMancınık hazır değil! " + remaining + " saniye kaldı.");
            return;
        }

        manager.fireCatapult(player, catapultBlock);
        event.setCancelled(true);
    }

    @EventHandler
    public void onDismount(org.bukkit.event.entity.EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        // Sadece mancınık ArmorStand'ından inerken işle
        if (event.getDismounted() instanceof org.bukkit.entity.ArmorStand) {
            org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) event.getDismounted();

            // Bu ArmorStand mancınık mount'u mu kontrol et
            if (manager.isMounted(player) && stand.equals(manager.getCatapultMount(player))) {
                // İnmeyi engelle eğer player sneak yapıyorsa (mount işlemi sırasında)
                // Sneak olmadan inmeye izin ver (örn: ArmorStand kırıldı, player öldü, vb)

                // NOT: Dismount olayı Shift+RightClick'ten SONRA tetikleniyor
                // Bu yüzden burada sadece cleanup yapıyoruz
                manager.dismountCatapult(player);
            }
        }
    }

    @EventHandler
    public void onExternalCatapultFire(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        // Shift + Sol Tık ile dışarıdan ateşleme
        if (!event.getPlayer().isSneaking())
            return;

        // Sol tık kontrolü (PlayerInteractEntityEvent'te action yok ama bu event sağ
        // tık ile tetiklenir)
        // DÜZELTME: PlayerInteractEntityEvent SADECE SAĞ TIK İLE ÇALIŞIR!
        // Sol tık (vurma) için EntityDamageByEntityEvent kullanmalıyız.
        // Ancak kullanıcı "şift sol tık ile koltuğa vurunca" dedi.
        // ArmorStand invulnerable olduğu için damage event tetiklenmeyebilir.
        // Bu yüzden PlayerInteractEvent (LEFT_CLICK_BLOCK/AIR) ile raytrace yapmalıyız
        // veya
        // ArmorStand'a vurma olayını yakalamalıyız.

        // ArmorStand'a sol tık (vurma) EntityDamageByEntityEvent ile yakalanır
        // (Invulnerable olsa bile event düşer ama cancelled olur)
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        if (!(event.getEntity() instanceof org.bukkit.entity.ArmorStand))
            return;

        Player player = (Player) event.getDamager();
        org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) event.getEntity();

        // Shift + Sol Tık (Vurma)
        if (!player.isSneaking())
            return;

        // Bu bir mancınık mount'u mu?
        // Bunu anlamak için mount'un altındaki bloğa bakabiliriz
        Block catapultBlock = stand.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
        if (catapultBlock.getType() != Material.STONE_BRICK_STAIRS &&
                catapultBlock.getType() != Material.COBBLESTONE_STAIRS) {
            catapultBlock = stand.getLocation().add(0, -0.5, 0).getBlock();
        }

        if (catapultBlock.getType() != Material.STONE_BRICK_STAIRS &&
                catapultBlock.getType() != Material.COBBLESTONE_STAIRS) {
            return;
        }

        // Yapı kontrolü
        if (!manager.isCatapultStructure(catapultBlock))
            return;

        // Cooldown kontrolü
        if (!manager.canFireCatapult(player)) {
            long remaining = manager.getCatapultCooldownRemaining(player);
            player.sendMessage("§cMancınık hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }

        // Ateşle (Dışarıdan)
        manager.fireCatapult(player, catapultBlock);
        event.setCancelled(true); // ArmorStand'a zarar verme
    }

    @EventHandler
    public void onPrepareAnvil(org.bukkit.event.inventory.PrepareAnvilEvent event) {
        org.bukkit.inventory.ItemStack first = event.getInventory().getItem(0);
        org.bukkit.inventory.ItemStack second = event.getInventory().getItem(1);

        if (first == null || second == null)
            return;

        // 1. Rusty Hook + Iron Ingot
        if (me.mami.stratocraft.manager.ItemManager.isCustomItem(first, "RUSTY_HOOK") &&
                second.getType() == Material.IRON_INGOT) {

            org.bukkit.inventory.ItemStack result = first.clone();
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) result.getItemMeta();
            if (meta != null) {
                meta.setDamage(0); // Tamir et
                result.setItemMeta(meta);
                event.setResult(result);
            }
        }
        // 2. Golden Hook + Gold Ingot
        else if (me.mami.stratocraft.manager.ItemManager.isCustomItem(first, "GOLDEN_HOOK") &&
                second.getType() == Material.GOLD_INGOT) {

            org.bukkit.inventory.ItemStack result = first.clone();
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) result.getItemMeta();
            if (meta != null) {
                meta.setDamage(0); // Tamir et
                result.setItemMeta(meta);
                event.setResult(result);
            }
        }
        // 3. Titan Grapple + Titanium Ingot (Iron Ingot placeholder for now, or
        // Titanium if item exists)
        else if (me.mami.stratocraft.manager.ItemManager.isCustomItem(first, "TITAN_GRAPPLE")) {
            // Titanyum kontrolü (ItemManager.TITANIUM_INGOT)
            if (me.mami.stratocraft.manager.ItemManager.isCustomItem(second, "TITANIUM_INGOT")) {
                org.bukkit.inventory.ItemStack result = first.clone();
                org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) result.getItemMeta();
                if (meta != null) {
                    meta.setDamage(0); // Tamir et
                    result.setItemMeta(meta);
                    event.setResult(result);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.FallingBlock))
            return;

        org.bukkit.entity.FallingBlock fallingBlock = (org.bukkit.entity.FallingBlock) event.getEntity();

        // 1. SUPPLY DROP KONTROLÜ
        if (fallingBlock.hasMetadata("SupplyDrop")) {
            // Sandığa dönüşmesine izin ver
            event.setCancelled(false);

            // Sandık içeriğini doldur (Bir tick sonra, blok oluştuktan sonra)
            org.bukkit.Bukkit.getScheduler().runTaskLater(me.mami.stratocraft.Main.getInstance(), () -> {
                Block block = event.getBlock();
                if (block.getType() == Material.CHEST) {
                    org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();

                    // Rastgele loot ekle
                    java.util.Random random = new java.util.Random();
                    // 3-5 arası eşya
                    int itemCount = 3 + random.nextInt(3);

                    for (int i = 0; i < itemCount; i++) {
                        int slot = random.nextInt(chest.getInventory().getSize());
                        // Basit loot tablosu (Geliştirilebilir)
                        Material[] loots = { Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.TNT,
                                Material.OBSIDIAN };
                        Material loot = loots[random.nextInt(loots.length)];
                        chest.getInventory().setItem(slot,
                                new org.bukkit.inventory.ItemStack(loot, 1 + random.nextInt(3)));
                    }

                    // Custom Item şansı (Titanyum vb.)
                    if (random.nextDouble() < 0.3) { // %30 şans
                        // ItemManager'dan rastgele custom item (Örnek)
                        // chest.getInventory().addItem(ItemManager.TITANIUM_INGOT.clone());
                    }

                    // Havai fişek patlat (bulunduğunu belli et)
                    org.bukkit.Location loc = block.getLocation().add(0.5, 1, 0.5);
                    org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) loc.getWorld().spawnEntity(loc,
                            org.bukkit.entity.EntityType.FIREWORK);
                    org.bukkit.inventory.meta.FireworkMeta fwm = fw.getFireworkMeta();
                    fwm.addEffect(org.bukkit.FireworkEffect.builder().withColor(org.bukkit.Color.GREEN)
                            .with(org.bukkit.FireworkEffect.Type.BALL_LARGE).build());
                    fwm.setPower(1);
                    fw.setFireworkMeta(fwm);

                    org.bukkit.Bukkit.broadcastMessage(
                            "§a§lHAVA YARDIMI İNDİ! §7Koordinat: " + block.getX() + ", " + block.getZ());
                }
            }, 5L);
            return;
        }

        // 2. MANCINIK MERMİSİ KONTROLÜ
        if (!fallingBlock.hasMetadata("SiegeAmmo"))
            return;

        event.setCancelled(true); // Bloğa dönüşmesini engelle

        org.bukkit.Location hitLoc = fallingBlock.getLocation();
        manager.triggerExplosion(hitLoc); // Merkezi patlama metodu

        fallingBlock.remove(); // Entity'i sil
    }

    @EventHandler
    public void onEntityDropItem(org.bukkit.event.entity.EntityDropItemEvent event) {
        // FallingBlock duvara çarpıp item'a dönüşürse (dropItem false olsa bile bazen
        // tetiklenir)
        if (event.getEntity() instanceof org.bukkit.entity.FallingBlock) {
            org.bukkit.entity.FallingBlock fb = (org.bukkit.entity.FallingBlock) event.getEntity();
            if (fb.hasMetadata("SiegeAmmo")) {
                event.setCancelled(true); // Item düşmesini engelle
                manager.triggerExplosion(fb.getLocation()); // Patlat
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        // Mancınık yapımı kontrolü (Stone Brick Stairs)
        if (block.getType() == Material.STONE_BRICK_STAIRS || block.getType() == Material.COBBLESTONE_STAIRS) {
            // Eğer bu blok bir mancınık yapısını tamamlıyorsa
            if (manager.isCatapultStructure(block)) {
                manager.playConstructionEffect(block.getLocation());
                event.getPlayer().sendMessage("§a§lMANCINIK İNŞA EDİLDİ!");
            }
        }
    }

    @EventHandler
    public void onBallistaArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Arrow))
            return;

        org.bukkit.entity.Arrow arrow = (org.bukkit.entity.Arrow) event.getEntity();

        // Balista oku mu?
        if (!arrow.hasMetadata("BallistaArrow"))
            return;

        org.bukkit.Location hitLoc = arrow.getLocation();

        // Küçük patlama efekti (Mancınıktan daha az güçlü)
        arrow.getWorld().createExplosion(hitLoc, 1.5f, false); // Blokları kırmaz

        // Partikül efekti
        arrow.getWorld().spawnParticle(
                org.bukkit.Particle.EXPLOSION_LARGE,
                hitLoc,
                3);

        arrow.remove(); // Oku sil
    }

    // ========== ENERJİ KALKANI (FORCE FIELD) ==========

    @EventHandler(priority = EventPriority.HIGH)
    public void onShieldGeneratorInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Jeneratör kontrolü: Beacon bloğu
        if (block.getType() != Material.BEACON)
            return;

        // Can Tapınağı kontrolü - Eğer Can Tapınağı ise bu handler'ı atla
        if (block.hasMetadata("HealingShrine"))
            return;

        // Zaten aktif bir kalkan var mı?
        if (manager.isShieldActive(block.getLocation())) {
            event.getPlayer().sendMessage("§cBu jeneratör zaten aktif!");
            event.setCancelled(true);
            return;
        }

        manager.activateShield(block.getLocation());
        event.getPlayer().sendMessage("§b§lENERJİ KALKANI AKTİF!");
        event.setCancelled(true); // Beacon menüsünü açmayı engelle
    }

    @EventHandler
    public void onGeneratorBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Jeneratör mü?
        if (block.getType() != Material.BEACON)
            return;

        if (manager.removeShield(block.getLocation())) {
            Player breaker = event.getPlayer();
            if (breaker != null) {
                breaker.sendMessage("§eEnerji kalkanı devre dışı bırakıldı!");
            }
        }
    }

    @EventHandler
    public void onPlayerMoveIntoShield(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış (X, Y, Z kontrolü)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş, işlem yapma
        }

        org.bukkit.Location to = event.getTo();
        if (to == null)
            return;

        // Kalkan içine girmeye çalışıyor mu?
        if (manager.isInsideShield(to)) {
            org.bukkit.Location from = event.getFrom();
            // Eğer dışarıdan içeriye giriyorsa engelle
            if (!manager.isInsideShield(from)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cEnerji kalkanı içine giremezsin! Önce kalkanı kır veya kapıyı aç.");
            }
        }
    }

    @EventHandler
    public void onProjectileHitShield(ProjectileHitEvent event) {
        org.bukkit.Location hitLoc = event.getEntity().getLocation();

        if (manager.isInsideShield(hitLoc)) {
            // Mermiyi yok et
            event.getEntity().remove();
            // Efekt
            hitLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.ELECTRIC_SPARK,
                    hitLoc,
                    10);
        }
    }

    // ========== KATEGORİ 2: KLAN ÖZEL YAPILAR ==========

    @EventHandler
    public void onHealingShrinePlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        // Beacon yerleştirildi mi?
        if (block.getType() != Material.BEACON)
            return;

        // Etrafında 3x3 Altın Bloğu var mı kontrol et
        if (manager.isShrineStructure(block)) {
            Player player = event.getPlayer();

            if (manager.createHealingShrine(block.getLocation(), player)) {
                player.sendMessage("§a§lCAN TAPINAĞI KURULDU!");
                player.sendMessage("§7Sadece klan üyeleriniz buradan faydalanabilir.");
            } else {
                player.sendMessage("§cCan Tapınağı kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHealingShrineBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.hasMetadata("HealingShrine")) {
            manager.removeHealingShrine(block.getLocation());
            event.getPlayer().sendMessage("§eCan Tapınağı kırıldı!");
        }
    }

    // ========== KATEGORİ 1: YENİ YAPILAR ==========

    // 3. BALİSTA (Ballista)
    @EventHandler(priority = EventPriority.HIGH)
    public void onBallistaInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Balista kontrolü: Dispenser bloğu
        if (block.getType() != Material.DISPENSER)
            return;

        Player player = event.getPlayer();

        // Cooldown kontrolü (Lokasyon bazlı)
        if (!manager.canFireBallista(block.getLocation())) {
            long remaining = manager.getBallistaCooldownRemaining(block.getLocation());
            player.sendMessage("§cBalista hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }

        manager.fireBallista(player, block);
        event.setCancelled(true); // Dispenser menüsünü açmayı engelle
    }

    // 4. LAV FISKIYESI (Lava Fountain)
    @EventHandler(priority = EventPriority.HIGH)
    public void onLavaFountainInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Lav Fıskiyesi kontrolü: Cauldron (Kazan) + Lava içinde
        if (block.getType() != Material.CAULDRON)
            return;

        org.bukkit.block.data.Levelled cauldron = (org.bukkit.block.data.Levelled) block.getBlockData();
        if (cauldron.getLevel() < 3)
            return; // Tam dolu olmalı

        org.bukkit.Location loc = block.getLocation();

        if (!manager.canActivateLavaFountain(loc)) {
            long remaining = manager.getLavaFountainCooldownRemaining(loc);
            event.getPlayer().sendMessage("§cLav Fıskiyesi hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }

        manager.activateLavaFountain(loc);
        event.setCancelled(true); // Cauldron etkileşimini engelle
    }

    // 5. ZEHİR GAZI YAYICI (Poison Gas Dispenser)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPoisonDispenserInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Zehir Yayıcı kontrolü: Dropper bloğu
        if (block.getType() != Material.DROPPER)
            return;

        org.bukkit.Location loc = block.getLocation();

        if (!manager.canActivatePoisonDispenser(loc)) {
            long remaining = manager.getPoisonDispenserCooldownRemaining(loc);
            event.getPlayer().sendMessage("§cZehir Yayıcı hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }

        manager.activatePoisonDispenser(loc);
        event.setCancelled(true); // Dropper menüsünü açmayı engelle
    }

    // ========== KATEGORİ 2: YENİ YAPILAR ==========

    // 2. GÜÇ TOTEMİ (Power Totem)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPowerTotemInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Güç Totemi kontrolü: Enchanting Table + Altında 2x2 Obsidyen
        if (block.getType() != Material.ENCHANTING_TABLE)
            return;

        if (manager.isTotemStructure(block, Material.OBSIDIAN)) {
            Player player = event.getPlayer();

            if (manager.createPowerTotem(block.getLocation(), player)) {
                player.sendMessage("§6§lGÜÇ TOTEMİ AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan güç alacak.");
                event.setCancelled(true); // Enchanting Table menüsünü açmayı engelle
            } else {
                player.sendMessage("§cGüç Totemi kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }

    // 3. HIZ ÇEMBERİ (Speed Circle)
    @EventHandler(priority = EventPriority.HIGH)
    public void onSpeedCircleInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Hız Çemberi kontrolü: Ender Chest + Altında 2x2 Lapis Bloğu
        if (block.getType() != Material.ENDER_CHEST)
            return;

        if (manager.isTotemStructure(block, Material.LAPIS_BLOCK)) {
            Player player = event.getPlayer();

            if (manager.createSpeedCircle(block.getLocation(), player)) {
                player.sendMessage("§b§lHIZ ÇEMBERİ AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan hız alacak.");
                event.setCancelled(true); // Ender Chest menüsünü açmayı engelle
            } else {
                player.sendMessage("§cHız Çemberi kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }

    // 4. SAVUNMA DUVARI (Defense Wall)
    @EventHandler(priority = EventPriority.HIGH)
    public void onDefenseWallInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Savunma Duvarı kontrolü: Anvil + Altında 2x2 Demir Bloğu
        if (block.getType() != Material.ANVIL)
            return;

        if (manager.isTotemStructure(block, Material.IRON_BLOCK)) {
            Player player = event.getPlayer();

            if (manager.createDefenseWall(block.getLocation(), player)) {
                player.sendMessage("§7§lSAVUNMA DUVARI AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan direnç alacak.");
                event.setCancelled(true); // Anvil menüsünü açmayı engelle
            } else {
                player.sendMessage("§cSavunma Duvarı kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClanStructureBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        org.bukkit.Location loc = block.getLocation();

        if (block.hasMetadata("PowerTotem")) {
            manager.removePowerTotem(loc);
            event.getPlayer().sendMessage("§eGüç Totemi kırıldı!");
        } else if (block.hasMetadata("SpeedCircle")) {
            manager.removeSpeedCircle(loc);
            event.getPlayer().sendMessage("§eHız Çemberi kırıldı!");
        } else if (block.hasMetadata("DefenseWall")) {
            manager.removeDefenseWall(loc);
            event.getPlayer().sendMessage("§eSavunma Duvarı kırıldı!");
        }
    }

    /**
     * Oyuncu çıkışında cooldown'ları temizle (Memory leak önleme)
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearPlayerCooldowns(event.getPlayer().getUniqueId());
    }
}
