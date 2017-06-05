package adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kirill.vkmessager.R;
import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import support.Constants;

/**
 * Created by Kirill on 06.02.2017.
 */

public class UserDialogRecyclerAdapter extends RecyclerView.Adapter<UserDialogRecyclerAdapter.ViewHolder> {
    private int count;
    private JSONArray messJson;
    private Context mContext;
    private String photo;
    private final static int TYPE_IN = 42;
    private final static int TYPE_OUT = 43;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView vBodyMSG;
        public ImageView vBodyImageView;
        public LinearLayout vLinerLayout;
        public CardView vCardView;
        public ViewHolder(View v) {
            super(v);
            vBodyMSG = (TextView) v.findViewById(R.id.user_dialog_textView);
            vBodyImageView = (ImageView) v.findViewById(R.id.user_dialog_imageView);
            vLinerLayout = (LinearLayout)v.findViewById(R.id.user_dialog_LinerLayout);
            vCardView = (CardView)v.findViewById(R.id.user_dialog_card_view);
        }
    }
    public UserDialogRecyclerAdapter(Context context, JSONArray istemsJson, String photo) {
        this.messJson = istemsJson;
        this.mContext = context;
        this.photo = photo;
        this.count = messJson.length();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType == TYPE_IN ? R.layout.adapter_dialog_user_right : R.layout.adapter_dialog_user_left, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return messJson.getJSONObject(position).getString("out").equals("1") ? TYPE_IN : TYPE_OUT;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            JSONObject postJson = messJson.getJSONObject(position);
            holder.vBodyMSG.setText(postJson.getString("body"));
            try {
                JSONArray fwd_msg = postJson.getJSONArray("fwd_messages");
                JSONObject fwd_mesage = fwd_msg.getJSONObject(0);
                holder.vBodyMSG.setText(holder.vBodyMSG.getText() + "\n\r" + fwd_mesage.getString("body"));
            }catch (JSONException s){
            }
            try {
                JSONArray photoArray = postJson.getJSONArray("attachments");
                JSONObject photoArray2 = photoArray.getJSONObject(0);
                try {
                    JSONObject photoArray3 = photoArray2.getJSONObject("photo");
                    holder.vBodyImageView.setVisibility(View.VISIBLE);
                    String photo = null;
                    try {
                        photo = photoArray3.getString("photo_1280");
                    }catch (JSONException a) {
                        try {
                            photo = photoArray3.getString("photo_604");
                        } catch (JSONException e) {
                            try {
                                photo = photoArray3.getString("photo_130");
                            } catch (JSONException f) {
                            }
                        }
                    }
                    Picasso.with(mContext)
                            .load(photo)
                            .error(R.drawable.ic_error_outline_black)
                            .into(holder.vBodyImageView);
                }catch (JSONException e ){
                    try {
                        JSONObject photoArray3 = photoArray2.getJSONObject("sticker");
                        holder.vBodyImageView.setVisibility(View.VISIBLE);
                        Picasso.with(mContext)
                                .load(photoArray3.getString("photo_512"))
                                .error(R.drawable.ic_error_outline_black)
                                .into(holder.vBodyImageView);
                    }catch (JSONException es){}
                }
            }catch (JSONException e){}
            holder.vBodyMSG.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", holder.vBodyMSG.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "Скопированно в буфер обменв" ,Toast.LENGTH_SHORT ).show();
                    return false;
                }
            });
        }catch (JSONException e){}
        if(position == count-1){
            String id = "0";
            try {
                JSONObject postJson = messJson.getJSONObject(count - 1);
                id = postJson.getString("id");
            }catch (JSONException e){}
            VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, Constants.ID,"count", "21", "offset", 0, "start_message_id" , id));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                                            @Override
                                            public void onComplete(VKResponse response) {
                                                super.onComplete(response);
                                                JSONObject metaJson = null;
                                                try {
                                                    metaJson = response.json.getJSONObject("response");
                                                    JSONArray sourceArray =  metaJson.getJSONArray("items");
                                                    for (int i = 1; i < sourceArray.length(); i++) {
                                                        messJson.put(sourceArray.getJSONObject(i));
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
            count+=20;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }
}