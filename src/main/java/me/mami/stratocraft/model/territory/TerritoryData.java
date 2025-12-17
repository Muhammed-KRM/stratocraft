package me.mami.stratocraft.model.territory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;

import me.mami.stratocraft.model.base.BaseModel;

/**
 * Klan Alanı Veri Modeli (Genişletilmiş)
 * 
 * Territory modelini genişleten, çit lokasyonları ve sınır koordinatlarını tutan model
 * 
 * Özellikler:
 * - Çit lokasyonları listesi
 * - Sınır koordinatları (hesaplanmış)
 * - Y yüksekliği kontrolü (MinY, MaxY)
 * - 3D alan kontrolü
 */
public class TerritoryData extends BaseModel {
    private UUID clanId;
    private Location center;
    private int radius; // Geriye uyumluluk için
    
    // YENİ ÖZELLİKLER
    private final List<Location> fenceLocations = new CopyOnWriteArrayList<>(); // Thread-safe
    private final List<Location> boundaryCoordinates = new CopyOnWriteArrayList<>(); // Hesaplanmış sınır koordinatları
    private int minY = Integer.MAX_VALUE; // En alçak çit Y koordinatı
    private int maxY = Integer.MIN_VALUE; // En yüksek çit Y koordinatı
    private int skyHeight = 150; // Gökyüzüne yükseklik (config'den)
    private int groundDepth = 50; // Yer altına derinlik (config'den)
    private long lastBoundaryUpdate = 0; // Son sınır güncelleme zamanı
    
    // Cache için
    private boolean boundariesDirty = true; // Sınırlar güncellenmeli mi?
    
    public TerritoryData(UUID clanId, Location center) {
        super();
        this.clanId = clanId;
        this.center = center;
        this.radius = 50; // Varsayılan
        if (center != null) {
            this.minY = center.getBlockY();
            this.maxY = center.getBlockY();
        }
    }
    
    public TerritoryData(UUID id, UUID clanId, Location center, int radius) {
        super(id);
        this.clanId = clanId;
        this.center = center;
        this.radius = radius;
        if (center != null) {
            this.minY = center.getBlockY();
            this.maxY = center.getBlockY();
        }
    }
    
    // Getters
    public UUID getClanId() { return clanId; }
    public Location getCenter() { return center; }
    public int getRadius() { return radius; }
    public List<Location> getFenceLocations() { return new ArrayList<>(fenceLocations); }
    public List<Location> getBoundaryCoordinates() { return new ArrayList<>(boundaryCoordinates); }
    public int getMinY() { return minY == Integer.MAX_VALUE ? (center != null ? center.getBlockY() : 0) : minY; }
    public int getMaxY() { return maxY == Integer.MIN_VALUE ? (center != null ? center.getBlockY() : 0) : maxY; }
    public int getSkyHeight() { return skyHeight; }
    public int getGroundDepth() { return groundDepth; }
    public long getLastBoundaryUpdate() { return lastBoundaryUpdate; }
    public boolean isBoundariesDirty() { return boundariesDirty; }
    
    // Setters
    public void setClanId(UUID clanId) { 
        this.clanId = clanId; 
        updateTimestamp(); 
    }
    
    public void setCenter(Location center) { 
        this.center = center; 
        updateTimestamp(); 
    }
    
    public void setRadius(int radius) { 
        this.radius = radius; 
        updateTimestamp(); 
    }
    
    public void setSkyHeight(int skyHeight) { 
        this.skyHeight = skyHeight; 
        updateTimestamp(); 
    }
    
    public void setGroundDepth(int groundDepth) { 
        this.groundDepth = groundDepth; 
        updateTimestamp(); 
    }
    
    public void setBoundariesDirty(boolean dirty) {
        this.boundariesDirty = dirty;
    }
    
    /**
     * Çit lokasyonu ekle
     * DÜZELTME: Dünya kontrolü ve null check
     */
    public void addFenceLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        
        // Center ile aynı dünyada olmalı
        if (center != null && !center.getWorld().equals(loc.getWorld())) {
            return; // Farklı dünya
        }
        
        // Aynı lokasyon var mı kontrol et
        for (Location existing : fenceLocations) {
            if (existing != null && existing.getWorld() != null &&
                existing.getWorld().equals(loc.getWorld()) &&
                existing.getBlockX() == loc.getBlockX() &&
                existing.getBlockY() == loc.getBlockY() &&
                existing.getBlockZ() == loc.getBlockZ()) {
                return; // Zaten var
            }
        }
        
