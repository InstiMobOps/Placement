package in.ac.iitm.placement.app;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import me.leolin.shortcutbadger.ShortcutBadger;


public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    ArrayList<Event> arrayList = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Tracker mTracker;
    private AppBarLayout appBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker("UA-66080953-1"); // Send hits to tracker id UA-XXXX-Y

// All subsequent hits will be send with screen name = "main screen"
        mTracker.setScreenName("main screen");
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"white\">" + getString(R.string.app_name) + "</font>"));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // what should happen when pulled
                new LoadPosts().execute();
                // Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), "Nothin to refresh :)", Snackbar.LENGTH_SHORT).show();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // use a linear layout manager

        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        Initialize();   //when open app
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //String value = extras.getString("new_variable_name");
            new LoadPosts().execute();
        }

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    // Log.d("arun message to you", "token sent to server");
                } else {
                    // Log.d("arun message to you", "some error couldn't  sent token to server");
                }
            }
        };
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: " + "main activity");
        mTracker.setScreenName("main activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(REGISTRATION_COMPLETE));
        appBarLayout.addOnOffsetChangedListener(this);

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(this);

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_filter).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            showSortDialog();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action_bar_button")
                    .setAction("sort")
                    .build());
            return true;
        } else if (id == R.id.action_filter) {


            return true;
        } else if (id == R.id.action_signout) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action_bar_button")
                    .setAction("signout")
                    .build());
            Utils.clearpref(this);
            Intent downloadIntent;
            downloadIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(downloadIntent);
            ShortcutBadger.with(getApplicationContext()).count(0);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void showSortDialog() {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this)
                .setTitle("Sort By")
                .setSingleChoiceItems(new String[]{"Name", "Event Date"}, 0, null)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        // Do something useful withe the position of the selected radio button
                        mAdapter = new AdapterMainActivityRecycler(MainActivity.this, Sort(arrayList, selectedPosition));
                        mRecyclerView.setAdapter(mAdapter);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void Initialize() {
        //mSwipeRefreshLayout.setRefreshing(true);
        if (!Utils.getprefBool("firstload", getBaseContext())) {
            new LoadPosts().execute();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(Utils.getprefString("ListData", getBaseContext()));
                arrayList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    Date date = null;
                    SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd, yyyy hh:mm a");
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jo.getString("event_date"));

                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    Event temp = new Event();
                    temp.setDate(formatter.format(date));
                    temp.setEvent(jo.getString("event"));
                    temp.setVenue(jo.getString("venue"));
                    temp.setName(jo.getString("name"));
                    temp.setDiscription(jo.getString("description"));
                    temp.setFormatedDate(date);
                    arrayList.add(temp);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mAdapter = new AdapterMainActivityRecycler(MainActivity.this, arrayList);
            mRecyclerView.setAdapter(mAdapter);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public ArrayList Sort(ArrayList list, int option) {
        switch (option) {
            case 0: {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("sort")
                        .setAction("by name")
                        .build());
                Collections.sort(list, Event.NameComparator);

            }
            break;
            case 1: {
                Collections.sort(list, Event.DateComparator);
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("sort")
                        .setAction("by date")
                        .build());
            }
            break;
        }
        return list;
    }

    private class LoadPosts extends AsyncTask<String, String, String> {
        String responseBody;
        JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final SwipeRefreshLayout mSwipeRefreshLayout2 = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
            if (!mSwipeRefreshLayout2.isRefreshing()) {
                mSwipeRefreshLayout2.setRefreshing(true);
                mSwipeRefreshLayout2.setColorSchemeResources(R.color.colorAccent);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(getString(R.string.dominename) + "/posts.php?department=" + Utils.getprefString("department", getBaseContext()));
            try {
                // Add your data
                // Execute HTTP Post Request
                HttpResponse responseLogin = httpclient.execute(httpget);
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
            int oldpostnum = arrayList.size();
            if (responseBody != null) {
                Utils.saveprefInt("NotfCount", 0, getBaseContext());
                ShortcutBadger.with(getApplicationContext()).count(0);
                Utils.saveprefString("ListData", responseBody, getBaseContext());
                try {
                    jsonArray = new JSONArray(responseBody);
                    arrayList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        Date date = null;
                        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd, yyyy hh:mm a");
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jo.getString("event_date"));

                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                        Event temp = new Event();
                        temp.setDate(formatter.format(date));
                        temp.setEvent(jo.getString("event"));
                        temp.setVenue(jo.getString("venue"));
                        temp.setName(jo.getString("name"));
                        temp.setDiscription(jo.getString("description"));
                        temp.setFormatedDate(date);
                        arrayList.add(temp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mAdapter = new AdapterMainActivityRecycler(MainActivity.this, arrayList);
                mRecyclerView.setAdapter(mAdapter);
                Utils.saveprefBool("firstload", true, getBaseContext());   // for first loading
                if (arrayList.size() == oldpostnum) {
                    Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), " Nothing new :)", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), Integer.toString(arrayList.size() - oldpostnum) + " new events added :)", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), "Error connectiong to server !!", Snackbar.LENGTH_SHORT).show();

            }
            mSwipeRefreshLayout.setRefreshing(false);


        }
    }


}
