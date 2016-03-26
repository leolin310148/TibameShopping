package tibame.example.shopping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CartListActivity extends AppCompatActivity {

    ListView listView;
    CartItemListAdapter cartItemListAdapter;
    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        listView= (ListView) findViewById(R.id.listView);
        cartItemListAdapter = new CartItemListAdapter();
        listView.setAdapter(cartItemListAdapter);

        firebase = new Firebase(Config.FIRE_BASE_URL);
        final String currentUserUid = getIntent().getStringExtra("userUid");

        firebase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ObjectMapper objectMapper = new ObjectMapper();

                List<Item> items = new ArrayList<Item>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                    Item item = objectMapper.convertValue(itemSnapshot.getValue(), Item.class);

                    String key = itemSnapshot.getKey();

                    if(item.getUserUid().equals(currentUserUid)){
                        Cart.removeFromCart(key);
                        continue;
                    }

                    if(Cart.getItemKeys().contains(key)){
                        item.setKey(key);
                        items.add(item);
                    }

                }

                cartItemListAdapter.setItems(items);
                cartItemListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void doCreateOrder(View view) {

        final String currentUserUid = getIntent().getStringExtra("userUid");

        for (Item item : cartItemListAdapter.getItems()) {

            Order order = new Order();
            order.setItem(item);
            order.setBuyerUserUid(currentUserUid);
            order.setStatus(Order.STATUS_PROCESSING);

            firebase.child("orders").push().setValue(order);
        }

        Cart.getItemKeys().clear();

        finish();

    }

    class CartItemListAdapter extends BaseAdapter {

        List<Item> items = new ArrayList<>();

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
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
                convertView = getLayoutInflater().inflate(R.layout.listitem_cart, null);
            }

            final Item item = (Item) getItem(position);

            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            textViewItemName.setText(item.getName());
            textViewItemPrice.setText(String.valueOf(item.getPrice()));

            Picasso.with(CartListActivity.this).load(new File(getCacheDir(),item.getKey())).into(imageView);

            Button buttonRemoveFromCart = (Button) convertView.findViewById(R.id.buttonRemoveFromCart);
            buttonRemoveFromCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cart.removeFromCart(item.getKey());
                    items.remove(item);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
