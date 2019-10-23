package craftslab.werobot.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

public class PowerUtil {
    private PowerManager.WakeLock wakeLock;

    public PowerUtil(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "WeRobot:Wakelock");
    }

    private void acquire() {
        wakeLock.acquire(1800000);
    }

    private void release() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void handleWakeLock(boolean isWake) {
        if (isWake) {
            this.acquire();
        } else {
            this.release();
        }
    }
}
