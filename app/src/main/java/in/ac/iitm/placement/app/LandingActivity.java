package in.ac.iitm.placement.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by arun on 05-Aug-15.
 */
public class LandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.getprefBool("logedin",this)){
            Intent downloadIntent;
            downloadIntent = new Intent(this, MainActivity.class);
            startActivity(downloadIntent);
            finish();
        }
        else {
            Intent downloadIntent;
            downloadIntent = new Intent(this, LoginActivity.class);
            startActivity(downloadIntent);
            finish();
        }
    }
}
