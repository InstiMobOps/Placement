package in.ac.iitm.placement.app;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;


public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    ArrayList<Event> arrayList = new ArrayList<>();
    ArrayList<Event> arrayListComing = new ArrayList<>();
    ArrayList<Event> arrayListOld = new ArrayList<>();
    ArrayList<Event> arrayListNotification = new ArrayList<>();
    RequestQueue queue = Volley.newRequestQueue(this);

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Tracker mTracker;
    private AppBarLayout appBarLayout;
    int tabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Updatecheck(this);
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker("UA-66080953-1"); // Send hits to tracker id UA-XXXX-Y

// All subsequent hits will be send with screen name = "main screen"
        mTracker.setScreenName("main screen");
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"white\">" + getString(R.string.app_name) + "</font>"));
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);

        tabLayout.addTab(tabLayout.newTab().setText("Coming Up"));
        tabLayout.addTab(tabLayout.newTab().setText("Finished"));
        tabLayout.addTab(tabLayout.newTab().setText("Notifications"));

        tabLayout.setTabTextColors(Color.BLACK, Color.WHITE);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                if (tabPosition == 0)
                    arrayList = arrayListComing;
                else if (tabPosition == 1)
                    arrayList = arrayListOld;
                else
                    arrayList = arrayListNotification;
                mAdapter = new AdapterMainActivityRecycler(MainActivity.this, arrayList);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // what should happen when pulled
                LoadPosts();
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
            LoadPosts();
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
            showFilterDialog();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action_bar_button")
                    .setAction("Filter")
                    .build());
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

    private void showFilterDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Filter By")
                .setSingleChoiceItems(new String[]{"Company", "Event"}, 0, null)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                final int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                ArrayList<String> filterList = Filter(selectedPosition);
                                String[] stockArr = new String[filterList.size()];
                                stockArr = filterList.toArray(stockArr);
                                dialog.dismiss();
                                final String[] finalStockArr = stockArr;
                                String dialogName = "";
                                if (selectedPosition == 0) {
                                    dialogName = "Choose Company";
                                } else if (selectedPosition == 1) {
                                    dialogName = "Choose Event";
                                }

                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Filter By Date")
                                        .setSingleChoiceItems(stockArr, 0, null)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        ArrayList<Event> secondList = new ArrayList<Event>();
                                                        int secondPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                                        if (selectedPosition == 0) {
                                                            for (Event a : arrayList) {
                                                                if (a.getName().equals(finalStockArr[secondPosition])) {
                                                                    secondList.add(a);
                                                                }
                                                            }
                                                        } else if (selectedPosition == 1) {
                                                            for (Event a : arrayList) {
                                                                if (a.getEvent().equals(finalStockArr[secondPosition])) {
                                                                    secondList.add(a);
                                                                }
                                                            }
                                                        }
                                                        mAdapter = new AdapterMainActivityRecycler(MainActivity.this, secondList);
                                                        mRecyclerView.setAdapter(mAdapter);
                                                        dialog.dismiss();
                                                    }
                                                }

                                        )
                                        .show();

                            }
                        }

                )


                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private ArrayList Filter(int selectedPosition) {
        int j = 0;
        ArrayList<String> filterList = new ArrayList<>();
        switch (selectedPosition) {
            case 0:
                for (j = 0; j < arrayList.size(); j++) {
                    String name = arrayList.get(j).getName();
                    int index = filterList.indexOf(name);
                    if (index == -1)
                        filterList.add(name);
                }
                break;
            case 1:
                for (j = 0; j < arrayList.size(); j++) {
                    String name = arrayList.get(j).getEvent();
                    int index = filterList.indexOf(name);
                    if (index == -1)
                        filterList.add(name);

                }
                break;
        }
        return filterList;
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
            LoadPosts();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(Utils.getprefString("ListData", getBaseContext()));
                arrayList.clear();
                arrayListOld.clear();
                arrayListComing.clear();
                arrayListNotification.clear();
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
                    if(temp.getName().equals("")){
                        arrayListNotification.add(temp);
                        Log.d("fuck","awsome");
                    }
                    else if (new Date().after(date)) {
                        arrayListOld.add(temp);
                        Log.d("fuck", "asshole");
                    }
                    else
                        arrayListComing.add(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (tabPosition == 0)
                arrayList = arrayListComing;
            else if (tabPosition == 1)
                arrayList = arrayListOld;
            else
                arrayList=arrayListNotification;
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
    public void LoadPosts(){
        final JSONArray[] jsonArray = new JSONArray[1];
        final SwipeRefreshLayout mSwipeRefreshLayout2 = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        if (!mSwipeRefreshLayout2.isRefreshing()) {
            mSwipeRefreshLayout2.setRefreshing(true);
            mSwipeRefreshLayout2.setColorSchemeResources(R.color.colorAccent);
        }
    String url =getString(R.string.dominename) + "/postsmob.php?department=" + Utils.getprefString("department", getBaseContext());
    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.d("Response", response);
                    String responseBody=response;
                    int oldpostnum = arrayList.size();
                    if (responseBody != null) {
                        Utils.saveprefInt("NotfCount", 0, getBaseContext());
                        ShortcutBadger.with(getApplicationContext()).count(0);
                        Utils.saveprefString("ListData", responseBody, getBaseContext());
                        try {
                            jsonArray[0] = new JSONArray(responseBody);
                            arrayList.clear();
                            arrayListOld.clear();
                            arrayListComing.clear();
                            arrayListNotification.clear();

                            for (int i = 0; i < jsonArray[0].length(); i++) {
                                JSONObject jo = jsonArray[0].getJSONObject(i);
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
                                if(temp.getName().equals("")){
                                    arrayListNotification.add(temp);
                                    Log.d("fuck","awsome");
                                }
                                else if (new Date().after(date)) {
                                    arrayListOld.add(temp);
                                    Log.d("fuck", "asshole");
                                }
                                else
                                    arrayListComing.add(temp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (tabPosition == 0)
                            arrayList = arrayListComing;
                        else if (tabPosition == 1)
                            arrayList = arrayListOld;
                        else
                            arrayList=arrayListNotification;
                        mAdapter = new AdapterMainActivityRecycler(MainActivity.this, arrayList);
                        mRecyclerView.setAdapter(mAdapter);
                        Utils.saveprefBool("firstload", true, getBaseContext());   // for first loading
                        if (arrayList.size() == oldpostnum) {
                            Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), " Nothing new :)", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), Integer.toString(arrayList.size() - oldpostnum) + " new events added :)", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.rootview), "Error connecting to server !!", Snackbar.LENGTH_SHORT).show();

                    }
                    mSwipeRefreshLayout.setRefreshing(false);

                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
    ) ;
    queue.add(postRequest);
}
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    public void ShowUpdateDiloge(){
        new AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage("New vertion is available.Do you want to Download")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                openWebPage(getString(R.string.updatecheckurl));
                                dialog.dismiss();
                            }
                        }

                )
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                })
                .show();
    }
    public void Updatecheck(final Context context){

        String url =context.getString(R.string.updatecheckurl)+"update.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("success","fuck this");

                        response = response.replaceAll("\\s", "");
                        if(response.equals("1")){
                            ShowUpdateDiloge();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("server error", error.toString());

            }
        }){
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<String, String>();
                params.put("appversion", Integer.toString(getAppVersion(context)));
                return params;
            }
        };
        queue.add(stringRequest);

    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
