package com.example.paul.all_sensor_logger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class NetworkCheckService extends Service {
    private int NetworkBroadcastRepeat = 0;
    private Handler retransmissionHandler;
    private HandlerThread retransmissionThread;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String version="";
    String localversion="";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "onCreate() executed");

        NetworkBroadcastRepeat = 0;

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG", "onStartCommand() executed");

        NetworkBroadcastRepeat = 0;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy() executed");
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            if(NetworkCheck.isNetworkConnected(context))
            {
                if(NetworkBroadcastRepeat <= 0) {
                    Toast.makeText(context, "Network Connected", Toast.LENGTH_LONG).show();
                    localversion = sharedPreferences.getString("VERSION", null);
                    if(localversion == null) {
                        API.get_argeement_version(new ResponseListener() {
                            public void onResponse(JSONObject response) {
                                try {
                                    version = response.getString("version");

                                    localversion = sharedPreferences.getString("VERSION", null);
                                    //Log.d("TAG~",version);

                                    if (localversion == null || version.compareTo(localversion) != 0)//version not match
                                    {
                                        Intent i = new Intent(getApplicationContext(), AgreementPage.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "Can't get contract version", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), AgreementPage.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        });
                    }
                    retransmissionThread = new HandlerThread("retransmission");
                    retransmissionThread.start();
                    retransmissionHandler = new Handler(retransmissionThread.getLooper());
                    retransmissionHandler.post(RetransmissionTask);


                }
                NetworkBroadcastRepeat++;
            }
            else
            {
                Toast.makeText(context, "Network Disconnect", Toast.LENGTH_LONG).show();
                NetworkBroadcastRepeat = 0;
            }
        }
    };

    public Runnable RetransmissionTask = new Runnable() {
        @Override
        public void run() {
            Log.e("Error check","Retransmission");
            String path = Environment.getExternalStorageDirectory().getPath()+"/Sensorlogger/";
            find_all(new File(path));
        }
    };

    public void find_all(File dir)
    {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    find_all(file);
                    if(file.list().length==0)
                    {
                        file.delete();
                    }
                } else {
                    // do something here with the file

                    final String path=file.getPath();
                    String parent = file.getParent();
                    parent = parent.substring(parent.lastIndexOf("/") + 1, parent.length());

                    //Log.d("Tag","parent"+ file.getParent());
                    Log.d("Tag", "name"+file.getName());

                    API.upload_file(file.getParent(),
                            sharedPreferences.getString("token", null),
                            sharedPreferences.getString(parent+"_type", null),
                            sharedPreferences.getString(parent+"_age" , null),
                            file.getName(),
                            sharedPreferences.getString(parent+"_account",null),
                            new ResponseListener() {
                                public void onResponse(JSONObject response) {
                                    File file=new File(path);
                                    file.delete();
                                }

                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Tag", "error");
                                    JSONObject response = null;
                                    try {
                                        if(error.networkResponse != null) {
                                            response = new JSONObject(new String(error.networkResponse.data));
                                            Log.d("Tag", response.toString());}
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(error.networkResponse != null) {
                                        Log.d("Tag", String.valueOf(error.networkResponse.statusCode));
                                        if(error.networkResponse.statusCode == 500 || error.networkResponse.statusCode == 401) {
                                            Log.d("Tag upload error","file already exists on server");
                                            File file_duplicated = new File(path);
                                            file_duplicated.delete();
                                        }
                                    }
                                }
                            });
                }
            }
        }
    }

}
