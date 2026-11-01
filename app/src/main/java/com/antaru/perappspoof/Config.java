package com.antaru.perappspoof;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Format data meniru Settings.Secure milik PenguinOS:
 *  perapp   = {"pkg":"PROFILE_KEY"}                    (PER_APPS_DEVICE_SPOOF; bentuk persisnya tidak terlihat di Settings)
 *  advanced = [{"pkg":..,"gpu":..,"cpu":..}]           (ADVANCED_APP_SPOOF_CONFIG)
 *  enabled  = master switch                            (PER_APPS_DEVICE_SPOOF_ENABLED / ADVANCED_APP_SPOOF_ENABLED)
 *  custom   = profil kustom                            (CUSTOM_SPOOF_PROFILES)
 */
public final class Config {
    private Config() {}

    public static final String MODULE_PKG = "com.antaru.perappspoof";
    public static final String PREFS = "config";
    public static final String ACTION_LOG = "com.antaru.perappspoof.LOG";
    public static final String LOG_FILE = "/data/local/tmp/perapp_spoof.log";

    private static final String K_ENABLED = "enabled";
    private static final String K_PERAPP = "perapp";
    private static final String K_ADVANCED = "advanced";
    private static final String K_CUSTOM = "custom_profiles";

    /** Lempar SecurityException bila LSPosed belum aktif untuk modul ini. */
    @SuppressWarnings("deprecation")
    public static SharedPreferences openWorldReadable(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_WORLD_READABLE);
    }

    public static boolean enabled(SharedPreferences sp) {
        return sp.getBoolean(K_ENABLED, true);
    }

    public static Map<String, String> perApp(SharedPreferences sp) {
        Map<String, String> out = new LinkedHashMap<>();
        String s = sp.getString(K_PERAPP, null);
        if (s == null) return out;
        try {
            JSONObject o = new JSONObject(s);
            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                out.put(k, o.optString(k));
            }
        } catch (Exception ignored) {}
        return out;
    }

    /** value = {gpuKey, cpuKey} */
    public static Map<String, String[]> advanced(SharedPreferences sp) {
        Map<String, String[]> out = new LinkedHashMap<>();
        String s = sp.getString(K_ADVANCED, null);
        if (s == null) return out;
        try {
            JSONArray a = new JSONArray(s);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.put(o.optString("pkg"), new String[]{o.optString("gpu"), o.optString("cpu")});
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static List<Profiles.Device> custom(SharedPreferences sp) {
        List<Profiles.Device> out = new ArrayList<>();
        String s = sp.getString(K_CUSTOM, null);
        if (s == null) return out;
        try {
            JSONArray a = new JSONArray(s);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.add(new Profiles.Device(o.optString("id"), o.optString("name"),
                        o.optString("brand"), o.optString("manufacturer"), o.optString("device"),
                        o.optString("model"), o.optString("product"), o.optString("fingerprint")));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static boolean save(SharedPreferences sp, boolean enabled,
                               Map<String, String> perapp, Map<String, String[]> adv) {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, String> e : perapp.entrySet()) {
            try { o.put(e.getKey(), e.getValue()); } catch (Exception ignored) {}
        }
        JSONArray a = new JSONArray();
        for (Map.Entry<String, String[]> e : adv.entrySet()) {
            try {
                JSONObject r = new JSONObject();
                r.put("pkg", e.getKey());
                r.put("gpu", e.getValue()[0]);
                r.put("cpu", e.getValue()[1]);
                a.put(r);
            } catch (Exception ignored) {}
        }
        return sp.edit().putBoolean(K_ENABLED, enabled)
                .putString(K_PERAPP, o.toString())
                .putString(K_ADVANCED, a.toString())
                .commit();
    }

    public static boolean saveCustom(SharedPreferences sp, List<Profiles.Device> list) {
        JSONArray a = new JSONArray();
        for (Profiles.Device d : list) {
            try {
                JSONObject o = new JSONObject();
                o.put("id", d.key);
                o.put("name", d.label);
                o.put("brand", d.brand);
                o.put("manufacturer", d.manufacturer);
                o.put("device", d.device);
                o.put("model", d.model);
                o.put("fingerprint", d.fingerprint);
                o.put("product", d.product);
                a.put(o);
            } catch (Exception ignored) {}
        }
        return sp.edit().putString(K_CUSTOM, a.toString()).commit();
    }
}
