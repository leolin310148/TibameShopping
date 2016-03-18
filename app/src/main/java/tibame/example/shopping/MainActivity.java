package tibame.example.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private CallbackManager callbackManager;
    private boolean isLogin = false;
    private AccessTokenTracker accessTokenTracker;
    private String userUid;
    private ListView listView;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

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

        firebase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Item> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                    Item item = objectMapper.convertValue(itemSnapshot.getValue(), Item.class);

                    String key = itemSnapshot.getKey();
                    item.setKey(key);

                    items.add(item);

                    File file = new File(getCacheDir(), key);
                    if (!file.exists()) {
                        byte[] bytes = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                        try {
                            IOUtils.copy(new ByteArrayInputStream(bytes), new FileOutputStream(file));
                        } catch (IOException e) {
                        }
                    }
                }

                ItemListAdapter adapter = new ItemListAdapter();
                adapter.setItems(items);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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

    class ItemListAdapter extends BaseAdapter {

        private List<Item> items;

        public void setItems(List<Item> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem_item, null);
            }

            Item item = (Item) getItem(position);

            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageViewItemPicture = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            textViewItemName.setText(item.getName());
            textViewItemPrice.setText(String.valueOf(item.getPrice()));
            Picasso.with(MainActivity.this).load(new File(getCacheDir(),item.getKey())).into(imageViewItemPicture);


            return convertView;
        }


    }
}
