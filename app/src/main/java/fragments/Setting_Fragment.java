package fragments;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.kirill.vkmessager.R;
import com.vk.sdk.VKSdk;

import java.io.File;

import activity.SignInActivity;
import support.DB.DbHelper;

/**
 * Created by Kirill on 07.02.2017.
 */

public class Setting_Fragment extends Fragment implements View.OnClickListener {
    private View vRootView;
    private Button out, keshTXT, keshPhoto;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vRootView = inflater.inflate(R.layout.fragment_setting, container, false);
        out = (Button)vRootView.findViewById(R.id.button);
        keshTXT = (Button)vRootView.findViewById(R.id.button3);
        keshPhoto = (Button)vRootView.findViewById(R.id.button2);
        keshPhoto.setOnClickListener(this);
        keshTXT.setOnClickListener(this);
        out.setOnClickListener(this);
        return vRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                VKSdk.logout();
                if (!VKSdk.isLoggedIn()) {
                    startActivity(new Intent(getContext(), SignInActivity.class));
                }
                break;
            case R.id.button3:
                DbHelper mDbHelper = new DbHelper(getContext());
                // Gets the database in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                //db.execSQL("delete from "+ DbHelper.LOG_TAG);
                db.execSQL("delete from "+ "guests");
                Log.d("TAP", " COMPLEATED");
                break;
            case R.id.button2:
                ContextWrapper cw = new ContextWrapper(getContext());
                File file = cw.getDir("photo", Context.MODE_PRIVATE);
                cw.deleteDatabase("photo");
                File[] files = file.listFiles();
                for (int i =0; i < files.length; i++) {
                        boolean fildel = new File(files[i].getAbsolutePath()).delete();//удаляет содержимое папки
                    Log.d("TAP", " COMPLEATED");
                    }
                break;
        }
    }
}
