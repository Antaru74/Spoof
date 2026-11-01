package com.antaru.perappspoof;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    /** Di-hook oleh SpoofHook.hookSelf -> true bila modul aktif di LSPosed. */
    static boolean isModuleActive() { return false; }

    private SharedPreferences sp;
    private boolean worldReadable = true;

    private EditText etPkg;
    private Switch swMaster, swModel, swCpuGpu;
    private Spinner spModel, spCpu, spGpu;
    private TextView tvStatus;
    private LinearLayout listBox;
    private final List<Profiles.Device> models = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            sp = Config.openWorldReadable(this);
        } catch (SecurityException e) {
            worldReadable = false;
            sp = getSharedPreferences(Config.PREFS, MODE_PRIVATE);
        }
        setContentView(buildUi());
        reloadModels();
        refresh();
        String s = isModuleActive() ? "Modul aktif di LSPosed." : "Modul belum aktif: aktifkan di LSPosed, centang modul ini dan game di scope, lalu buka ulang.";
        if (!worldReadable) s += "\nKonfigurasi tidak bisa dibaca game (MODE_WORLD_READABLE ditolak).";
        status(s);
    }

    // ------------------------------------------------------------------ UI

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView label(String t) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setPadding(0, dp(12), 0, dp(4));
        tv.setTextSize(15);
        return tv;
    }

    private View buildUi() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(48));
        sv.addView(root);

        swMaster = new Switch(this);
        swMaster.setText("Aktifkan spoofing (master)");
        swMaster.setChecked(Config.enabled(sp));
        swMaster.setOnCheckedChangeListener((v, on) -> setMaster(on));
        root.addView(swMaster);

        root.addView(label("Package name aplikasi/game"));
        etPkg = new EditText(this);
        etPkg.setHint("contoh: com.mobile.legends");
        etPkg.setSingleLine(true);
        etPkg.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        root.addView(etPkg);

        swModel = new Switch(this);
        swModel.setText("Spoof model device");
        swModel.setChecked(true);
        swModel.setPadding(0, dp(16), 0, 0);
        root.addView(swModel);
        spModel = new Spinner(this);
        root.addView(spModel);
        Button bCustom = new Button(this);
        bCustom.setText("Profil kustom baru");
        bCustom.setOnClickListener(v -> showCustomDialog());
        root.addView(bCustom);

        swCpuGpu = new Switch(this);
        swCpuGpu.setText("Spoof CPU & GPU (saja)");
        swCpuGpu.setPadding(0, dp(16), 0, 0);
        root.addView(swCpuGpu);
        root.addView(label("CPU"));
        spCpu = new Spinner(this);
        List<String> cpus = new ArrayList<>();
        cpus.add("None");
        for (Profiles.Cpu c : Profiles.CPUS) cpus.add(c.label);
        spCpu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cpus));
        root.addView(spCpu);
        root.addView(label("GPU"));
        spGpu = new Spinner(this);
        List<String> gpus = new ArrayList<>();
        gpus.add("None");
        for (Profiles.Gpu g : Profiles.GPUS) gpus.add(g.label);
        spGpu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gpus));
        root.addView(spGpu);

        Button bApply = new Button(this);
        bApply.setText("Apply");
        bApply.setOnClickListener(v -> onApply());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(20);
        root.addView(bApply, lp);

        tvStatus = new TextView(this);
        tvStatus.setPadding(0, dp(12), 0, dp(12));
        root.addView(tvStatus);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = new TextView(this);
        t.setText("Daftar app terkonfigurasi");
        t.setTextSize(16);
        head.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        Button bClear = new Button(this);
        bClear.setText("Clear all");
        bClear.setOnClickListener(v -> confirmClearAll());
        head.addView(bClear);
        root.addView(head);

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(listBox);

        TextView foot = new TextView(this);
        foot.setText("Log: " + Config.LOG_FILE);
        foot.setPadding(0, dp(16), 0, 0);
        root.addView(foot);
        return sv;
    }

    private void status(String s) { tvStatus.setText(s); }

    private void reloadModels() {
        models.clear();
        models.addAll(Profiles.DEVICES);
        models.addAll(Config.custom(sp));
        List<String> labels = new ArrayList<>();
        for (Profiles.Device d : models) labels.add(d.label);
        spModel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
    }

    private void refresh() {
        listBox.removeAllViews();
        Map<String, String> perapp = Config.perApp(sp);
        Map<String, String[]> adv = Config.advanced(sp);
        Set<String> all = new LinkedHashSet<>(perapp.keySet());
        all.addAll(adv.keySet());
        if (all.isEmpty()) {
            TextView e = new TextView(this);
            e.setText("Belum ada app terkonfigurasi");
            e.setPadding(0, dp(8), 0, 0);
            listBox.addView(e);
            return;
        }
        List<Profiles.Device> custom = Config.custom(sp);
        for (final String pkg : all) {
            StringBuilder sb = new StringBuilder(pkg);
            String mk = perapp.get(pkg);
            if (mk != null) {
                Profiles.Device d = Profiles.findDevice(mk, custom);
                sb.append("\n  model: ").append(d != null ? d.label : mk + " (hilang)");
            }
            String[] a = adv.get(pkg);
            if (a != null) {
                Profiles.Cpu c = Profiles.findCpu(a[1]);
                Profiles.Gpu g = Profiles.findGpu(a[0]);
                sb.append("\n  CPU: ").append(c != null ? c.label : "-");
                sb.append("\n  GPU: ").append(g != null ? g.label : "-");
            }
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));
            TextView tv = new TextView(this);
            tv.setText(sb.toString());
            row.addView(tv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            Button del = new Button(this);
            del.setText("Hapus");
            del.setOnClickListener(v -> confirmRemove(pkg));
            row.addView(del);
            listBox.addView(row);
        }
    }

    // ------------------------------------------------------------------ aksi

    private void onApply() {
        final String pkg = etPkg.getText().toString().trim();
        final boolean modelOn = swModel.isChecked();
        final boolean cgOn = swCpuGpu.isChecked();
        final int mi = spModel.getSelectedItemPosition();
        final String modelKey = (mi >= 0 && mi < models.size()) ? models.get(mi).key : null;
        final int ci = spCpu.getSelectedItemPosition();
        final int gi = spGpu.getSelectedItemPosition();
        final String cpuKey = ci > 0 ? Profiles.CPUS.get(ci - 1).key : "";
        final String gpuKey = gi > 0 ? Profiles.GPUS.get(gi - 1).key : "";
        status("Menerapkan...");
        new Thread(() -> {
            final String result = doApply(pkg, modelOn, modelKey, cgOn, cpuKey, gpuKey);
            RootLog.append("APPLY " + result);
            runOnUiThread(() -> { status(result); refresh(); });
        }).start();
    }

    private String doApply(String pkg, boolean modelOn, String modelKey,
                           boolean cgOn, String cpuKey, String gpuKey) {
        if (!pkg.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+"))
            return "FAIL package name tidak valid: '" + pkg + "'";
        if (!modelOn && !cgOn)
            return "FAIL pkg=" + pkg + " pilih minimal satu: spoof model device atau spoof CPU & GPU";
        try {
            getPackageManager().getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return "FAIL pkg=" + pkg + " aplikasi tidak terpasang";
        }
        if (!worldReadable)
            return "FAIL pkg=" + pkg + " LSPosed belum aktif untuk modul ini, konfigurasi tidak akan terbaca game";

        Map<String, String> perapp = new LinkedHashMap<>(Config.perApp(sp));
        Map<String, String[]> adv = new LinkedHashMap<>(Config.advanced(sp));
        if (modelOn) {
            if (modelKey == null) return "FAIL pkg=" + pkg + " profil model belum dipilih";
            perapp.put(pkg, modelKey);
        } else {
            perapp.remove(pkg);
        }
        if (cgOn) {
            if (cpuKey.isEmpty() && gpuKey.isEmpty())
                return "FAIL pkg=" + pkg + " pilih CPU atau GPU (tidak boleh keduanya None)";
            adv.put(pkg, new String[]{gpuKey, cpuKey});
        } else {
            adv.remove(pkg);
        }
        if (!Config.save(sp, Config.enabled(sp), perapp, adv))
            return "FAIL pkg=" + pkg + " gagal menulis konfigurasi (commit false)";

        boolean stopped = RootLog.su("am force-stop " + pkg);
        return "OK pkg=" + pkg + " model=" + (modelOn ? modelKey : "-")
                + " cpu=" + (cgOn && !cpuKey.isEmpty() ? cpuKey : "-")
                + " gpu=" + (cgOn && !gpuKey.isEmpty() ? gpuKey : "-")
                + " forceStop=" + (stopped ? "ok" : "gagal (root?)");
    }

    private void setMaster(final boolean on) {
        final Map<String, String> perapp = Config.perApp(sp);
        final Map<String, String[]> adv = Config.advanced(sp);
        final Set<String> targets = new LinkedHashSet<>(perapp.keySet());
        targets.addAll(adv.keySet());
        new Thread(() -> {
            boolean saved = Config.save(sp, on, perapp, adv);
            for (String p : targets) RootLog.su("am force-stop " + p);
            String r = (saved ? "OK" : "FAIL") + " master=" + (on ? "on" : "off") + " apps=" + targets.size();
            RootLog.append("MASTER " + r);
            runOnUiThread(() -> status(r));
        }).start();
    }

    private void confirmRemove(final String pkg) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus app?")
                .setMessage("Hapus " + pkg + " dari daftar spoofing?")
                .setPositiveButton("Hapus", (d, w) -> removePkgs(java.util.Collections.singleton(pkg)))
                .setNegativeButton("Batal", null).show();
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all")
                .setMessage("Semua konfigurasi spoofing akan dihapus dan app di-force-stop.")
                .setPositiveButton("Clear all", (d, w) -> {
                    Set<String> all = new LinkedHashSet<>(Config.perApp(sp).keySet());
                    all.addAll(Config.advanced(sp).keySet());
                    removePkgs(all);
                })
                .setNegativeButton("Batal", null).show();
    }

    private void removePkgs(final Set<String> pkgs) {
        new Thread(() -> {
            Map<String, String> perapp = new LinkedHashMap<>(Config.perApp(sp));
            Map<String, String[]> adv = new LinkedHashMap<>(Config.advanced(sp));
            for (String p : pkgs) { perapp.remove(p); adv.remove(p); }
            boolean saved = Config.save(sp, Config.enabled(sp), perapp, adv);
            for (String p : pkgs) RootLog.su("am force-stop " + p);
            String r = (saved ? "OK" : "FAIL") + " removed=" + pkgs;
            RootLog.append("REMOVE " + r);
            runOnUiThread(() -> { status(r); refresh(); });
        }).start();
    }

    private void showCustomDialog() {
        final String[] hints = {"Profile name", "BRAND", "MANUFACTURER", "DEVICE", "MODEL",
                "FINGERPRINT (Optional)", "PRODUCT (Optional)"};
        final EditText[] f = new EditText[hints.length];
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        for (int i = 0; i < hints.length; i++) {
            f[i] = new EditText(this);
            f[i].setHint(hints[i]);
            f[i].setSingleLine(true);
            box.addView(f[i]);
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        new AlertDialog.Builder(this)
                .setTitle("Add Custom Spoof Profile")
                .setView(sv)
                .setPositiveButton("Save", (d, w) -> {
                    for (int i = 0; i < 5; i++) {
                        if (f[i].getText().toString().trim().isEmpty()) {
                            status("FAIL profil kustom: " + hints[i] + " wajib diisi");
                            return;
                        }
                    }
                    List<Profiles.Device> list = new ArrayList<>(Config.custom(sp));
                    list.add(new Profiles.Device("CUSTOM_" + System.currentTimeMillis(),
                            f[0].getText().toString().trim(), f[1].getText().toString().trim(),
                            f[2].getText().toString().trim(), f[3].getText().toString().trim(),
                            f[4].getText().toString().trim(), f[6].getText().toString().trim(),
                            f[5].getText().toString().trim()));
                    boolean ok = Config.saveCustom(sp, list);
                    reloadModels();
                    status(ok ? "OK profil kustom disimpan" : "FAIL profil kustom gagal disimpan");
                })
                .setNegativeButton("Cancel", null).show();
    }
}
