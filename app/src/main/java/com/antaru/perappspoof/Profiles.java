package com.antaru.perappspoof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Preset yang meniru daftar di PenguinOS Settings (branch celerity):
 *  - perapp_spoof_profile_values/labels (custom_arrays.xml)  -> DEVICES
 *  - GPU_DISPLAY / CPU_DISPLAY (AdvancedAppSpoofSettings.kt) -> GPUS / CPUS
 *
 * CATATAN: file Settings PenguinOS hanya memuat KEY dan LABEL. Nilai asli (MODEL, DEVICE,
 * renderer, dst.) ada di sisi framework (PerAppsPropsUtils.java) yang tidak terbaca saat
 * modul ini dibuat, jadi nilai di bawah ditulis dari pengetahuan umum. Tanda "// ~" berarti
 * kurang yakin, cocokkan dulu. Preset yang nilainya tidak diketahui sengaja tidak dimasukkan.
 */
public final class Profiles {
    private Profiles() {}

    public static final class Device {
        public final String key, label, brand, manufacturer, device, model, product, fingerprint;
        public Device(String key, String label, String brand, String manufacturer,
                      String device, String model, String product, String fingerprint) {
            this.key = key; this.label = label; this.brand = brand; this.manufacturer = manufacturer;
            this.device = device; this.model = model; this.product = product; this.fingerprint = fingerprint;
        }
    }

    /** hardware/platform boleh null = tidak ditimpa. */
    public static final class Cpu {
        public final String key, label, socManufacturer, socModel, hardware, platform;
        public Cpu(String key, String label, String socManufacturer, String socModel,
                   String hardware, String platform) {
            this.key = key; this.label = label; this.socManufacturer = socManufacturer;
            this.socModel = socModel; this.hardware = hardware; this.platform = platform;
        }
    }

    public static final class Gpu {
        public final String key, label, renderer, vendor;
        public Gpu(String key, String label, String renderer, String vendor) {
            this.key = key; this.label = label; this.renderer = renderer; this.vendor = vendor;
        }
    }

    public static final List<Device> DEVICES;
    public static final List<Cpu> CPUS;
    public static final List<Gpu> GPUS;

