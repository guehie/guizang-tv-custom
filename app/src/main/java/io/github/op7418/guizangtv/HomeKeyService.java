package io.github.op7418.guizangtv;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/** This Xiaomi firmware broadcasts homekey while bypassing accessibility key callbacks. */
public final class HomeKeyService extends AccessibilityService {
    private static HomeKeyService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered;
    private long allowStockHomeUntil;
    private final Runnable returnHome = () -> {
        PowerManager power = (PowerManager)getSystemService(POWER_SERVICE);
        if (!enabled(this) || power == null || !power.isInteractive()) return;
        try {
            startActivity(new Intent(this, FrameHomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            Log.i("GuiZangHome", "System Home notification -> GuiZang");
        } catch (RuntimeException unavailable) {
            Log.w("GuiZangHome", "Cannot return Home", unavailable);
        }
    };
    private final BroadcastReceiver homeKey = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!XiaomiControls.enabled() || !enabled(context)
                    || !Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())
                    || !"homekey".equals(intent.getStringExtra("reason"))) return;
            if(SystemClock.uptimeMillis()<allowStockHomeUntil){allowStockHomeUntil=0;return;}
            handler.removeCallbacks(returnHome);
            // Let Xiaomi's synchronous Home action settle before returning our existing task.
            handler.postDelayed(returnHome, 150);
        }
    };
    private static boolean enabled(Context c) {
        return c.getSharedPreferences("home-key", MODE_PRIVATE).getBoolean("enabled", true);
    }
    static boolean active() { return instance != null && enabled(instance); }
    static boolean openStockHome() {
        HomeKeyService service=instance;
        if(service==null)return false;
        // On this firmware the public alias inherits HOME_STATE from its target.
        // Ask Android to perform Home; let this one intentional visit remain visible.
        service.handler.removeCallbacks(service.returnHome);
        service.allowStockHomeUntil=SystemClock.uptimeMillis()+2000;
        boolean opened=service.performGlobalAction(GLOBAL_ACTION_HOME);
        if(!opened)service.allowStockHomeUntil=0;
        Log.i("GuiZangHome","Intentional stock Home requested: "+opened);
        return opened;
    }

    static void show(Activity a) {
        boolean active = active();
        new AlertDialog.Builder(a).setTitle("Home 键回到归藏")
                .setMessage("当前：" + (active ? "已启用" : "未启用")
                        + "\n\n收到系统主页键通知后返回归藏，长按保持系统行为。小米原桌面仍可从首页进入。"
                        + "\n\n不读取屏幕内容，不过滤其他按键。")
                .setPositiveButton(active ? "关闭" : "开启", (d, which) -> {
                    a.getSharedPreferences("home-key", MODE_PRIVATE).edit().putBoolean("enabled", !active).apply();
                    if (instance != null) instance.handler.removeCallbacks(instance.returnHome);
                    if (!active && instance == null) {
                        try { a.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
                        catch (RuntimeException unavailable) {
                            Toast.makeText(a, "本机无无障碍设置入口，需由电脑启用归藏主页键服务", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton("返回", null).show();
    }
    @Override protected void onServiceConnected() {
        instance = this;
        if (!registered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            // Only the system may request a Home return through this receiver.
            if (Build.VERSION.SDK_INT >= 33)
                registerReceiver(homeKey, filter, "android.permission.STOP_APP_SWITCHES", handler, RECEIVER_NOT_EXPORTED);
            else registerLegacy(filter);
            registered = true;
        }
        Log.i("GuiZangHome", "System Home notification listener connected");
    }
    // Before API 33 there is no export flag; the signature permission restricts the sender.
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLegacy(IntentFilter filter) {
        registerReceiver(homeKey, filter, "android.permission.STOP_APP_SWITCHES", handler);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { handler.removeCallbacks(returnHome); }
    private void disconnect() {
        if (instance == this) instance = null;
        handler.removeCallbacksAndMessages(null);
        if (registered) { unregisterReceiver(homeKey); registered = false; }
    }
    @Override public boolean onUnbind(Intent intent) {
        disconnect();
        return super.onUnbind(intent);
    }
    @Override public void onDestroy() { disconnect(); super.onDestroy(); }
}
