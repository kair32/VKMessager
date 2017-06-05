package adapters;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import activity.UserDialog;
import support.DB.DbDialogs;
import support.ParseData;
import utilis.CachingDataUsers;
import utilis.Constants;

/**
 * Created by Kirill on 02.02.2017.
 */

public class DialogsResyclerAdapter extends RecyclerView.Adapter<DialogsResyclerAdapter.ViewHolder> {
    private String mVkTS = null;
    private VKUsersArray usersArray;
    private int count;
    private Context mContext;
    private JSONArray itemsJson;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mMSGTextView, mNameTextView, vLastSendMsgTextView;
        public ImageView vAvatarImageView, vNotReadImageView;
        public LinearLayout vLinerLayout;
        public String mUserID;
        public ViewHolder(View v) {
            super(v);
            vNotReadImageView = (ImageView) v.findViewById(R.id.not_read_circleImageView);
            mMSGTextView = (TextView) v.findViewById(R.id.dialog_lastMSG_textView);
            mNameTextView = (TextView) v.findViewById(R.id.dialog_fullname_TextView);
            vAvatarImageView = (ImageView) v.findViewById(R.id.dialogImageView);
            vLinerLayout = (LinearLayout)v.findViewById(R.id.dialogs_fragment_linerlayout);
            vLastSendMsgTextView = (TextView)v.findViewById(R.id.dialog_data_TextView);
        }
    }
    public DialogsResyclerAdapter(Context context, JSONArray istemsJson, VKUsersArray users, int counts) {
        itemsJson = istemsJson;
        usersArray = users;
        mContext = context;
        count = counts;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_fragment_dialogs, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 5000, 1000);

        try {
            JSONObject postJson = itemsJson.getJSONObject(position);
            JSONObject finalpostJson = postJson.getJSONObject("message");
            try {
                if(postJson.getString("unread")!=null)holder.vLinerLayout.setBackgroundColor(mContext.getColor(R.color.colorNotRead));
            }catch (JSONException e){if(finalpostJson.getString("read_state").equals("0"))holder.vNotReadImageView.setVisibility(View.VISIBLE);}
            holder.mUserID = finalpostJson.getString("user_id");
            String body = finalpostJson.getString("body");//тело сообщения
            try {
                JSONArray attachments = finalpostJson.getJSONArray("attachments");
                String attachmentsType = attachments.getJSONObject(0).getString("type");
                if(attachmentsType.equals("photo"))body = "Фотография";
                else body = "Стикер";
            }catch (JSONException e) {
                try {
                    JSONArray attachments = finalpostJson.getJSONArray("fwd_messages");
                    body = "Пересланное сообщение";
                }catch (JSONException ignored){}
            }
            holder.mMSGTextView.setText(body);
//дата
            ParseData datapars = new ParseData();
            holder.vLastSendMsgTextView.setText(datapars.ParseData(finalpostJson.getString("date")));
            String dat = datapars.ParseData(finalpostJson.getString("date"));
//
            final CachingDataUsers cachingDataUsers = new CachingDataUsers();
            VKUsersArray userArray = cachingDataUsers.CheckFileText(mContext);
            boolean download = true;
            final String finalBody = body;
            for (int i=0;i<userArray.getCount();i++){
                if(holder.mUserID.equals(Integer.toString(userArray.get(i).id))){
                    download = cachingDataUsers.CheckFilePhoto(mContext,userArray.get(i).photo_100,holder.vAvatarImageView);
                    holder.mNameTextView.setText(userArray.get(i).last_name);
                    cachingDataUsers.StartCashDialog(userArray.get(i).last_name,userArray.get(i).photo_100, finalBody,userArray.get(0).id,mContext);
                    Log.d("TAP", " Я КРАСАВЧИК Я НЕ БУДУ СКАЧИВАТЬ УРАА Я ЭКОНОМЛЮ ТРАФИК!!!!!");}
            }
            if (download){
                Log.d("TAP", " Я СКАЧАЛ!!!!");
                VKRequest currentRequest = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "online, first_name, last_name, photo_100", VKApiConst.USER_ID, holder.mUserID));
                currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKList<VKApiUser> user = (VKList)response.parsedModel;
                        cachingDataUsers.StartCashDialog(user.get(0).first_name+ " " + user.get(0).last_name,user.get(0).photo_100, finalBody,user.get(0).id,mContext);
                        holder.mNameTextView.setText(user.get(0).first_name + " " + user.get(0).last_name);
                        Picasso.with(mContext)
                                .load(user.get(0).photo_100)
                                .into(cachingDataUsers.StartCach(mContext, user.get(0).photo_100, holder.vAvatarImageView));
                        cachingDataUsers.StartCachText(user.get(0).first_name + user.get(0).last_name, user.get(0).photo_100, user.get(0).id, mContext);
                }
            });}
            try {
                String read_state = finalpostJson.getString("read_state");//не прочитанно оппонентом
            } catch (JSONException e) {}

            /*DbDialogs dbDialogs = new DbDialogs(mContext);
            SQLiteDatabase db = dbDialogs.getWritableDatabase();*/
            //cachingDataUsers.StartCashDialog(holder.mNameTextView.getText(),);
        }catch (JSONException e){

        }
        holder.vLinerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.ID = Integer.parseInt(holder.mUserID);
                Intent intent = new Intent(v.getContext(), UserDialog.class);
                v.getContext().startActivity(intent);
            }
        });
        if(position == count-1){
            VKRequest request = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.OFFSET, count-1, VKApiConst.COUNT, 21, "preview_length", 25));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    JSONObject metaJson = null;
                    try {
                        metaJson = response.json.getJSONObject("response");
                        JSONArray sourceArray =  metaJson.getJSONArray("items");
                        for (int i = 1; i < sourceArray.length(); i++) {
                            itemsJson.put(sourceArray.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            count+=20;
            notifyDataSetChanged();
            // вызываем метод догрузки новых айтемов или просто добавляем новые откуда-то
            // после подгрузки новых элементов не забываем вызвать notifyDataSetChanged()
        }
    }

    public void Zapros(){
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
                    if(jsonObject1.getString("count")!=null){
                        JSONArray jsonObject2 = jsonObject1.getJSONArray("items");
                        JSONObject jsonObject3 = jsonObject2.getJSONObject(0);
                        Log.d("messages", "Start");
                        VKRequest currentRequest = VKApi.messages().getDialogs(VKParameters.from( VKApiConst.COUNT, 20, "preview_length", 25));
                        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                try {
                                    Log.d("messages", "GOOD");
                                    JSONObject metaJson = response.json.getJSONObject("response");
                                    itemsJson = metaJson.getJSONArray("items");
                                    notifyDataSetChanged();
                                }
                                catch (JSONException e){}
                                Log.d("messages", "OOPS");
                                }

                            @Override
                            public void onError(VKError error) {
                                Log.d("messages", "Eror");
                                super.onError(error);
                            }
                        });
                        }
                } catch (JSONException e) {e.printStackTrace();}
            }
        });
    }

    @Override
    public int getItemCount() {
        return count;
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
                        String VKTS = json.getString("ts");
                        if(VKTS.equals(mVkTS));
                        else {if(mVkTS==null) mVkTS=VKTS;
                            Zapros();
                            mVkTS=VKTS;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}