    static {
        List<Device> d = new ArrayList<>();
        d.add(new Device("BS4C", "Black Shark 4 (China)", "blackshark", "Black Shark", "PRS", "PRS-A0", "PRS", "")); // ~
        d.add(new Device("F5", "Poco F5", "POCO", "Xiaomi", "marble", "23049PCD8G", "marble_global", ""));
        d.add(new Device("GZF5", "Samsung Galaxy Z Fold 5", "samsung", "samsung", "q5q", "SM-F946B", "q5qxxx", ""));
        d.add(new Device("LY700", "Lenovo Legion Y700", "Lenovo", "LENOVO", "TB-9707F", "Lenovo TB-9707F", "TB-9707F", "")); // ~
        d.add(new Device("LY70023", "Lenovo Legion Y700 (2023)", "Lenovo", "LENOVO", "TB320FC", "Lenovo TB320FC", "TB320FC", ""));
        d.add(new Device("MI11TP", "Xiaomi 11T Pro", "Xiaomi", "Xiaomi", "vili", "2107113SG", "vili_global", ""));
        d.add(new Device("MI13", "Xiaomi 13", "Xiaomi", "Xiaomi", "fuxi", "2211133G", "fuxi_global", ""));
        d.add(new Device("MI13P", "Xiaomi 13 Pro", "Xiaomi", "Xiaomi", "nuwa", "2210132G", "nuwa_global", ""));
        d.add(new Device("MI14P", "Xiaomi 14 Pro", "Xiaomi", "Xiaomi", "shennong", "23116PN5BC", "shennong", ""));
        d.add(new Device("OP12", "OnePlus 12", "OnePlus", "OnePlus", "waffle", "CPH2581", "CPH2581", "")); // ~
        d.add(new Device("OP13", "OnePlus 13", "OnePlus", "OnePlus", "dodge", "CPH2655", "CPH2655", "")); // ~
        d.add(new Device("OP8P5G", "OnePlus 8 Pro 5G", "OnePlus", "OnePlus", "OnePlus8Pro", "IN2023", "OnePlus8Pro", ""));
        d.add(new Device("PXL", "Pixel XL", "google", "Google", "marlin", "Pixel XL", "marlin",
                "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys"));
        d.add(new Device("PXL10PXL", "Pixel 10 Pro XL", "google", "Google", "mustang", "Pixel 10 Pro XL", "mustang", "")); // ~
        d.add(new Device("RM9P", "REDMAGIC 9 Pro", "nubia", "nubia", "NX769J", "NX769J", "NX769J", ""));
        d.add(new Device("RM10P", "RedMagic 10 Pro", "nubia", "nubia", "NX789J", "NX789J", "NX789J", ""));
        d.add(new Device("RM11P", "REDMAGIC 11 Pro", "nubia", "nubia", "NX809J", "NX809J", "NX809J", "")); // ~
        d.add(new Device("S25U", "Samsung Galaxy S25 Ultra", "samsung", "samsung", "e3q", "SM-S938B", "e3qxxx", ""));
        // Tidak dimasukkan (nilai tidak diketahui): HMV2R, IFT50P, RM15P5G, RMX14, RMP35G, ROG6DU, ROG8P, ROG9P.
        // Tambahkan lewat tombol "Profil kustom" di aplikasi.
        DEVICES = Collections.unmodifiableList(d);

        List<Cpu> c = new ArrayList<>();
        c.add(new Cpu("sd8elitegen5", "Snapdragon 8 Elite Gen 5 (SM8850)", "QTI", "SM8850", "qcom", "canoe")); // ~
        c.add(new Cpu("sd8elite", "Snapdragon 8 Elite (SM8750)", "QTI", "SM8750", "qcom", "sun"));
        c.add(new Cpu("sd8gen3", "Snapdragon 8 Gen 3 (SM8650)", "QTI", "SM8650", "qcom", "pineapple"));
        c.add(new Cpu("sd8gen2", "Snapdragon 8 Gen 2 (SM8550)", "QTI", "SM8550", "qcom", "kalama"));
        c.add(new Cpu("dimensity9400plus", "Dimensity 9400+ (MT6991)", "Mediatek", "MT6991", "mt6991", "mt6991"));
        c.add(new Cpu("dimensity9400", "Dimensity 9400 (MT6989)", "Mediatek", "MT6989", "mt6989", "mt6989"));
        c.add(new Cpu("dimensity8350", "Dimensity 8350 (MT6897)", "Mediatek", "MT6897", "mt6897", "mt6897"));
        c.add(new Cpu("kirin9030pro", "Kirin 9030 Pro", "HiSilicon", "Kirin 9030 Pro", null, null));
        c.add(new Cpu("kirin9030s", "Kirin 9030S", "HiSilicon", "Kirin 9030S", null, null));
        c.add(new Cpu("kirin9020", "Kirin 9020", "HiSilicon", "Kirin 9020", null, null));
        c.add(new Cpu("kirin9020a", "Kirin 9020A", "HiSilicon", "Kirin 9020A", null, null));
        c.add(new Cpu("kirin9000s", "Kirin 9000S", "HiSilicon", "Kirin 9000S", null, null));
        c.add(new Cpu("kirin9000", "Kirin 9000", "HiSilicon", "Kirin 9000", null, null));
        c.add(new Cpu("xuanjie_o1", "Xiaomi Xring O1", "Xiaomi", "XRING O1", null, null));
        c.add(new Cpu("xuanjie_o3", "Xiaomi Xring O3", "Xiaomi", "XRING O3", null, null));
        CPUS = Collections.unmodifiableList(c);

        List<Gpu> g = new ArrayList<>();
        g.add(new Gpu("adreno840", "Adreno 840 · Snapdragon 8 Elite Gen 5", "Adreno (TM) 840", "Qualcomm"));
        g.add(new Gpu("adreno830", "Adreno 830 · Snapdragon 8 Elite", "Adreno (TM) 830", "Qualcomm"));
        g.add(new Gpu("adreno750", "Adreno 750 · Snapdragon 8 Gen 3", "Adreno (TM) 750", "Qualcomm"));
        g.add(new Gpu("adreno740", "Adreno 740 · Snapdragon 8 Gen 2", "Adreno (TM) 740", "Qualcomm"));
        g.add(new Gpu("adreno735", "Adreno 735 · Snapdragon 8s Gen 3", "Adreno (TM) 735", "Qualcomm"));
        g.add(new Gpu("adreno730", "Adreno 730 · Snapdragon 8 Gen 1", "Adreno (TM) 730", "Qualcomm"));
        g.add(new Gpu("adreno720", "Adreno 720 · Snapdragon 7s Gen 3", "Adreno (TM) 720", "Qualcomm"));
        g.add(new Gpu("mali_g925", "Mali-G925 Immortalis · Dimensity 9400", "Mali-G925-Immortalis MC12", "ARM"));
        g.add(new Gpu("mali_g920", "Mali-G920 Immortalis · Dimensity 9400+", "Mali-G920-Immortalis MC12", "ARM")); // ~
        g.add(new Gpu("mali_g720", "Mali-G720 Immortalis · Dimensity 9300", "Mali-G720-Immortalis MC12", "ARM"));
        g.add(new Gpu("mali_g715", "Mali-G715 Immortalis · Dimensity 9200+", "Mali-G715-Immortalis MC11", "ARM"));
        g.add(new Gpu("mali_g615", "Mali-G615 · Dimensity 8300 / 8350", "Mali-G615 MC6", "ARM"));
        g.add(new Gpu("mali_g78", "Mali-G78 MP24 · Kirin 9000", "Mali-G78 MP24", "ARM"));
        g.add(new Gpu("maleoon930", "Maleoon 930 · Kirin 9030 Pro / 9030S", "Maleoon 930", "Huawei")); // ~
        g.add(new Gpu("maleoon920", "Maleoon 920 · Kirin 9020", "Maleoon 920", "Huawei")); // ~
        g.add(new Gpu("maleoon910", "Maleoon 910 · Kirin 9010", "Maleoon 910", "Huawei")); // ~
        g.add(new Gpu("xclipse940", "Samsung Xclipse 940 · Exynos 2400", "Samsung Xclipse 940", "Samsung"));
        g.add(new Gpu("xclipse920", "Samsung Xclipse 920 · Exynos 2200", "Samsung Xclipse 920", "Samsung"));
        g.add(new Gpu("apple_a18pro", "Apple A18 Pro GPU", "Apple A18 Pro GPU", "Apple"));
        g.add(new Gpu("apple_a17pro", "Apple A17 Pro GPU", "Apple A17 Pro GPU", "Apple"));
        GPUS = Collections.unmodifiableList(g);
    }

    public static Device findDevice(String key, List<Device> custom) {
        if (key == null) return null;
        for (Device d : DEVICES) if (d.key.equals(key)) return d;
        if (custom != null) for (Device d : custom) if (d.key.equals(key)) return d;
        return null;
    }

    public static Cpu findCpu(String key) {
        if (key == null || key.isEmpty()) return null;
        for (Cpu c : CPUS) if (c.key.equals(key)) return c;
        return null;
    }

    public static Gpu findGpu(String key) {
        if (key == null || key.isEmpty()) return null;
        for (Gpu g : GPUS) if (g.key.equals(key)) return g;
        return null;
    }
}
