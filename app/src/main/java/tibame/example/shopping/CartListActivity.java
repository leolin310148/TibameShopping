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
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CartListActivity extends AppCompatActivity {

    ListView listView;
    CartListAdapter cartListAdapter = new CartListAdapter();
    DataSnapshot dataSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(cartListAdapter);

        final Firebase firebase = new Firebase(Config.FIRE_BASE_URL);
        firebase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CartListActivity.this.dataSnapshot = dataSnapshot;
                renderItems();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void renderItems() {
        ObjectMapper objectMapper = new ObjectMapper();
        Set<String> itemKeys = Cart.getItemKeys();
        List<Item> items = new ArrayList<>();
        String userUid = getIntent().getStringExtra("userUid");

        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
            if (itemKeys.contains(itemSnapshot.getKey())) {

                Item item = objectMapper.convertValue(itemSnapshot.getValue(), Item.class);

                if (Objects.equals(item.getUserUid(), userUid)) {
                    Cart.removeFromCart(itemSnapshot.getKey());
                    Toast.makeText(CartListActivity.this, "您是商品:" + item.getName() + "的賣家，將從購物車移除此商品。", Toast.LENGTH_SHORT).show();
                    continue;
                }

                item.setKey(itemSnapshot.getKey());
                items.add(item);
            }

        }
        cartListAdapter.setItems(items);
        cartListAdapter.notifyDataSetChanged();
    }

    class CartListAdapter extends BaseAdapter {
        private List<Item> items = new ArrayList<>();

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
                convertView = getLayoutInflater().inflate(R.layout.listitem_cart, null);
            }

            final Item item = (Item) getItem(position);

            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            ItemPresenter presenter = new ItemPresenter();
            presenter.setTextViewItemName(textViewItemName);
            presenter.setTextViewItemPrice(textViewItemPrice);
            presenter.setImageViewItemPicture(imageView);
            presenter.setItem(item);
            presenter.render(CartListActivity.this);

            Button buttonRemoveFromCart = (Button) convertView.findViewById(R.id.buttonRemoveFromCart);
            buttonRemoveFromCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cart.removeFromCart(item.getKey());
                    renderItems();
                }
            });


            return convertView;
        }
    }
}
