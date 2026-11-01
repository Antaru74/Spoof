package com.antaru.perappspoof;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Menerima laporan hook dari proses game dan menuliskannya ke /data/local/tmp/perapp_spoof.log. */
public class LogReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Config.ACTION_LOG.equals(intent.getAction())) return;
        final String msg = intent.getStringExtra("msg");
        if (msg == null) return;
        final PendingResult pr = goAsync();
        new Thread(() -> {
            try {
                RootLog.append("HOOK " + msg);
            } finally {
                pr.finish();
            }
        }).start();
    }
}
