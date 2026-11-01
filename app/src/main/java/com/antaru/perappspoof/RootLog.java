package com.antaru.perappspoof;

import android.util.Log;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Tulis log ke /data/local/tmp lewat root (app biasa tidak boleh menulis ke sana). */
public final class RootLog {
    private RootLog() {}

    private static final String TAG = "PerAppSpoof";

    public static boolean su(String cmd) {
        Process p = null;
        try {
            p = new ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start();
            InputStream in = p.getInputStream();
            byte[] buf = new byte[1024];
            while (in.read(buf) > 0) { /* habiskan output */ }
            return p.waitFor() == 0;
        } catch (Throwable t) {
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }

    public static synchronized boolean append(String line) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        String full = (ts + " " + line).replace("\n", " | ").replace("\r", " ");
        if (full.length() > 800) full = full.substring(0, 800);
        String esc = full.replace("'", "'\\''");
        boolean ok = su("echo '" + esc + "' >> " + Config.LOG_FILE + " && chmod 666 " + Config.LOG_FILE);
        if (!ok) Log.e(TAG, "gagal menulis " + Config.LOG_FILE + " (root?) : " + full);
        return ok;
    }
}
