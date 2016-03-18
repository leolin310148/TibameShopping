package tibame.example.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {


    private CallbackManager callbackManager;
    private boolean isLogin = false;
    private AccessTokenTracker accessTokenTracker;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Firebase firebase = new Firebase("https://tibame-0312-leo.firebaseio.com/");

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "登入失敗", Toast.LENGTH_SHORT).show();
            }
        });

        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                ImageView imageViewUserPicture = (ImageView) findViewById(R.id.imageViewUserPicture);

                if (authData != null) {
                    isLogin = true;

                    String userName = (String) authData.getProviderData().get("displayName");
                    String imageUrl = (String) authData.getProviderData().get("profileImageURL");

                    Picasso.with(MainActivity.this).load(imageUrl).into(imageViewUserPicture);

                    String uid = authData.getUid();
                    firebase.child("users").child(uid).child("name").setValue(userName);

                    userUid = uid;
                } else {
                    isLogin = false;
                    imageViewUserPicture.setImageResource(0);
                }
                System.out.println("authData=" + authData);
                System.out.println("isLogin=" + isLogin);
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                System.out.println("currentAccessToken=" + currentAccessToken);
                if (currentAccessToken == null) {
                    firebase.unauth();
                } else {
                    firebase.authWithOAuthToken("facebook", currentAccessToken.getToken(), new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Toast.makeText(MainActivity.this, "已經成功連結facebook到firebase", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(MainActivity.this, "連結facebook到firebase失敗", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    public void goAddItem(View view) {
        if (isLogin) {
            Intent intent = new Intent(this, NewItemActivity.class);
            intent.putExtra("userUid", userUid);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "請先登入，才可以上架商品。", Toast.LENGTH_SHORT).show();
        }
    }
}
