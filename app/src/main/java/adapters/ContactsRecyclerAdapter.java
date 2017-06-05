package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.squareup.picasso.Target;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import activity.MainActivity;
import activity.UserDialog;
import support.DB.DbHelper;
import utilis.CachingDataUsers;
import utilis.Constants;

/**
 * Created by Kirill on 01.02.2017.
 */

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder> {


    private VKUsersArray vkArray;
    private static Context mContext;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private boolean flag;
    private CachingDataUsers cachingDataUsers;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageView,mImageViewOnline;
        public LinearLayout vLinerLayout;
            public ViewHolder(View v) {
                super(v);
                mImageViewOnline = (ImageView) v.findViewById(R.id.online_circleImageView);
                mTextView = (TextView) v.findViewById(R.id.contactTextView);
                mImageView = (ImageView) v.findViewById(R.id.contactImageView);
                vLinerLayout = (LinearLayout)v.findViewById(R.id.contacts_fragment_LinerLayout);
            }
    }
    public ContactsRecyclerAdapter(VKUsersArray vkarray, Context context) {
        vkArray = vkarray;
        mContext = context;
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1000 , 25000);
        cachingDataUsers = new CachingDataUsers();
    }
    @Override
    public ContactsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_fragment_contacts, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(vkArray.get(position).first_name + " " + vkArray.get(position).last_name);
        boolean download = cachingDataUsers.CheckFilePhoto(mContext,vkArray.get(position).photo_100,holder.mImageView);

        if(vkArray.get(position).online)holder.mImageViewOnline.setVisibility(View.VISIBLE);else holder.mImageViewOnline.setVisibility(View.INVISIBLE);
        //holder.mTextView.setText(holder.mTextView.getText() + " " + "*");
        if(download) {
            Log.d("TAP", " Я СКАЧАЛ!!!!");
            //cachingDataUsers.StartCachText(vkArray.get(position).first_name + " " + vkArray.get(position).last_name, vkArray.get(position).photo_100, vkArray.get(position).id, mContext);
            Picasso.with(mContext)
                    .load(vkArray.get(position).photo_100)
                    .into(cachingDataUsers.StartCach(mContext, vkArray.get(position).photo_100, holder.mImageView));
        }
        else  Log.d("TAP", " Я КРАСАВЧИК Я НЕ БУДУ СКАЧИВАТЬ УРАА Я ЭКОНОМЛЮ ТРАФИК!!!!!");
        holder.vLinerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.ID = vkArray.get(position).id;
                Intent intent = new Intent(v.getContext(), UserDialog.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vkArray.getCount();
    }
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            VKRequest request = VKApi.friends().get(VKParameters.from("order", "hints", VKApiConst.FIELDS, "id,onine,photo_100"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                        for (int i=0; i< usersArray.getCount();i++) {
                            if(!usersArray.get(i).photo_100.equals(vkArray.get(i).photo_100) || usersArray.get(i).id != vkArray.get(i).id || cachingDataUsers.CheckFileText(mContext).getCount()==0) {
                                VKApiUserFull full = new VKApiUserFull();
                                full.photo_100 = usersArray.get(i).photo_100;
                                full.last_name = usersArray.get(i).last_name;
                                full.first_name = usersArray.get(i).first_name;
                                full.id = usersArray.get(i).id;
                                full.online = usersArray.get(i).online;
                                vkArray.add(i,full);
                                flag = true;
                                notifyDataSetChanged();
                            }
                            if(vkArray.get(i).online!=usersArray.get(i).online && vkArray.get(i).id==usersArray.get(i).id) {
                                vkArray.get(i).online = usersArray.get(i).online;
                                Activity test = (Activity) mContext;
                                test.setTitle("Контакты");
                                notifyDataSetChanged();}
                        }
                    if(flag){
                        DbHelper mDbHelper = new DbHelper(mContext);
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        db.execSQL("delete from "+ "guests");
                        for (int i =0; i< vkArray.getCount();i++){
                                cachingDataUsers.StartCachText(vkArray.get(i).first_name + " " + vkArray.get(i).last_name, vkArray.get(i).photo_100, vkArray.get(i).id, mContext);
                        }
                    }
                }
            });
        }
    }
}