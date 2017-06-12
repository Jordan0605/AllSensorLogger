package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.paul.all_sensor_logger.bt.BTSerialDevice;
import com.example.paul.all_sensor_logger.bt.BTSerialPortCommunicationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import android.webkit.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import com.example.paul.all_sensor_logger.LogAndUploadService.LocalBinder;


import static android.content.Context.BIND_AUTO_CREATE;
import static android.location.LocationManager.NETWORK_PROVIDER;

/*Tab page class inhreits Fragment*/
public class MainFragment extends Fragment {
    //buttons
    private Button startbutton;
    private Button logoutbutton;
    private Button recordbutton;
    private SensorManager mSensorManager;
    private List<String> deviceSensorsName = new ArrayList<String>();
    private ListView lv;
    private List<Sensor> deviceSensors = new ArrayList<Sensor>();
    private FileOutputStream file_acc;
    private FileOutputStream file_gro;
    private FileOutputStream file_mag;
    private FileOutputStream file_gps;
    private String subdir;
    private String filename_acc;
    private String filename_gro;
    private String filename_mag;
    private String filename_gps;
    private String filename_audio;
    private MainActivity mMainActivity;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String path;
    private String ts;
    private int IsFileOpen = 0;
    private Sensor[] SensorList = new Sensor[3];
    private boolean is_recording;
    private MediaRecorder mRecorder = null;
    //private Thread timer;
    private LocationManager mgr;
    private String best;
    private boolean start = false;
    int FileCount = 0;
    int UploadFileCount = 0;
    Timer timer;
    private Handler handler = new Handler();
    private Handler uploadFileHandler = new Handler();
    private Handler retransmissionFileHandler = new Handler();
    private Intent LogAndUploadServiceIntent;
    LogAndUploadService mService;
    boolean mBounded;
    private final String IS_LOGGING = "IS_LOGGING";
    public static TextView txt_location_provider;
    public static TextView txt_file_number;
    public static TextView txt_log_time;
    public static TextView txt_current_car;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        //timer = new Timer(true);
        //findBT();


        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();
        Boolean isLogging = sharedPreferences.getBoolean(IS_LOGGING,false);
        init_buttons();

        //set text view
        txt_location_provider = (TextView) getView().findViewById(R.id.txt_location_provider);
        txt_file_number = (TextView) getView().findViewById(R.id.txt_file_number);
        txt_log_time = (TextView) getView().findViewById(R.id.txt_log_time);
        txt_current_car = (TextView) getView().findViewById(R.id.txt_current_car);
        //check location provider
        timer = new Timer(true);
        timer.schedule(new TrackLocationProvider(), 0, 500);

        initialLocationManager();
        //flags
        is_recording = false;
        IsFileOpen = 0;

        /*get sensor*/
        //mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        String t_name = sharedPreferences.getString("Name1", null);
        String t_cartype = sharedPreferences.getString("CarType1", null);
        String t_carage = sharedPreferences.getString("CarAge1", null);

        Toast.makeText(getContext(), t_name + t_cartype + t_carage, Toast.LENGTH_LONG).show();

