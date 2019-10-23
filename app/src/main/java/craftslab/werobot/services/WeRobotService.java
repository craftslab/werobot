package craftslab.werobot.services;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import craftslab.werobot.utils.PowerUtil;
import craftslab.werobot.utils.RandomMsg;

public class WeRobotService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "WeRobotService";
    private boolean mMutex = false;
    private PowerUtil powerUtil;
    private SharedPreferences sharedPreferences;
    private String receiverName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (sharedPreferences == null) {
            return;
        }

        setCurrentActivityName(event);

        if (!mMutex) {
            mMutex = true;
            watchChat(event);
            mMutex = false;
        }
    }

    private void setCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );
            getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO
        }
    }

    private void watchChat(AccessibilityEvent event) {
        int types = event.getContentChangeTypes();
        if ((types & AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) == 0
                || (types & AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE) == 0) {
            return;
        }

        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        receiverName = sharedPreferences.getString("pref_set_receiver", "");
        if (!receiverName.isEmpty()) {
            // TODO
            findLastReplyNode(rootNode);
        }

        final Handler handler = new Handler();
        int delaySecs = sharedPreferences.getInt("pref_send_delay", 0) * 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(rootNode);
            }
        }, delaySecs);
    }

    // FOR DEBUGGING ONLY
    private void walkAllNodes(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        Log.d(TAG, "node name: " + node.getClassName()
                + " description: " + node.getContentDescription()
                + " childs: " + node.getChildCount());
        if (node.getChildCount() == 0) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); ++i) {
            walkAllNodes(node.getChild(i));
        }
    }

    private AccessibilityNodeInfo findLastReplyNode(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo replyNode = fetchLastListNode(node);
        if (replyNode == null) {
            return null;
        }

        if (replyNode.getChildCount() == 1
                && "android.widget.RelativeLayout".contentEquals(replyNode.getChild(0).getClassName().toString())) {
            return fetchViewNode(replyNode.getChild(0));
        } else if (replyNode.getChildCount() == 2
                && "android.widget.RelativeLayout".contentEquals(replyNode.getChild(0).getClassName().toString())
                && "android.widget.RelativeLayout".contentEquals(replyNode.getChild(1).getClassName().toString())) {
            return fetchViewNode(replyNode.getChild(1));
        }

        return null;
    }

    private AccessibilityNodeInfo fetchLastListNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        if ("android.widget.ListView".contentEquals(node.getClassName().toString())) {
            return node;
        }

        for (int i = node.getChildCount() - 1; i >= 0; --i) {
            AccessibilityNodeInfo listNode = fetchLastListNode(node.getChild(i));
            if (listNode != null) {
                return listNode;
            }
        }

        return null;
    }

    private AccessibilityNodeInfo fetchViewNode(AccessibilityNodeInfo node) {
        if (node.getChildCount() != 2) {
            return null;
        }

        AccessibilityNodeInfo nameNode = node.getChild(0);
        AccessibilityNodeInfo contentNode = node.getChild(1);
        AccessibilityNodeInfo buf = null;

        if ("android.widget.ImageView".contentEquals(nameNode.getClassName().toString())) {
            if (nameNode.getContentDescription() != null) {
                String name = nameNode.getContentDescription().toString().replace("头像", "");
                if (receiverName.contentEquals(name)) {
                    if ("android.view.View".contentEquals(contentNode.getClassName().toString())) {
                        // TODO
                        buf = contentNode;
                    }
                }
            }
        }

        return buf;
    }

    private void sendMessage(AccessibilityNodeInfo node) {
        String message;

        if (sharedPreferences.getBoolean("pref_send_random", false)) {
            String randomLen = sharedPreferences.getString("pref_random_len", "100");
            message = RandomMsg.unicode(Integer.parseInt(randomLen));
        } else {
            // TODO
            message = "Hello World!";
        }

        try {
            AccessibilityNodeInfo inputNode = findInputNode(node);
            if (inputNode != null) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                AccessibilityNodeInfo sendNode = findSendNode(rootNode);
                if (sendNode != null) {
                    sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        } catch (Exception e) {
            // TODO
        }
    }

    private AccessibilityNodeInfo findInputNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        if (node.getChildCount() == 0) {
            if ("android.widget.EditText".contentEquals(node.getClassName().toString()))
                return node;
            else
                return null;
        }

        AccessibilityNodeInfo inputNode;
        for (int i = node.getChildCount() - 1; i >= 0; --i) {
            inputNode = findInputNode(node.getChild(i));
            if (inputNode != null) {
                return inputNode;
            }
        }

        return null;
    }

    private AccessibilityNodeInfo findSendNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        if (node.getChildCount() == 0) {
            if ("android.widget.Button".contentEquals(node.getClassName().toString()) && node.getText().toString().contains("发送")) {
                return node;
            } else {
                return null;
            }
        }

        AccessibilityNodeInfo sendNode;
        for (int i = node.getChildCount() - 1; i >= 0; --i) {
            sendNode = findSendNode(node.getChild(i));
            if (sendNode != null) {
                return sendNode;
            }
        }

        return null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        this.watchFlagsFromPreference();
    }

    private void watchFlagsFromPreference() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.powerUtil = new PowerUtil(this);
        this.powerUtil.handleWakeLock(true);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public void onDestroy() {
        this.powerUtil.handleWakeLock(false);
        super.onDestroy();
    }
}
