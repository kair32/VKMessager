package fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kirill.vkmessager.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import adapters.ContactsRecyclerAdapter;
import adapters.DialogsResyclerAdapter;
import utilis.CachingDataUsers;

/**
 * Created by Kirill on 01.02.2017.
 */

public class Dialogs_Fragment extends Fragment {
    private View vRootView;
    private int DY = 0, DYnext = 0;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vRootView = inflater.inflate(R.layout.fragment_dialogs, container, false);
        mRecyclerView = (RecyclerView) vRootView.findViewById(R.id.dialogRecyclerView);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Json();
        return vRootView;
    }
    public void Json(){
        CachingDataUsers cachingDataUsers = new CachingDataUsers();
        final VKUsersArray usersArray = new VKUsersArray();// cachingDataUsers.CheckDialogs(getContext());
        if(usersArray.size()==0) {
            VKRequest currentRequest = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.COUNT, 20, "preview_length", 25));
            currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject metaJson = response.json.getJSONObject("response");
                        JSONArray itemsJson = metaJson.getJSONArray("items");
                        mAdapter = new DialogsResyclerAdapter(getContext(), itemsJson, null, itemsJson.length());
                        mRecyclerView.setAdapter(mAdapter);
                    } catch (JSONException e) {
                    }
                }
            });
        }
        else{ mAdapter = new DialogsResyclerAdapter(getContext(), null, usersArray, usersArray.getCount());
            mRecyclerView.setAdapter(mAdapter);}
    }
}