        if (t_carage == null || t_cartype == null || t_name == null) {
            int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
            if (CarInfoCounter == 0) {
                editor.putInt("CarInfoCounter", 1);
                editor.putInt("CarInfoNow", 1);
                editor.commit();
            }
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

            new AlertDialog.Builder(getActivity())
                    .setTitle("Please key in new car info")
                    .setView(v1)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String Pop_name = ((EditText) v1.findViewById(R.id.pop_name)).getText().toString();
                            String Pop_cartype = ((EditText) v1.findViewById(R.id.pop_cartype)).getText().toString();
                            String Pop_carage = ((EditText) v1.findViewById(R.id.pop_carage)).getText().toString();

                            if ("".equals(Pop_name)) {
                                Toast.makeText(getContext(), "Name can't be empty", Toast.LENGTH_LONG).show();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if ("".equals(Pop_cartype)) {
                                Toast.makeText(getContext(), "Car type can't be empty", Toast.LENGTH_LONG).show();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if ("".equals(Pop_carage)) {
                                Toast.makeText(getContext(), "Car age can't be empty", Toast.LENGTH_LONG).show();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {
                                int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                if (CarInfoCounter == 0) {
                                    Log.v("Tag", "Something wrong in main 1");
                                } else {
                                    CarInfoCounter++;
                                    String c = "Name1";
                                    String a = "CarType1";
                                    String b = "CarAge1";
                                    editor.putString(c, Pop_name);
                                    editor.putString(a, Pop_cartype);
                                    editor.putString(b, Pop_carage);
                                    editor.putString("Device", android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
                                    editor.commit();
                                    String t_id = sharedPreferences.getString("ID", null);
                                    Update(t_id, Pop_name, Pop_cartype, Pop_carage, android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
                                }
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    })
                    .setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_BACK:
                                    Log.v("Tag", "KEYCODE_BACK");
                                    return true;
                            }
                            return false;
                        }
                    })
                    .show();

        }else{
//            int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
//            if (CarInfoCounter == 0) {
//                editor.putInt("CarInfoCounter", 1);
//                editor.putInt("CarInfoNow", 1);
//                editor.commit();
//            }
        }
        if(isLogging != true) {
            LogAndUploadServiceIntent = new Intent(getActivity(), LogAndUploadService.class);
            getActivity().bindService(LogAndUploadServiceIntent, mConnection, BIND_AUTO_CREATE);
        }
        else
        {
            /*Start  listening*/
            startbutton.setBackgroundColor(Color.RED);
            startbutton.setText("Stop");
            //VolleyController.notificationServiceStartBuilder(getActivity());
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            LocalBinder mLocalBinder = (LocalBinder) service;
            mService = mLocalBinder.getServerInstance();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_logout:
                String version = sharedPreferences.getString("VERSION", null);
                editor.clear();
                editor.commit();
                editor.putString("VERSION", version);
                editor.commit();
                VolleyController.getInstance().setUserCredentials(null);
                Intent intent = new Intent();
                intent.setClass(getActivity(),LoginPage.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps();
        }

        int counter = sharedPreferences.getInt("CarInfoNow", 0);
        if(counter != 0) {
            String t_cartype = sharedPreferences.getString("CarType" + counter, null);
            txt_current_car.setText(t_cartype);
        }
        else
            Toast.makeText(getActivity(), "Currently no selected car !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Boolean isLogging = sharedPreferences.getBoolean(IS_LOGGING,false);
        if(isLogging != true) {
            if(mSensorManager != null)
                mSensorManager.unregisterListener(mysensorListener);
            if (IsFileOpen == 1) {
                close_all();
                IsFileOpen = 0;
            }
            if (is_recording) {
                //stopRecording();
            }
            VolleyController.cancelNotificationService(getActivity());
            getActivity().unbindService(mConnection);
            Intent intent = new Intent(getActivity(), NetworkCheckService.class);
            getActivity().stopService(intent);
        }
        timer.cancel();
    }


    final SensorEventListener mysensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(start){
            long timeStamp = System.currentTimeMillis();
            String data = Long.toString(timeStamp);
            for (float val : event.values) {
                data = data + "," + val;
            }
            data += "\n";
            try {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        file_acc.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        file_gro.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        file_mag.write(data.getBytes());
                        IsFileOpen = 1;
                        break;

                    default:
                        Log.d("Tag", "unexcepted sensor type");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private String Update(final String id, final String name, final String cartype, final String carage, final String device){
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~andersen/UpdateUser.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("success")){
                    Toast.makeText(getActivity(), "Add Success!", Toast.LENGTH_SHORT).show();
                    txt_current_car.setText(cartype);

                }else{
                    Toast.makeText(getActivity(), "Add Fail!", Toast.LENGTH_SHORT).show();


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("ID", id );
                map.put("NAME", name);
                map.put("CAR_TYPE", cartype);
                map.put("CAR_AGE", carage);
                map.put("DEVICE_NAME", device);
                map.put("OLD_NAME", "");
                map.put("OLD_CAR_TYPE", "");
                map.put("OLD_CAR_AGE", "0");
                map.put("OLD_DEVICE_NAME", "");
                return map;
            }
        };
        mQueue.add(stringRequest);

        return "";

    }

    private Button.OnClickListener logoutbuttonListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            timer.cancel();
            String version = sharedPreferences.getString("VERSION", null);
            editor.clear();
            editor.commit();
            editor.putString("VERSION", version);
            editor.commit();
            VolleyController.getInstance().setUserCredentials(null);
            CookieManager cookieManager = CookieManager.getInstance();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                cookieManager.removeAllCookies(null);
            else
                cookieManager.removeAllCookie();
            Intent intent = new Intent();
            intent.setClass(getActivity(),LoginPage.class);
            startActivity(intent);
            getActivity().finish();
        }
    };

    private Button.OnClickListener  diebuttonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
           String a=null;
            a.length();
        }
    };



    private Handler uiCallback = new Handler() {
        public void handleMessage(Message msg) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            try {
                fileupload(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/", filename_audio,"","");
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            Long tsLong = System.currentTimeMillis() / 1000;
            mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + tsLong.toString() + ".3gp");
            filename_audio=tsLong.toString() + ".3gp";
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("Tag", "prepare() failed");
            }
            mRecorder.start();

        }
    };


    private Button.OnClickListener startbuttonListener = new Button.OnClickListener() {

        @Override

        public void onClick(View v) {


            if (startbutton.getText().equals("Stop")) {

/*
                LogAndUploadServiceIntent = new Intent(getActivity(), LogAndUploadService.class);
                getActivity().stopService(LogAndUploadServiceIntent);
                retransmissionFileHandler.post(RetransmissionFileThread);*/
                mService.stopLog();
                startbutton.setBackgroundResource(android.R.drawable.btn_default);
                startbutton.setText("Start");
                VolleyController.cancelNotificationService(getActivity());
                editor.putBoolean(IS_LOGGING,false);
                editor.commit();
/*
                timer.cancel();
                FileCount = 0;


                mSensorManager.unregisterListener(mysensorListener);
                try {
                    mgr.removeUpdates(locationlistener);
                    mgr.removeGpsStatusListener(GPSstatusListener);
                } catch (SecurityException e) {
                    Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
                }
                int counter = sharedPreferences.getInt("CarInfoNow", 0);
                String t_cartype = sharedPreferences.getString("CarType" + counter, null);
                String t_carage = sharedPreferences.getString("CarAge" + counter, null);



                try {
                    close_all();
                    for (int i = 0; i < 3; i++) {
                        SensorList[i] = null;
                    }
                    folderfileupload(path, t_cartype, t_carage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
*/

            } else {


                /*Start  listening*/
                startbutton.setBackgroundColor(Color.RED);
                startbutton.setText("Stop");

                /*LogAndUploadServiceIntent = new Intent(getActivity(), LogAndUploadService.class);
                getActivity().startService(LogAndUploadServiceIntent);*/
                mService.startLog();
                editor.putBoolean(IS_LOGGING,true);
                editor.commit();
                VolleyController.notificationServiceStartBuilder(getActivity());
/*
                for (int i = 0; i < 3; i++) {
                    SensorList[i] = null;
                }
                SensorList[0] = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(mysensorListener, SensorList[0], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[1] = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                mSensorManager.registerListener(mysensorListener, SensorList[1], SensorManager.SENSOR_DELAY_FASTEST);
                SensorList[2] = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                mSensorManager.registerListener(mysensorListener, SensorList[2], SensorManager.SENSOR_DELAY_FASTEST);

                Long tsLong = System.currentTimeMillis() / 1000;
                ts = tsLong.toString();

                path = Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + ts;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileCount = FileCount + 1;
                filename_acc = ts + "_acc_" + FileCount;
                filename_gro = ts + "_gro_" + FileCount;
                filename_mag = ts + "_mag_" + FileCount;
                filename_gps = ts + "_gps_" + FileCount;

                try {
                    if (SensorList[0] != null) {
                        file_acc = new FileOutputStream(new File(path, (filename_acc)));
                        Log.d("Tag", "ACC fileopen");
                        Toast.makeText(getContext(), "ACC fileopen", Toast.LENGTH_SHORT).show();
                    }
                    if (SensorList[1] != null) {
                        file_gro = new FileOutputStream(new File(path, (filename_gro)));
                        Log.d("Tag", "GRO fileopen");
                        Toast.makeText(getContext(), "GRO fileopen", Toast.LENGTH_SHORT).show();
                    }
                    if (SensorList[2] != null) {
                        file_mag = new FileOutputStream(new File(path, (filename_mag)));
                        Log.d("Tag", "MAG fileopen");
                        Toast.makeText(getContext(), "MAG fileopen", Toast.LENGTH_SHORT).show();
                    }
                    file_gps = new FileOutputStream(new File(path, (filename_gps)));
                    start = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                startbutton.setText("Stop");



                timer = new Timer(true);
                timer.schedule(new MyTimerTask(), 30000, 30000);

*/
            }

        }
    };





    public class MyTimerTask extends TimerTask
    {

        public void run()
        {
            handler.post(new Runnable() {
                public void run() {
/*
                    //END
                    int counter = sharedPreferences.getInt("CarInfoNow", 0);
                    String t_cartype = sharedPreferences.getString("CarType" + counter, null);
                    String t_carage = sharedPreferences.getString("CarAge" + counter, null);


                    try {
                        close_all();
                        folderfileupload(path, t_cartype, t_carage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //DELAY 3 SECONDS


                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

*/
                    close_all();
                    uploadFileHandler.post(UploadFileThread);
                    //RESTART
                    path = Environment.getExternalStorageDirectory().getPath() + "/Sensorlogger/" + ts;
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    FileCount = FileCount + 1;
                    filename_acc = ts + "_acc_" + FileCount;
                    filename_gro = ts + "_gro_" + FileCount;
                    filename_mag = ts + "_mag_" + FileCount;
                    filename_gps = ts + "_gps_" + FileCount;
                    Log.e("filenameDebug", filename_acc);

                    try {
                        if (SensorList[0] != null) {
                            file_acc = new FileOutputStream(new File(path, (filename_acc)));
                            Log.d("Tag", "ACC fileopen");
                            Toast.makeText(getContext(), "ACC fileopen", Toast.LENGTH_SHORT).show();
                        }
                        if (SensorList[1] != null) {
                            file_gro = new FileOutputStream(new File(path, (filename_gro)));
                            Log.d("Tag", "GRO fileopen");
                            Toast.makeText(getContext(), "GRO fileopen", Toast.LENGTH_SHORT).show();
                        }
                        if (SensorList[2] != null) {
                            file_mag = new FileOutputStream(new File(path, (filename_mag)));
                            Log.d("Tag", "MAG fileopen");
                            Toast.makeText(getContext(), "MAG fileopen", Toast.LENGTH_SHORT).show();
                        }

                        file_gps = new FileOutputStream(new File(path, (filename_gps)));


                        start = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    public Runnable UploadFileThread = new Runnable() {
        @Override
        public void run() {
            UploadFileCount = UploadFileCount +1;
            int counter = sharedPreferences.getInt("CarInfoNow", 0);
            String t_cartype = sharedPreferences.getString("CarType" + counter, null);
            String t_carage = sharedPreferences.getString("CarAge" + counter, null);
            String upload_filename_acc = ts + "_acc_" + UploadFileCount;
            String upload_filename_gro = ts + "_gro_" + UploadFileCount;
            String upload_filename_mag = ts + "_mag_" + UploadFileCount;
            String upload_filename_gps = ts + "_gps_" + UploadFileCount;
            try {
                //close_all();
                //folderfileupload(path, t_cartype, t_carage);
                fileupload(path,upload_filename_acc,t_cartype,t_carage);
                fileupload(path,upload_filename_gro,t_cartype,t_carage);
                fileupload(path,upload_filename_mag,t_cartype,t_carage);
                fileupload(path,upload_filename_gps,t_cartype,t_carage);
            } catch (IOException e) {
                e.printStackTrace();
            }
                    }
    };

    public Runnable RetransmissionFileThread = new Runnable() {
        @Override
        public void run() {
            int counter = sharedPreferences.getInt("CarInfoNow", 0);
            String t_cartype = sharedPreferences.getString("CarType" + counter, null);
            String t_carage = sharedPreferences.getString("CarAge" + counter, null);
            try {
                close_all();
                folderfileupload(LogAndUploadService.path, t_cartype, t_carage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private void token_check() {
        API.check_token_valid(sharedPreferences.getString("token", null), new ResponseListener() {
            public void onResponse(JSONObject response) {

            }

            public void onErrorResponse(VolleyError error) {
                API.login(sharedPreferences.getString("account", null), sharedPreferences.getString("passwd", null), new ResponseListener() {
                    public void onResponse(JSONObject response) {
                        Log.d("Tag", "token expire,getting new token");
                        String token = null;
                        try {
                            token = response.getJSONObject("data").getString("token");
                            editor.putString("token", token);
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "no token found while uploading, please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getContext(), LoginPage.class);
                        startActivity(i);
                    }
                });
            }
        });
    }

    private void fileupload(final String filepath,final String filename, final String CarType, final String CarAge) throws IOException {
        if (/*NetworkCheck.isNetworkConnected(getContext())*/true) {
            //token_check();
            final String token = sharedPreferences.getString("token", null);
            API.upload_file(filepath, token,CarType, CarAge, filename, sharedPreferences.getString("account", null), new ResponseListener() {
                public void onResponse(JSONObject response) {
                    File file = new File(filepath+"/"+filename);
                    file.delete();
                    Toast.makeText(getContext(), "upload success@@", Toast.LENGTH_SHORT).show();
                }

                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "something went wrong while uploading", Toast.LENGTH_SHORT).show();
                    JSONObject response = null;
                    Log.d("Tag status", error + ">>" +error.networkResponse+"\n");


                    try {
                        if(error.networkResponse != null) {
                            response = new JSONObject(new String(error.networkResponse.data));
                            Log.d("Tag", response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Log.d("Tag", String.valueOf(error.networkResponse.statusCode));

                    editor.putString(ts + "_type", CarType);
                    editor.putString(ts + "_age", CarAge);
                    editor.putString(ts + "_account", sharedPreferences.getString("account", null));
                    editor.commit();
                }
            });

        } else {
            Toast.makeText(getContext(), "no network aviable now, will upload later", Toast.LENGTH_SHORT).show();
            editor.putString(ts + "_type", CarType);
            editor.putString(ts + "_age", CarAge);
            editor.putString(ts + "_account", sharedPreferences.getString("account", null));
            editor.commit();
        }
    }

    private void folderfileupload(final String dirpath, final String CarType, final String CarAge) throws IOException {
        File file_dir = new File(dirpath);
        File[] files = file_dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            String filename = files[i].getName();
            fileupload( dirpath,filename, CarType, CarAge);
        }
    }

    private void close_all() {
        try {
            if (SensorList[0] != null) {
                file_acc.close();
            }
            if (SensorList[1] != null) {
                file_gro.close();
            }
            if (SensorList[2] != null) {
                file_mag.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initialLocationManager() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        /*
        mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (best != null)
            location = mgr.getLastKnownLocation(best);
        mgr.requestLocationUpdates(NETWORK_PROVIDER, 1000, 0, locationlistener); // 讓locationlistener處理資料有變化時的事情
        mgr.addGpsStatusListener(GPSstatusListener);//to get GPS status
        Log.d("GPS", "GPS Ready");
        Toast.makeText(getContext(), best, Toast.LENGTH_SHORT).show();*/
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public class TrackLocationProvider extends TimerTask
    {
        public void run()
        {
            //solve Fragment MainFragment not attached to Activity problem
            Activity activity = getActivity();
            if(activity != null && isAdded())
            handler.post(new Runnable() {
                public void run() {
                    if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        txt_location_provider.setText(getString(R.string.toggle_off));
                    else
                        txt_location_provider.setText(getString(R.string.toggle_on));
                }
            });
        }
    }

    private final LocationListener locationlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            float speed = location.getSpeed() * (float) (3.6);
            long time = location.getTime();
            double height = location.getAltitude();
            float bearing = location.getBearing();
            Log.e("FileCount",String.valueOf(FileCount));

            String data = time + "," + lat + "," + lng + "," + speed + "," + height + "," + bearing + "\n";
            try {
                file_gps.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    };

    private final GpsStatus.Listener GPSstatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            // TODO Auto-generated method stub
        }
    };

    private void init_buttons() {
        startbutton = (Button) getView().findViewById(R.id.start_button);
        startbutton.setOnClickListener(startbuttonListener);
        logoutbutton = (Button) getView().findViewById(R.id.logout_button);
        logoutbutton.setOnClickListener(logoutbuttonListener);
    }

    /*private void findBT()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), "The device don't support bluetooth", Toast.LENGTH_LONG).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v1 = inflater.inflate(R.layout.popup_layout_carlist, null);
        ListView listView = (ListView) v1.findViewById(R.id.car_list);
        listView.setAdapter(mArrayAdapter);


        final AlertDialog dialog_list = new AlertDialog.Builder(getActivity())
                .setTitle("Choose device")
                .setView(v1)
                .setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                Log.v("Tag", "KEYCODE_BACK");
                                return true;
                        }
                        return false;
                    }
                })
                .show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//响应listview中的item的点击事件

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                dialog_list.cancel();
            }
        });
    }*/
}


