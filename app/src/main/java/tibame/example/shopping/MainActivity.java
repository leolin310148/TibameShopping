package tibame.example.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.facebook.FacebookSdk;
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
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AuthData authData;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        Firebase.setAndroidContext(getApplicationContext());

        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

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

        final Firebase firebase = new Firebase("https://tibame-0312-leo.firebaseio.com");


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Toast.makeText(MainActivity.this, "還沒登入facebook", Toast.LENGTH_SHORT).show();
                    firebase.unauth();
                } else {
                    Toast.makeText(MainActivity.this, "已經登入facebook", Toast.LENGTH_SHORT).show();

                    firebase.authWithOAuthToken("facebook", currentAccessToken.getToken(), new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            Toast.makeText(MainActivity.this, "已經登入firebase with facebook", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(MainActivity.this, "登入firebase失敗", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };


        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                MainActivity.this.authData = authData;

                ImageView imageView = (ImageView) findViewById(R.id.imageViewUserPicture);

                if (authData == null) {
                    Toast.makeText(MainActivity.this, "還沒登入firebase", Toast.LENGTH_SHORT).show();

                    imageView.setImageResource(0);
                } else {
                    Toast.makeText(MainActivity.this, "已經登入firebase", Toast.LENGTH_SHORT).show();

                    String uid = authData.getUid();
                    String imageUrl = (String) authData.getProviderData().get("profileImageURL");
                    String userName = (String) authData.getProviderData().get("displayName");


                    Picasso.with(MainActivity.this).load(imageUrl).into(imageView);

                    firebase.child("users").child(uid).child("name").setValue(userName);
                }
            }
        });


        firebase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ObjectMapper objectMapper = new ObjectMapper();

                List<Item> items = new ArrayList<Item>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                    Item item = objectMapper.convertValue(itemSnapshot.getValue(), Item.class);

                    String key = itemSnapshot.getKey();
                    File file = new File(getCacheDir(), key);
                    if (file.exists() == false) {
                        try {
                            byte[] bytes = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                            FileOutputStream fos = new FileOutputStream(file);
                            IOUtils.copy(bais, fos);
                        } catch (IOException e) {

                        }
                    }

                    item.setKey(key);
                    items.add(item);

                }


//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemNames);
                ItemListAdapter adapter = new ItemListAdapter();
                adapter.setItems(items);
                listView.setAdapter(adapter);


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                intent.putExtra("item", item);

                boolean canAddToCart = true;
                if (authData != null && Objects.equals(item.getUserUid(), authData.getUid())) {
                    canAddToCart = false;
                }

                intent.putExtra("canAddToCart", canAddToCart);

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        View cartLayout = findViewById(R.id.cartLayout);
        Set<String> itemKeys = Cart.getItemKeys();
        if (itemKeys.isEmpty()) {
            cartLayout.setVisibility(View.GONE);
        } else {
            cartLayout.setVisibility(View.VISIBLE);
            TextView textViewCartCount = (TextView) findViewById(R.id.textViewCartCount);
            textViewCartCount.setText(String.valueOf(itemKeys.size()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void goAddItem(View view) {

        if (authData == null) {
            Toast.makeText(MainActivity.this, "請先登入，才可以上架商品", Toast.LENGTH_SHORT).show();
        } else {

            Intent intent = new Intent(this, NewItemActivity.class);
            intent.putExtra("userUid", authData.getUid());
            startActivity(intent);


        }

    }

    public void goCart(View view) {

    }


    class ItemListAdapter extends BaseAdapter {

        List<Item> items;

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
                convertView = getLayoutInflater().inflate(R.layout.listitem, null);
            }

            Item item = (Item) getItem(position);

            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            ItemPresenter presenter = new ItemPresenter();
            presenter.setTextViewItemName(textViewItemName);
            presenter.setTextViewItemPrice(textViewItemPrice);
            presenter.setImageViewItemPicture(imageView);
            presenter.setItem(item);
            presenter.render(MainActivity.this);

            return convertView;
        }
    }

}