        fenceLocations.add(loc.clone()); // Clone ekle (referans sorunu önleme)
        updateYBounds();
        boundariesDirty = true;
        updateTimestamp();
    }
    
    /**
     * Çit lokasyonu kaldır
     * DÜZELTME: Null check ve dünya kontrolü
     */
    public void removeFenceLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        
        fenceLocations.removeIf(existing -> 
            existing != null && existing.getWorld() != null &&
            existing.getWorld().equals(loc.getWorld()) &&
            existing.getBlockX() == loc.getBlockX() &&
            existing.getBlockY() == loc.getBlockY() &&
            existing.getBlockZ() == loc.getBlockZ()
        );
        
        updateYBounds();
        boundariesDirty = true;
        updateTimestamp();
    }
    
    /**
     * Tüm çit lokasyonlarını temizle
     */
    public void clearFenceLocations() {
        fenceLocations.clear();
        boundaryCoordinates.clear();
        if (center != null) {
            minY = center.getBlockY();
            maxY = center.getBlockY();
        } else {
            minY = Integer.MAX_VALUE;
            maxY = Integer.MIN_VALUE;
        }
        boundariesDirty = true;
        updateTimestamp();
    }
    
    /**
     * Sınır koordinatlarını temizle (YENİ)
     */
    public void clearBoundaries() {
        boundaryCoordinates.clear();
        boundariesDirty = true;
        updateTimestamp();
    }
    
    /**
     * Y yüksekliğini güncelle (MinY, MaxY)
     * DÜZELTME: Null check ve dünya kontrolü
     */
    public void updateYBounds() {
        if (fenceLocations.isEmpty()) {
            if (center != null) {
                minY = center.getBlockY();
                maxY = center.getBlockY();
            } else {
                minY = Integer.MAX_VALUE;
                maxY = Integer.MIN_VALUE;
            }
            return;
        }
        
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        
        // Center ile aynı dünyadaki çitleri kontrol et
        for (Location fenceLoc : fenceLocations) {
            if (fenceLoc == null || fenceLoc.getWorld() == null) continue;
            
            // Dünya kontrolü
            if (center != null && !center.getWorld().equals(fenceLoc.getWorld())) {
                continue; // Farklı dünya, atla
            }
            
            int y = fenceLoc.getBlockY();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        
        // Center'ı da dahil et
        if (center != null) {
            int centerY = center.getBlockY();
            if (centerY < minY) minY = centerY;
            if (centerY > maxY) maxY = centerY;
        }
        
        updateTimestamp();
    }
    
    /**
     * Sınır koordinatlarını hesapla (basit versiyon - çitler arası çizgi)
     * ✅ DÜZELTME: Sadece kenarlara (sınırlara) partikül göster
     * Not: Gerçek sınır hesaplama TerritoryBoundaryManager'da yapılacak
     * DÜZELTME: Null check ve dünya kontrolü
     */
    public void calculateBoundaries() {
        boundaryCoordinates.clear();
        
        if (fenceLocations.isEmpty() || center == null || center.getWorld() == null) {
            boundariesDirty = false;
            return;
        }
        
        // ✅ DÜZELTME: Sadece kenarlara (sınırlara) partikül göster
        // Radius bazlı sınır hesaplama (basit versiyon)
        // Gerçek hesaplama TerritoryBoundaryManager'da yapılacak
        int radius = getRadius();
        if (radius <= 0) {
            // Radius yoksa çitlerden hesapla
            for (Location fenceLoc : fenceLocations) {
                if (fenceLoc == null || fenceLoc.getWorld() == null) continue;
                if (!center.getWorld().equals(fenceLoc.getWorld())) continue;
                boundaryCoordinates.add(fenceLoc.clone());
            }
        } else {
            // ✅ DÜZELTME: Radius varsa, sadece kenarlara (sınırlara) partikül göster
            // Daire çevresi boyunca partikül noktaları oluştur
            // Her 2 blokta bir partikül (daha yoğun görünüm için)
            int particleCount = (int) (radius * 2 * Math.PI / 2.0); // Her 2 blokta bir partikül
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                // ✅ DÜZELTME: Y koordinatını center'dan al (sınır çizgisi için)
                // TerritoryBoundaryParticleTask'ta oyuncunun Y seviyesine göre ayarlanacak
                Location boundaryLoc = new Location(center.getWorld(), x, center.getY(), z);
                boundaryCoordinates.add(boundaryLoc);
            }
        }
        
        lastBoundaryUpdate = System.currentTimeMillis();
        boundariesDirty = false;
        updateTimestamp();
    }
    
    /**
     * Konum alan içinde mi? (3D kontrol)
     */
    public boolean isInsideTerritory(Location loc) {
        if (loc == null || center == null) return false;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        
        // X-Z düzlemi kontrolü (radius bazlı - geriye uyumluluk)
        double distance2D = Math.sqrt(
            Math.pow(loc.getX() - center.getX(), 2) + 
            Math.pow(loc.getZ() - center.getZ(), 2)
        );
        
        if (distance2D > radius) return false;
        
        // Y yüksekliği kontrolü
        int locY = loc.getBlockY();
        int effectiveMinY = getMinY() - groundDepth;
        int effectiveMaxY = getMaxY() + skyHeight;
        
        return locY >= effectiveMinY && locY <= effectiveMaxY;
    }
    
    /**
     * Partikül için sınır çizgisi al (basit versiyon)
     * Not: Gerçek hesaplama TerritoryBoundaryManager'da yapılacak
     */
    public List<Location> getBoundaryLine() {
        if (boundariesDirty) {
            calculateBoundaries();
        }
        return new ArrayList<>(boundaryCoordinates);
    }
    
    /**
     * Alan büyüklüğü hesapla (blok²)
     */
    public int calculateArea() {
        if (fenceLocations.isEmpty()) {
            // Radius bazlı hesaplama (geriye uyumluluk)
            return (int) (Math.PI * radius * radius);
        }
        
        // Çitler arası alan hesaplama (basit)
        // Gerçek hesaplama TerritoryBoundaryManager'da yapılacak
        return fenceLocations.size() * 4; // Yaklaşık
    }
    
    /**
     * Çit sayısı
     */
    public int getFenceCount() {
        return fenceLocations.size();
    }
    
    /**
     * Genişlet (radius bazlı - geriye uyumluluk)
     */
    public void expand(int amount) {
        this.radius += amount;
        boundariesDirty = true;
        updateTimestamp();
    }
}

