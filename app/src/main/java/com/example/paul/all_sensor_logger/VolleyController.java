package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.auth0.android.result.Credentials;

/**
 * Created by APaul on 2015/11/1.
 */
public class VolleyController extends Application {
    public static final String TAG = VolleyController.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static VolleyController mInstance;
    private Credentials mUserCredentials;
    public static final int NOTIFY_SERVICE_ID = 100;
    public static final int REQUEST_NOTIFICATION_SERVICE = 0x01;
    public static final int REQUEST_CODE_EDIT = 0x01 << 2;

    public static final String AUTH0_idToken = "AUTH0_idToken";
    public static final String CAR_INFO_NAME = "CAR_INFO_NAME";
    public static final String CAR_INFO_CARTYPE = "CAR_INFO_CARTYPE";
    public static final String CAR_INFO_CARAGE = "CAR_INFO_CARAGE";
    public static final String LIST_VIEW_POSITION = "LIST_VIEW_POSITION";


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized VolleyController getInstance() {
        if(mInstance==null)
        {
            mInstance=new VolleyController();

        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        (getRequestQueue()).add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public Credentials getUserCredentials() {
        return mUserCredentials;
    }

    public void setUserCredentials(Credentials userCredentials) {
        this.mUserCredentials = userCredentials;
    }

    public static void notificationServiceStartBuilder(Activity activity) {
        final int notifyID = NOTIFY_SERVICE_ID; // 通知的識別號碼
        final boolean autoCancel = false; // 點擊通知後是否要自動移除掉通知
        final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效
        final int requestCode = REQUEST_NOTIFICATION_SERVICE; // PendingIntent的Request Code
        final Intent intent = activity.getIntent(); // 目前Activity的Intent
        intent.setClass(activity, MainActivity.class);



        final int flags = PendingIntent.FLAG_UPDATE_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), requestCode, intent, flags); // 取得PendingIntent

        final NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        final Notification notification = new Notification.Builder(activity.getApplicationContext()).setSmallIcon(R.drawable.base_main_2).setContentTitle(activity.getString(R.string.notification_title)).setContentText(activity.getString(R.string.toggle_on)).setSound(soundUri).setContentIntent(pendingIntent).setAutoCancel(autoCancel).build(); // 建立通知
        notification.flags = Notification.FLAG_ONGOING_EVENT; //將訊息常駐在status bar
        notificationManager.notify(notifyID, notification); // 發送通知
    }

    public static void cancelNotificationService(Activity activity) {
        final int notifyID = NOTIFY_SERVICE_ID;
        final NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        notificationManager.cancel(notifyID);
    }


}