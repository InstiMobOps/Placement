package in.ac.iitm.placement.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    ProgressDialog progress;
    Button signin;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;
    //RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getSupportActionBar().hide();
        signin = (Button) findViewById(R.id.button);
       // container =(RelativeLayout) findViewById(R.id.container);
        // Button revoke =(Button) findViewById(R.id.revoke);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    progress = new ProgressDialog(LoginActivity.this);
                    progress.setCancelable(false);
                    progress.setMessage("Logging In...");
                    progress.show();
                    signin.setEnabled(false);
                    mGoogleApiClient.connect();
                    signin.setEnabled(false);

                }

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                    Log.d("hete", "on failed");
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.

        signin.setEnabled(true);
        mSignInClicked = false;

        String personName = "", personId = "",personEmail="",personRollno="";

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            personId = currentPerson.getId();
            personEmail=Plus.AccountApi.getAccountName(mGoogleApiClient);
        }
        if (personEmail.endsWith("smail.iitm.ac.in")){
            Utils.saveprefString("rollno", personEmail.substring(0,8).toLowerCase(), getBaseContext());
            Utils.saveprefString("personname", personName, getBaseContext());
            Utils.saveprefString("personemail", personEmail.toLowerCase(), getBaseContext());
            Utils.saveprefString("department", personEmail.substring(0,2).toLowerCase(), getBaseContext());
            Log.d("email", personEmail);
            Log.d("rollno", personEmail.substring(0, 8).toLowerCase());
            Log.d("rollno", personEmail.substring(0,2).toLowerCase());
            new  PlacementLogin().execute();
        }
        else {
            Snackbar.make((RelativeLayout) findViewById(R.id.container), " Account you login with is not smail try again using smail account :)", Snackbar.LENGTH_LONG).show();
            signOutFromGplus();
            signin.setEnabled(true);

        }
        //Toast.makeText(this, "User" + personName + " is connected!" + Plus.AccountApi.getAccountName(mGoogleApiClient), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Log.d("hete", "on suspend");

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {

            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
                Log.d("hete", "on result");
                progress.dismiss();
                signin.setEnabled(true);
                signOutFromGplus();

            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    /**
     * Sign-out from google
     */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            Log.d("logout","logout");
        }
        if (progress!=null) {
            progress.dismiss();

        }
    }

    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e("", "User access revoked!");
                            mGoogleApiClient.connect();
                        }

                    });
        }
    }

    private class PlacementLogin extends AsyncTask<String, String, String> {
        String responseBody;
        String token;
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(getString(R.string.dominename)+"/moblogin.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("rollno", Utils.getprefString("rollno", getBaseContext())));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse responseLogin = httpclient.execute(httppost);
                // Log.d("Parse Exception", "" + "");


                try {
                    responseBody = EntityUtils.toString(responseLogin.getEntity());

                } catch (ParseException e) {
                    e.printStackTrace();

                    Log.d("Parse Exception", e + "");
                }
            } catch (ClientProtocolException e) {
                //saveBool("Network_error", true);

                // TODO Auto-generated catch block
            } catch (IOException e) {
                //saveBool("Network_error", true);

                // TODO Auto-generated catch block
            }

            return responseBody;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseBody==null){
                Snackbar.make((RelativeLayout) findViewById(R.id.container), "Error connecting to server !!", Snackbar.LENGTH_SHORT).show();
                Log.d("invalid login", responseBody + "ok");
                signOutFromGplus();
                Utils.clearpref(getBaseContext());
                signin.setEnabled(true);
            }
            else{
                responseBody = responseBody.replaceAll("\\s", "");
                // Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
                if (responseBody.equals("1")) {
                    Log.d("valid login", responseBody + "ok");
                    Intent downloadIntent;
                    downloadIntent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(downloadIntent);
                    Utils.saveprefBool("logedin", true, getBaseContext());
                    signOutFromGplus();

                    //Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
                    finish();

                }
                else if (responseBody.equals("2")){
                    Snackbar.make((RelativeLayout) findViewById(R.id.container), "You have not registered for placement", Snackbar.LENGTH_LONG).show();
                    Log.d("invalid login", responseBody + "ok");
                    signOutFromGplus();
                    Utils.clearpref(getBaseContext());
                    signin.setEnabled(true);

                }
                else{
                    Snackbar.make((RelativeLayout) findViewById(R.id.container), "Error connecting to server !!", Snackbar.LENGTH_SHORT).show();
                    Log.d("invalid login", responseBody + "ok");
                    signOutFromGplus();
                    Utils.clearpref(getBaseContext());
                    signin.setEnabled(true);
                }
            }

        }
    }

}
