package com.antaru.perappspoof;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Berjalan saat proses app dimulai (sebelum kode app), sama seperti spoof di framework PenguinOS
 * "at process start": baca konfigurasi, timpa Build.*, SystemProperties, dan string GL.
 */
public class SpoofHook implements IXposedHookLoadPackage {
    private static final String TAG = "[PerAppSpoof] ";
    private static final int GL_VENDOR = 0x1F00;
    private static final int GL_RENDERER = 0x1F01;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) {
        if (Config.MODULE_PKG.equals(lpp.packageName)) {
            hookSelf(lpp);
            return;
        }
        if (!lpp.isFirstApplication) return;
        try {
            process(lpp);
        } catch (Throwable t) {
            XposedBridge.log(TAG + lpp.packageName + " error: " + t);
        }
    }

    private void hookSelf(XC_LoadPackage.LoadPackageParam lpp) {
        try {
            XposedHelpers.findAndHookMethod(Config.MODULE_PKG + ".MainActivity", lpp.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
        } catch (Throwable t) {
            XposedBridge.log(TAG + "hookSelf gagal: " + t);
        }
    }

    private void process(XC_LoadPackage.LoadPackageParam lpp) {
        final String pkg = lpp.packageName;
        XSharedPreferences xsp = new XSharedPreferences(Config.MODULE_PKG, Config.PREFS);
        xsp.makeWorldReadable();
        xsp.reload();

        List<String> report = new ArrayList<>();
        if (!xsp.getFile().canRead()) {
            report.add("FAIL konfigurasi tidak terbaca: " + xsp.getFile());
            publish(pkg, report);
            return;
        }
        if (!Config.enabled(xsp)) return;

        String modelKey = Config.perApp(xsp).get(pkg);
        String[] adv = Config.advanced(xsp).get(pkg);
        if (modelKey == null && adv == null) return; // app ada di scope tapi belum dikonfigurasi

        Map<String, String> props = new HashMap<>();
        if (modelKey != null) applyDevice(modelKey, Config.custom(xsp), props, report);
        if (adv != null) applyCpuGpu(lpp, adv[1], adv[0], props, report);
        if (!props.isEmpty()) hookSystemProperties(lpp, props, report);
        publish(pkg, report);
    }

    // ---------------------------------------------------------------- model device

    private void applyDevice(String key, List<Profiles.Device> custom,
                             Map<String, String> props, List<String> report) {
        Profiles.Device d = Profiles.findDevice(key, custom);
        if (d == null) {
            report.add("FAIL model: profil '" + key + "' tidak ditemukan");
            return;
        }
        String[][] fields = {
                {"BRAND", d.brand}, {"MANUFACTURER", d.manufacturer}, {"DEVICE", d.device},
                {"MODEL", d.model}, {"PRODUCT", d.product}, {"FINGERPRINT", d.fingerprint}};
        int ok = 0, total = 0;
        for (String[] f : fields) {
            if (empty(f[1])) continue;
            total++;
            if (setBuild(f[0], f[1])) ok++;
        }
        String[] partitions = {"ro.product", "ro.product.system", "ro.product.vendor",
                "ro.product.odm", "ro.product.product", "ro.product.system_ext"};
        for (String p : partitions) {
            put(props, p + ".brand", d.brand);
            put(props, p + ".manufacturer", d.manufacturer);
            put(props, p + ".device", d.device);
            put(props, p + ".model", d.model);
            put(props, p + ".name", d.product);
        }
        put(props, "ro.build.product", d.device);
        put(props, "ro.build.fingerprint", d.fingerprint);
        report.add((ok == total ? "OK" : "FAIL") + " model=" + d.label + " (Build " + ok + "/" + total + ")");
    }

    // ---------------------------------------------------------------- CPU & GPU

    private void applyCpuGpu(XC_LoadPackage.LoadPackageParam lpp, String cpuKey, String gpuKey,
                             Map<String, String> props, List<String> report) {
        if (!empty(cpuKey)) {
            Profiles.Cpu c = Profiles.findCpu(cpuKey);
            if (c == null) {
                report.add("FAIL cpu: preset '" + cpuKey + "' tidak ditemukan");
            } else {
                int ok = 0, total = 0;
                if (Build.VERSION.SDK_INT >= 31) {
                    total += 2;
                    if (setBuild("SOC_MANUFACTURER", c.socManufacturer)) ok++;
                    if (setBuild("SOC_MODEL", c.socModel)) ok++;
                }
                if (!empty(c.hardware)) { total++; if (setBuild("HARDWARE", c.hardware)) ok++; }
                if (!empty(c.platform)) { total++; if (setBuild("BOARD", c.platform)) ok++; }
                put(props, "ro.soc.manufacturer", c.socManufacturer);
                put(props, "ro.soc.model", c.socModel);
                put(props, "ro.hardware", c.hardware);
                put(props, "ro.boot.hardware", c.hardware);
                put(props, "ro.hardware.chipname", c.socModel);
                put(props, "ro.board.platform", c.platform);
                put(props, "ro.product.board", c.platform);
                report.add((ok == total ? "OK" : "FAIL") + " cpu=" + c.label + " (Build " + ok + "/" + total + ")");
            }
        }
        if (!empty(gpuKey)) {
            Profiles.Gpu g = Profiles.findGpu(gpuKey);
            if (g == null) {
                report.add("FAIL gpu: preset '" + gpuKey + "' tidak ditemukan");
            } else {
                int hooked = hookGl(lpp, g.renderer, g.vendor);
                report.add((hooked > 0 ? "OK" : "FAIL") + " gpu=" + g.label + " (hook GL " + hooked + ")");
            }
        }
    }

    private int hookGl(XC_LoadPackage.LoadPackageParam lpp, final String renderer, final String vendor) {
        XC_MethodHook h = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam p) {
                if (p.args.length == 0 || !(p.args[0] instanceof Integer)) return;
                int name = (Integer) p.args[0];
                if (name == GL_RENDERER && !empty(renderer)) p.setResult(renderer);
                else if (name == GL_VENDOR && !empty(vendor)) p.setResult(vendor);
            }
        };
        int n = 0;
        String[] classes = {"android.opengl.GLES20", "com.google.android.gles_jni.GLImpl"};
        for (String cn : classes) {
            try {
                Class<?> c = XposedHelpers.findClass(cn, lpp.classLoader);
                if (!XposedBridge.hookAllMethods(c, "glGetString", h).isEmpty()) n++;
            } catch (Throwable t) {
                XposedBridge.log(TAG + "hook " + cn + " gagal: " + t);
            }
        }
        return n;
    }

    // ---------------------------------------------------------------- SystemProperties

    private void hookSystemProperties(XC_LoadPackage.LoadPackageParam lpp,
                                      final Map<String, String> props, List<String> report) {
        XC_MethodHook h = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam p) {
                if (p.args.length == 0 || !(p.args[0] instanceof String)) return;
                String v = props.get(p.args[0]);
                if (v != null) p.setResult(v);
            }
        };
        try {
            Class<?> sp = XposedHelpers.findClass("android.os.SystemProperties", lpp.classLoader);
            int n = XposedBridge.hookAllMethods(sp, "get", h).size();
            report.add((n > 0 ? "OK" : "FAIL") + " SystemProperties (" + props.size() + " key, hook " + n + ")");
        } catch (Throwable t) {
            report.add("FAIL SystemProperties: " + t);
        }
    }

    // ---------------------------------------------------------------- util

    private static boolean setBuild(String name, String value) {
        try {
            Field f = Build.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(null, value);
            return true;
        } catch (Throwable t) {
            XposedBridge.log(TAG + "set Build." + name + " gagal: " + t);
            return false;
        }
    }

    private static void put(Map<String, String> m, String k, String v) {
        if (!empty(v)) m.put(k, v);
    }

    private static boolean empty(String s) {
        return s == null || s.isEmpty();
    }

    /** Catat ke log Xposed, lalu kirim ke modul (yang menulis ke /data/local/tmp lewat root). */
    private void publish(final String pkg, List<String> report) {
        boolean fail = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < report.size(); i++) {
            String r = report.get(i);
            if (r.startsWith("FAIL")) fail = true;
            if (i > 0) sb.append(" | ");
            sb.append(r);
        }
        final String msg = (fail ? "FAIL" : "OK") + " pkg=" + pkg + " " + sb;
        XposedBridge.log(TAG + msg);
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                private boolean sent;

                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (sent) return;
                    sent = true;
                    try {
                        Intent i = new Intent(Config.ACTION_LOG);
                        i.setComponent(new ComponentName(Config.MODULE_PKG, Config.MODULE_PKG + ".LogReceiver"));
                        i.putExtra("msg", msg);
                        ((Context) p.args[0]).sendBroadcast(i);
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + "kirim log gagal: " + t);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(TAG + "hook Application.attach gagal: " + t);
        }
    }
}
