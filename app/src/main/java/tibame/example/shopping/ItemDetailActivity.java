package tibame.example.shopping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        final Item item = (Item) getIntent().getSerializableExtra("item");

        TextView textViewItemName = (TextView) findViewById(R.id.textViewItemName);
        TextView textViewItemPrice = (TextView) findViewById(R.id.textViewItemPrice);
        ImageView imageView = (ImageView) findViewById(R.id.imageViewItemPicture);

        textViewItemName.setText(item.getName());
        textViewItemPrice.setText(String.valueOf(item.getPrice()));

        Picasso.with(ItemDetailActivity.this).load(new File(getCacheDir(),item.getKey())).into(imageView);

        Button buttonAddToCart = (Button) findViewById(R.id.buttonAddToCart);

        boolean canAddToCart = getIntent().getBooleanExtra("canAddToCart", true);
        if(canAddToCart == false){
            buttonAddToCart.setVisibility(View.GONE);
        }else {
            buttonAddToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cart.addToCart(item.getKey());
                    finish();
                }
            });
        }
    }
}
