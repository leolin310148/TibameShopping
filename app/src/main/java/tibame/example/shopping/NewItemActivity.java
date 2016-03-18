package tibame.example.shopping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class NewItemActivity extends AppCompatActivity {

    private ImageView imageViewItemPicture;
    private EditText editTextItemName;
    private EditText editTextItemPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        imageViewItemPicture = (ImageView) findViewById(R.id.imageViewItemPicture);
        editTextItemName = (EditText) findViewById(R.id.editTextItemName);
        editTextItemPrice = (EditText) findViewById(R.id.editTextItemPrice);
    }

    public void choosePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1002);
    }

    public void doAddItem(View view) {

        final Firebase firebase = new Firebase("https://tibame-0312-leo.firebaseio.com/");

        String name = editTextItemName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "請輸入商品名稱", Toast.LENGTH_SHORT).show();
            return;
        }

        String priceString = editTextItemPrice.getText().toString();
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "請輸入商品價格", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceString);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewItemPicture.getDrawable();
        if (bitmapDrawable == null) {
            Toast.makeText(this, "請提供商品照片", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        String itemPictureBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

        String userUid = getIntent().getStringExtra("userUid");

        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setImageBase64(itemPictureBase64);
        item.setUserUid(userUid);

        firebase.child("items").push().setValue(item);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (data != null) {
                Uri uri = data.getData();
                Picasso.with(this).load(uri).into(imageViewItemPicture);
            }
        } else if (requestCode == 1002) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                imageViewItemPicture.setImageBitmap(bitmap);
            }
        }
    }

}
