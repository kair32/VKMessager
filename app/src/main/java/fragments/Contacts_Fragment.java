package fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.example.kirill.vkmessager.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import adapters.ContactsRecyclerAdapter;
import adapters.ContactsRecyclerAdapterOnline;
import support.CachingDataUsers;

/**
 * Created by Kirill on 31.01.2017.
 */

public class Contacts_Fragment extends Fragment implements View.OnClickListener{
    private RecyclerView mRecyclerView, mRecyclerView2;
    private RecyclerView.Adapter mAdapter, mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder> ContactsResylViewAdapter;
    private View vRootView;
    private static final int REQUEST_LOGIN = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vRootView = inflater.inflate(R.layout.fragment_contacts_, container, false);

        CachingDataUsers cachingDataUsers = new CachingDataUsers();
        final VKUsersArray usersArray = cachingDataUsers.CheckFileText(getContext());
        mRecyclerView = (RecyclerView) vRootView.findViewById(R.id.contactsRecyclerView);
        getActivity().setTitle("Обновление...");
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        if(usersArray.size()==0){
            VKRequest request = VKApi.friends().get(VKParameters.from("order", "hints", VKApiConst.FIELDS, "id,onine,photo_100"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                    mAdapter = new ContactsRecyclerAdapter(usersArray, getContext());
                    mRecyclerView.setAdapter(mAdapter);
                }});
        }
        else{ mAdapter = new ContactsRecyclerAdapter(usersArray, getContext());
        mRecyclerView.setAdapter(mAdapter);}

        final VKUsersArray usersArray2 = new VKUsersArray();
        mRecyclerView2 = (RecyclerView) vRootView.findViewById(R.id.contactsOnlineRecyclerView);
        mRecyclerView2.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView2.setLayoutManager(mLayoutManager);
            final VKRequest request = VKApi.friends().get(VKParameters.from("order", "hints", VKApiConst.FIELDS, "id,onine,photo_100"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    VKUsersArray usersArrayPars = (VKUsersArray) response.parsedModel;
                    for (int i = 0; i < usersArrayPars.getCount(); i++){
                        VKApiUserFull full = new VKApiUserFull();
                        full.photo_100 = usersArrayPars.get(i).photo_100;
                        full.last_name = usersArrayPars.get(i).last_name;
                        full.first_name = usersArrayPars.get(i).first_name;
                        full.id = usersArrayPars.get(i).id;
                        full.online = usersArrayPars.get(i).online;
                        if(usersArrayPars.get(i).online)
                            usersArray2.add(full);
                    }
                    mAdapter2 = new ContactsRecyclerAdapterOnline(usersArray2, getContext());
                    mRecyclerView2.setAdapter(mAdapter2);
                }});
        mRecyclerView2.setAdapter(mAdapter2);

        TabHost tabHost = (TabHost) vRootView.findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setContent(R.id.contactsRecyclerView);
        tabSpec.setIndicator("ВСЕ");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.contactsOnlineRecyclerView);
        tabSpec.setIndicator("ОНЛАЙН");
        tabHost.addTab(tabSpec);

        return vRootView;
    }

    @Override
    public void onClick(View v) {
        //int i= (int) v.getTag();
        //Log.d("TAPs", "s"  + v.getTag());
    }
}
