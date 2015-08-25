package in.ac.iitm.placement.app;

/**
 * Created by arun on 21-Jul-15.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

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

public class RegistrationIntentService extends IntentService {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                // Log.i(TAG, "GCM Registration Token: " + token);

                // TODO: Implement this method to send any registration to your app's servers.
                sendRegistrationToServer(token);

                // Subscribe to topic channels
                subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        //  Log.d("",Utils.getprefString("LastSentToken",getBaseContext()));
        Utils.saveprefString("clintid", token, this);
        // Log.d("LastSentToken", Utils.getprefString("LastSentToken", getBaseContext()));
        // Log.d("clintid", Utils.getprefString("clintid", getBaseContext()));
        Log.d("sdfsdf",token);
        Log.d("last",Utils.getprefString("LastSentToken",getBaseContext()));
        if ((!token.equals(Utils.getprefString("LastSentToken", getBaseContext()))) && Utils.isNetworkAvailable(getBaseContext())) {
            new Register().execute();
        }
        // Add custom implementation, as needed.
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    // [END subscribe_topics]
    private class Register extends AsyncTask<String, String, String> {
        String responseBody;
        String token;
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(getString(R.string.dominename)+"/gcmregister.php");
            try {
                // Add your data
                token = Utils.getprefString("clintid", getBaseContext());
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("gcmid", token));
                nameValuePairs.add(new BasicNameValuePair("rollno", Utils.getprefString("rollno", getBaseContext())));
                nameValuePairs.add(new BasicNameValuePair("department", Utils.getprefString("department",getBaseContext())));
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

            responseBody = responseBody.replaceAll("\\s", "");
            // Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
            if (responseBody.equals("1")||responseBody.equals("2")) {
                Log.d("Responce", responseBody + "ok");

                //Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
                Utils.saveprefString("LastSentToken", Utils.getprefString("clintid", getBaseContext()), getBaseContext());

            }
        }
    }
}
