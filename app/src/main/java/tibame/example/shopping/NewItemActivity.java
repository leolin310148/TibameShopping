package tibame.example.shopping;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class NewItemActivity extends AppCompatActivity {

    private ImageView imageViewItemPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        imageViewItemPicture = (ImageView) findViewById(R.id.imageViewItemPicture);
    }

    public void choosePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Uri uri = data.getData();
            Picasso.with(this).load(uri).into(imageViewItemPicture);
        }
    }

}
