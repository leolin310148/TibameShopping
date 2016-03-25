package tibame.example.shopping;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        final Firebase firebase = new Firebase(Config.FIRE_BASE_URL);

        final Item item = (Item) getIntent().getSerializableExtra("item");

        ImageView imageViewItemPicture = (ImageView) findViewById(R.id.imageViewItemPicture);
        TextView textViewItemName = (TextView) findViewById(R.id.textViewItemName);
        TextView textViewItemPrice = (TextView) findViewById(R.id.textViewItemPrice);
        final TextView textViewItemSeller = (TextView) findViewById(R.id.textViewItemSeller);
        Button buttonAddToCart = (Button) findViewById(R.id.buttonAddToCart);

        ItemPresenter presenter = new ItemPresenter();
        presenter.setTextViewItemName(textViewItemName);
        presenter.setTextViewItemPrice(textViewItemPrice);
        presenter.setImageViewItemPicture(imageViewItemPicture);
        presenter.setItem(item);
        presenter.render(this);

        firebase.child("users").child(item.getUserUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = (String) dataSnapshot.getValue();
                textViewItemSeller.setText(userName);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        final boolean canAddToCart = getIntent().getBooleanExtra("canAddToCart", false);
        if (canAddToCart == false) {
            buttonAddToCart.setVisibility(View.GONE);
        } else {
            buttonAddToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cart.addToCart(item.getKey());
                    Toast.makeText(ItemDetailActivity.this, "已將商品加入購物車。", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        }

    }
}
