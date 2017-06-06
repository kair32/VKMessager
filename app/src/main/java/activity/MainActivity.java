package activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kirill.vkmessager.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.util.Timer;
import java.util.TimerTask;

import fragments.Contacts_Fragment;
import fragments.Dialogs_Fragment;
import fragments.Setting_Fragment;
import jp.wasabeef.blurry.Blurry;
import support.MyService;
import support.CachingDataUsers;
import support.Constants;

@TargetApi(Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    final CachingDataUsers cachingDataUsers = new CachingDataUsers();
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



    private ImageView vAvatarImageView, vBackgroundImageView;
    private TextView vFullNameTextView;
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 600);


        verifyStoragePermissions(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_contacts);
        View hView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        mSettings = getSharedPreferences(Constants.SETTING_NAME, Context.MODE_PRIVATE);
        vFullNameTextView = (TextView) hView.findViewById(R.id.nav_header_name_textView);
        vBackgroundImageView = (ImageView)hView.findViewById(R.id.nav_headler_start_main_image_view);
        vAvatarImageView = (ImageView)hView.findViewById(R.id.nav_header_imageView);
        UserData(mSettings.getString("User_name", ""), mSettings.getString("User_photo", ""));


        Intent service = new Intent(this, MyService.class);
        this.startService(service);

        setTitle("Контакты");
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                    switch (res) {
                        case LoggedOut:
                            startActivity(new Intent(getBaseContext(), SignInActivity.class));
                            break;
                        case LoggedIn:
                            Constants.FragmentTransaction = getSupportFragmentManager().beginTransaction();
                            Constants.FragmentTransaction.addToBackStack(null);
                            Constants.FragmentTransaction.replace(R.id.container,new Contacts_Fragment()).commit();
                            break;
                        case Pending:
                            Constants.FragmentTransaction = getSupportFragmentManager().beginTransaction();
                            Constants.FragmentTransaction.addToBackStack(null);
                            Constants.FragmentTransaction.replace(R.id.container,new Contacts_Fragment()).commit();
                            break;
                        case Unknown:
                            break;
                    }
            }
            @Override
            public void onError(VKError error) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        vBackgroundImageView.setImageResource (R.mipmap.ic_launcher);
        /*Blurry.with(MainActivity.this)
                .radius(10)
                .sampling(4)
                .async()
                .capture(vBackgroundImageView)
                .into(vBackgroundImageView);*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_contacts:
                Constants.FragmentTransaction = getSupportFragmentManager().beginTransaction();
                Constants.FragmentTransaction.addToBackStack(null);
                Constants.FragmentTransaction.replace(R.id.container,new Contacts_Fragment()).commit();
                break;
            case R.id.nav_dialogs:
                Constants.FragmentTransaction = getSupportFragmentManager().beginTransaction();
                Constants.FragmentTransaction.addToBackStack(null);
                Constants.FragmentTransaction.replace(R.id.container,new Dialogs_Fragment()).commit();
                break;
            case R.id.nav_setting:
                Constants.FragmentTransaction = getSupportFragmentManager().beginTransaction();
                Constants.FragmentTransaction.addToBackStack(null);
                Constants.FragmentTransaction.replace(R.id.container,new Setting_Fragment()).commit();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void UserData(String name, String photo){
        vFullNameTextView.setText(name);
        Picasso.with(getBaseContext())
                .load(photo)
                .error(R.drawable.ic_error_outline_black)
                .into(vAvatarImageView);
        Picasso.with(getBaseContext())
                .load(photo)
                .into(vBackgroundImageView);
        Picasso.with(getBaseContext())
                .load(photo)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        vBackgroundImageView.setImageBitmap(bitmap);
                        Blurry.with(MainActivity.this)
                                .radius(10)
                                .sampling(4)
                                .async()
                                .capture(vBackgroundImageView)
                                .into(vBackgroundImageView);
                    }
                    @Override public void onBitmapFailed(Drawable errorDrawable) {}
                    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
       /* vBackgroundImageView.post(new Runnable() {
            @Override
            public void run() {
                Blurry.with(MainActivity.this)
                        .radius(10)
                        .sampling(4)
                        .async()
                        .capture(vBackgroundImageView)
                        .into(vBackgroundImageView);
            }
        });*/
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            /*Blurry.with(MainActivity.this)
                    .radius(10)
                    .sampling(4)
                    .async()
                    .capture(vBackgroundImageView)
                    .into(vBackgroundImageView);*/


            VKRequest currentRequest = VKApi.users().get( VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, photo_100", VKApiConst.USER_ID, mSettings.getString("User_ID", "")));
            currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    VKList<VKApiUser> user = (VKList)response.parsedModel;
                    if(!mSettings.getString("User_name", "").equals(user.get(0).first_name + " " + user.get(0).last_name)
                            || !mSettings.getString("User_photo", "").equals(user.get(0).photo_100)) {
                        if(user.size()!=0){
                            UserData(user.get(0).first_name + " " + user.get(0).last_name, user.get(0).photo_100);
                            cachingDataUsers.StartCach(getBaseContext(), user.get(0).photo_100, vAvatarImageView);
                            SharedPreferences.Editor editor = getSharedPreferences(Constants.SETTING_NAME, MODE_PRIVATE).edit();
                            editor.putString("User_name", user.get(0).first_name + " " + user.get(0).last_name);
                            editor.putString("User_photo", user.get(0).photo_100);
                            editor.commit();
                        }
                    }}});
        }
    }
}
