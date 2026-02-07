# Island Upgrade System - BentoBox BSkyBlock

## 📋 Gereksinimler

✅ **Zorunlu Eklentiler:**
- Spigot/Paper 1.19+
- BentoBox (2.0+)
- BentoBox-BSkyBlock
- BentoBox-Limits
- Vault
- Bir ekonomi eklentisi (EssentialsX veya CMI)
- PlaceholderAPI
- DeluxeMenus
- LuckPerms (piston limitleri için)

✅ **Opsiyonel:**
- GeneratorUpgrade eklentisi (jeneratör yükseltmeleri için)

---

## 🚀 Kurulum Adımları

### 1. Eklentiyi Derleyin

```bash
cd IslandUpgradePlugin
mvn clean package
```

Derlenmiş `.jar` dosyası `target/IslandUpgrade-1.0.0.jar` konumunda oluşacak.

### 2. Dosyaları Yerleştirin

1. **IslandUpgrade-1.0.0.jar** → `plugins/` klasörüne
2. **IslandUpgrade.yml** → `plugins/DeluxeMenus/gui_menus/` klasörüne
3. **BentoBoxLimits.yml** içeriğini → `plugins/BentoBox/addons/Limits/config.yml` dosyasına ekleyin

### 3. Sunucuyu Başlatın

İlk başlatmada eklenti otomatik olarak yapılandırma dosyalarını oluşturacak:
- `plugins/IslandUpgrade/config.yml`
- `plugins/IslandUpgrade/data.yml`

### 4. İzinleri Ayarlayın

Varsayılan olarak tüm oyunculara başlangıç limitleri verin:

**Piston Limitleri:**
```bash
lp group default permission set bskyblock.island.limit.PISTON.250 true
lp group default permission set bskyblock.island.limit.STICKY_PISTON.250 true
```

**Üye Limiti:**
```bash
lp group default permission set bskyblock.team.maxsize.4 true
```

**BentoBox Limits Yapılandırması:**
1. `BentoBox/addons/Limits/config.yml` dosyasını açın
2. `blocklimits:` bölümüne şunu ekleyin:
```yaml
blocklimits:
  PISTON: 250
  STICKY_PISTON: 250
```
3. Sunucuyu yeniden başlatın

---

## ⚙️ Yapılandırma

### Config.yml Düzenleme

`plugins/IslandUpgrade/config.yml` dosyasını açın ve istediğiniz gibi özelleştirin:

```yaml
# Ada sınırı seviyeleri
border:
  levels:
    1:
      size: 50      # Başlangıç boyutu
      cost: 0       # Ücretsiz
    2:
      size: 100     # 2. seviye boyutu
      cost: 75000   # 75,000 para
    3:
      size: 150
      cost: 150000
```

**Yeni seviye eklemek için:**
```yaml
    4:
      size: 200
      cost: 300000
```

### Mesajları Özelleştirme

```yaml
messages:
  prefix: '&8[&6Ada Yükseltme&8]&r '
  upgrade-success: '&aYükseltme başarılı! &7Yeni seviye: &b{level}'
  insufficient-funds: '&cYetersiz bakiye! Gereken: &6{cost} ₺'
```

---

## 🎮 Kullanım

### Oyuncu Komutları

- `/yükseltme` - Ana yükseltme menüsünü açar
- `/islandupgrade border` - Ada sınırını yükseltir
- `/islandupgrade member` - Üye sayısını yükseltir
- `/islandupgrade piston` - Piston limitini yükseltir

### Admin Komutları

- `/islandupgrade reload` - Yapılandırmayı yeniden yükler

### İzinler

```yaml
islandupgrade.use: true        # Oyuncular için
islandupgrade.admin: op        # Adminler için
```

---

## 🎨 DeluxeMenu Özelleştirme

`plugins/DeluxeMenus/gui_menus/IslandUpgrade.yml` dosyasını düzenleyerek menüyü özelleştirebilirsiniz:

### Öğe Pozisyonlarını Değiştirme

```yaml
items:
  ada-siniri:
    slot: 11    # Bu sayıyı değiştirerek pozisyonu değiştirin
```

### Görünümü Değiştirme

