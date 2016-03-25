package tibame.example.shopping;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * @author leo
 */
public class ItemPresenter {
    private TextView textViewItemName;
    private TextView textViewItemPrice;
    private ImageView imageViewItemPicture;
    private Item item;

    public void setTextViewItemName(TextView textViewItemName) {
        this.textViewItemName = textViewItemName;
    }

    public void setTextViewItemPrice(TextView textViewItemPrice) {
        this.textViewItemPrice = textViewItemPrice;
    }

    public void setImageViewItemPicture(ImageView imageViewItemPicture) {
        this.imageViewItemPicture = imageViewItemPicture;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void render(Context context) {
        textViewItemName.setText(item.getName());
        textViewItemPrice.setText(String.valueOf(item.getPrice()));
        Picasso.with(context).load(new File(context.getCacheDir(), item.getKey())).into(imageViewItemPicture);
    }
}
