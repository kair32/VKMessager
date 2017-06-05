package activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.kirill.vkmessager.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import support.Constants;

import static support.Constants.isResumed;
import static support.Constants.mScore;

/**
 * Created by Kirill on 31.01.2017.
 */

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        findViewById(R.id.sign_in_buttson).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResumed=true;
                VKSdk.login(SignInActivity.this, mScore);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.SETTING_NAME, MODE_PRIVATE).edit();
                    editor.putString("User_ID", res.userId);
                    editor.commit();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    // Пользователь успешно авторизовался
                }
                @Override
                public void onError(VKError error) {
                    Log.d("TAP", " false");
                    // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                }
            })) {
                super.onActivityResult(requestCode, resultCode, data);
                Log.d("TAP", " dont know");
            }
    }
}
