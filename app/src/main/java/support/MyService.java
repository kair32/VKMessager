package support;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.example.kirill.vkmessager.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import activity.MainActivity;
import activity.UserDialog;
import utilis.CachingDataUsers;
import utilis.Constants;

public class MyService extends Service {
    NotificationManager nm;
    private int mVkTS = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer mTimer = new Timer();
        MyTimerTask mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 5000 , 5000);
        return super.onStartCommand(intent, flags, startId);
    }


    void sendNotif() {
        VKRequest request = new VKRequest("messages.getLongPollHistory", VKParameters.from("ts", mVkTS)
        );
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) response.json.get("response");
                    JSONObject jsonObject1 = jsonObject.getJSONObject("messages");
                    JSONArray jsonObject2 = jsonObject1.getJSONArray("items");
                    final String mBodyMSG = jsonObject2.getJSONObject(0).getString("body");
                    if(jsonObject2.getJSONObject(0).getString("out").equals("0")) {
                        JSONArray jsonObjectProfil = jsonObject.getJSONArray("profiles");
                        final String mProfil = jsonObjectProfil.getJSONObject(0).getString("first_name") + " " + jsonObjectProfil.getJSONObject(0).getString("last_name");
                        String mPhotoURL = jsonObjectProfil.getJSONObject(0).getString("photo_medium_rec");
                        Constants.ID = Integer.parseInt(jsonObjectProfil.getJSONObject(0).getString("id"));

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Notification.Builder builder = new Notification.Builder(getBaseContext());
                                Intent notificationIntent = new Intent(getBaseContext(),  UserDialog.class);
                                PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                                        0, notificationIntent,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                                builder.setLargeIcon(bitmap);
                                builder.setContentIntent(contentIntent)
                                        .setSmallIcon(R.drawable.ic_ab_app)
                                        .setTicker(mBodyMSG)
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setContentTitle(mProfil)
                                        .setContentText(mBodyMSG);

                                Notification notification = builder.build();
                                notification.vibrate = new long[] { 100, 100, 100, 100 };
                                notification.ledARGB = Color.BLUE;
                                notification.ledOffMS = 100;
                                notification.ledOnMS = 100;
                                notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;
                                NotificationManager notificationManager = (NotificationManager) getBaseContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);
                                final int NOTIFY_ID = 101;
                                notificationManager.notify(NOTIFY_ID, notification);
                            }
                            @Override public void onBitmapFailed(Drawable errorDrawable) {}
                            @Override public void onPrepareLoad(Drawable placeHolderDrawable) {}
                        };
                        ContextWrapper cw = new ContextWrapper(getBaseContext());
                        File file = cw.getDir("photo", Context.MODE_PRIVATE);
                        cw.deleteDatabase("photo");
                        File fileqs = null;
                        File[] files = file.listFiles();
                        for (int i =0; i < files.length; i++) {
                            String str = mPhotoURL.replace(".", "").replace("/", "").replace(":", "") + ".png";
                            if (str.equals(files[i].getName())) {
                                mPhotoURL = files[i].getAbsolutePath();
                                fileqs = new File(files[i].getAbsolutePath());
                                }
                        }
                        Picasso.with(getBaseContext())
                                .load(fileqs)
                                .into(target);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MyService()   {
    }
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            VKRequest request = new VKRequest("messages.getLongPollServer");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    JSONObject jsonObject = response.json;
                    try {
                        JSONObject json = jsonObject.getJSONObject("response");
                        if(json.getInt("ts") == mVkTS);
                        else {if(mVkTS == 0) mVkTS=json.getInt("ts");
                            sendNotif();
                            mVkTS=json.getInt("ts");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
