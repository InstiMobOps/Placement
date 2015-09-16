package in.ac.iitm.placement.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arun on 21-Jul-15.
 */
public class Utils {
    public static void saveprefString(String key, String value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getprefString(String key, Context cont) {
        SharedPreferences pref = cont.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getString(key, "");
    }

    public static void saveprefBool(String key, Boolean value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }
    public static void saveprefInt(String key, int value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static int getprefInt(String key, Context cont) {
        SharedPreferences pref = cont.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getInt(key, 0);
    }

    public static Boolean getprefBool(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getBoolean(key, false);

    }

    public static void clearpref(Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