```yaml
  ada-siniri:
    material: GRASS_BLOCK    # Farklı bir blok kullanın
    display_name: '&a&lYeni İsim'
```

---

## 📊 PlaceholderAPI Placeholder'ları

### Ada Sınırı
- `%islandupgrade_border_level%` - Mevcut seviye
- `%islandupgrade_border_size%` - Mevcut boyut
- `%islandupgrade_border_next_size%` - Sonraki seviye boyutu
- `%islandupgrade_border_cost%` - Yükseltme maliyeti
- `%islandupgrade_border_status%` - Durum mesajı

### Üye Sayısı
- `%islandupgrade_member_level%` - Mevcut seviye
- `%islandupgrade_member_limit%` - Mevcut limit
- `%islandupgrade_member_next_limit%` - Sonraki limit
- `%islandupgrade_member_cost%` - Yükseltme maliyeti
- `%islandupgrade_member_status%` - Durum mesajı

### Piston Limiti
- `%islandupgrade_piston_level%` - Mevcut seviye
- `%islandupgrade_piston_limit%` - Mevcut limit
- `%islandupgrade_piston_next_limit%` - Sonraki limit
- `%islandupgrade_piston_cost%` - Yükseltme maliyeti
- `%islandupgrade_piston_status%` - Durum mesajı

---

## 🔧 Sorun Giderme

### Piston limiti çalışmıyor?

1. BentoBox-Limits addon'unun kurulu olduğundan emin olun
2. `config.yml` dosyasında piston limitleri tanımlı mı kontrol edin
3. LuckPerms izinlerini kontrol edin:
```bash
lp user <oyuncu> permission info
```

### Menü açılmıyor?

1. DeluxeMenus kurulu mu?
2. `IslandUpgrade.yml` doğru klasörde mi? (`plugins/DeluxeMenus/gui_menus/`)
3. Konsol hatalarını kontrol edin

### Para çekilmiyor?

1. Vault kurulu mu?
2. EssentialsX veya CMI aktif mi?
3. Oyuncunun yeterli parası var mı?

### Placeholder'lar çalışmıyor?

1. PlaceholderAPI kurulu mu?
2. Sunucuyu yeniden başlattınız mı?
3. `/papi parse me %islandupgrade_border_level%` komutuyla test edin

---

## 📝 Örnek Kullanım Senaryosu

1. Oyuncu `/yükseltme` yazar
2. Menü açılır ve mevcut seviyeleri gösterir
3. "Ada Sınırı" öğesine tıklar
4. Sistem:
   - Yeterli parası olup olmadığını kontrol eder
   - Parayı çeker
   - Ada sınırını genişletir
   - Seviyeyi kaydeder
   - Başarı mesajı gösterir

---

## 🎯 Gelişmiş Özellikler

### Jeneratör Entegrasyonu

Jeneratör yükseltme eklentiniz varsa, menüdeki jeneratör öğesi otomatik olarak ona yönlendirecektir:

```yaml
  jenerator:
    left_click_commands:
      - '[player] generator'    # Jeneratör komutunuz
```

### Özel Seviyeler Ekleme

Her yükseltme türüne sınırsız seviye ekleyebilirsiniz. Sadece `config.yml` dosyasına yeni seviyeler ekleyin:

```yaml
piston:
  levels:
    11:
      limit: 15000
      cost: 2000000
    12:
      limit: 20000
      cost: 5000000
```

---

## 📞 Destek

Sorun yaşıyorsanız:
1. Konsol loglarını kontrol edin
2. `/islandupgrade reload` komutunu deneyin
3. Tüm bağımlılıkların güncel olduğundan emin olun

---

## 🌟 Özellikler

✅ Ada sınırı yükseltme (3 seviye)
✅ Üye sayısı yükseltme (5 seviye)
✅ Piston limiti yükseltme (10 seviye)
✅ Jeneratör yükseltme entegrasyonu
✅ DeluxeMenu ile güzel GUI
✅ PlaceholderAPI desteği
✅ Ekonomi sistemi entegrasyonu
✅ Tamamen özelleştirilebilir
✅ Türkçe mesaj desteği

---

## 📄 Lisans

Bu eklenti özel olarak sizin için hazırlanmıştır.

---

**Keyifli oyunlar! 🎮**
