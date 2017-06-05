package activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kirill.vkmessager.R;
import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import adapters.UserDialogRecyclerAdapter;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import support.Constants;

/**
 * Created by Kirill on 03.02.2017.
 */

public class UserDialog extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ImageView sendMSGBut, backButtonImageView, vEmojiImageview;
    private EmojiconEditText sendMSGEditText;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private int mCountMSG;
    private EmojIconActions emojIcon;
    private TextView onlineTextView, data;
    private View rootView;
    private ProgressBar sendMSGProgresar;
    private Toast toast;
    private String photo_100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dialog);
        toast = Toast.makeText(this, "Ошибка! Сообщение не отправленно.", Toast.LENGTH_SHORT);
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 5000, 3000);

        VKRequest friendRequest = VKApi.users().get( VKParameters.from(VKApiConst.FIELDS, "online, first_name, last_name, photo_100", VKApiConst.USER_ID, Constants.ID));
        friendRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                ImageView avatar = (ImageView) findViewById(R.id.avatar_friend_imageView);
                TextView nameTxt = (TextView)findViewById(R.id.name_friend_textView);
                onlineTextView = (TextView)findViewById(R.id.online_textView);
                super.onComplete(response);
                VKList<VKApiUser> user = (VKList)response.parsedModel;
                if(user.get(0).online)onlineTextView.setText("онлайн");else onlineTextView.setText("офлайн");//обновляется один раз за вход!
                nameTxt.setText(user.get(0).first_name + " " + user.get(0).last_name);
                photo_100 = user.get(0).photo_100;
                Picasso.with(getBaseContext())
                        .load(user.get(0).photo_100)
                        .error(R.drawable.ic_error_outline_black)
                        .into(avatar);
            }}
        );

        backButtonImageView = (ImageView)findViewById(R.id.back_button_imageView_user_dialog);
        backButtonImageView.setOnClickListener(this);

        Zapros();
        sendMSGBut = (ImageView)findViewById(R.id.user_dialog_sendMSG);
        sendMSGBut.setOnClickListener(this);
        sendMSGEditText = (EmojiconEditText)findViewById(R.id.user_dialog_editText);

        vEmojiImageview = (ImageView)findViewById(R.id.user_dialog_smail_stikers);
        rootView = findViewById(R.id.rootview);
        sendMSGProgresar = (ProgressBar)findViewById(R.id.sendMSG_progressBar);
        emojIcon = new EmojIconActions(this, rootView, sendMSGEditText, vEmojiImageview);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }
            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });
        }


    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.user_dialog_sendMSG:
                sendMSGBut.setVisibility(View.INVISIBLE);
                sendMSGProgresar.setVisibility(View.VISIBLE);
                final VKRequest sendMSG = new VKRequest("messages.send",VKParameters.from(VKApiConst.USER_ID, Constants.ID, VKApiConst.MESSAGE, sendMSGEditText.getText()));
                sendMSG.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        sendMSGBut.setVisibility(View.VISIBLE);
                        super.onComplete(response);
                        Zapros();
                        sendMSGEditText.setText("");
                        sendMSGProgresar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(VKError error) {
                        sendMSGBut.setVisibility(View.VISIBLE);
                        sendMSGProgresar.setVisibility(View.GONE);
                        String s = sendMSGEditText.getText().toString();
                        if(sendMSGEditText.getText().toString().equals(""));
                        else toast.show();
                        super.onError(error);
                    }});
                break;
            case R.id.back_button_imageView_user_dialog:
                finish();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
        }
        return false;
    }
    public void Zapros(){
            VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, Constants.ID));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject metaJson = null;
                try {
                    metaJson = response.json.getJSONObject("response");
                    mCountMSG = metaJson.getInt("count");
                    JSONArray itemsJson = metaJson.getJSONArray("items");

                    mRecyclerView = (RecyclerView) findViewById(R.id.user_dialog_recycleView);
                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new LinearLayoutManager(getBaseContext());
                    mLayoutManager.setReverseLayout(true);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mAdapter = new UserDialogRecyclerAdapter(getBaseContext(), itemsJson, photo_100);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, Constants.ID, "count", "0"));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                JSONObject metaJson = response.json.getJSONObject("response");
                                if (mCountMSG != metaJson.getInt("count"))Zapros();
                            } catch (JSONException e) {e.printStackTrace();}
                        }
                    });
                }
            });
        }
    }
}
