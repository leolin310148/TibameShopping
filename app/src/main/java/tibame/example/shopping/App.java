package tibame.example.shopping;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

/**
 * @author leo
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
        Firebase.setAndroidContext(this);
    }
}
