package com.example.paul.all_sensor_logger;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;

import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by User on 2017/3/8.
 */

public class LogAndUploadService extends Service {
    //use in timer, don't need another thread
    private Handler logFilehandler = new Handler();
    private Handler RunOnUiHandler = new Handler();
    private Handler uploadFileHandler;
    private HandlerThread uploadFileThread;
    private Handler stopLogFileHandler;
    private HandlerThread stopLogFileThread;
    private Handler clockHandler;
    private HandlerThread clockThread;
    private FileOutputStream file_acc;
    private FileOutputStream file_gro;
    private FileOutputStream file_mag;
    private FileOutputStream file_gps;
    private String filename_acc;
    private String filename_gro;
    private String filename_mag;
    private String filename_gps;
    private Sensor[] SensorList = new Sensor[3];
    public static String path;
    private String ts;
    private Timer timer;
    private int FileCount = 0;
    private int UploadFileCount = 0;
    private int tempCount = 0;
    private int logDataCount = 0;
    private LocationManager mgr;
    private String best;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private SensorManager mSensorManager;
    private int timesec1 = 0, csec1 = 0, cmin1 = 0, chour1 = 0;


    IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public LogAndUploadService getServerInstance() {
            return LogAndUploadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        for (int i = 0; i < 3; i++) {
            SensorList[i] = null;
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();
    }


    public class MyTimerTask extends TimerTask
    {

        public void run()
        {
            logFilehandler.post(new Runnable() {
                public void run() {
                    close_all();

                    uploadFileHandler.removeCallbacks(UploadFileTask);
                    uploadFileThread.quit();
                    String threadname = "upload_"+String.valueOf(FileCount);
                    uploadFileThread = new HandlerThread(threadname);
                    uploadFileThread.start();
                    uploadFileHandler = new Handler(uploadFileThread.getLooper());
                    uploadFileHandler.post(UploadFileTask);



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
                            Toast.makeText(LogAndUploadService.this, "ACC fileopen", Toast.LENGTH_SHORT).show();
                        }
                        if (SensorList[1] != null) {
                            file_gro = new FileOutputStream(new File(path, (filename_gro)));
                            Log.d("Tag", "GRO fileopen");
                            Toast.makeText(LogAndUploadService.this, "GRO fileopen", Toast.LENGTH_SHORT).show();
                        }
                        if (SensorList[2] != null) {
                            file_mag = new FileOutputStream(new File(path, (filename_mag)));
                            Log.d("Tag", "MAG fileopen");
                            Toast.makeText(LogAndUploadService.this, "MAG fileopen", Toast.LENGTH_SHORT).show();
                        }

                        file_gps = new FileOutputStream(new File(path, (filename_gps)));

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    //MainFragment.txt_file_number.setText(String.valueOf(FileCount));
                }
            });
        }
    }

    public Runnable UploadFileTask = new Runnable() {
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

    public Runnable ClockTask = new Runnable() {
        @Override
        public void run() {
            timesec1++;
            int newTimer = timesec1;
            csec1 = newTimer % 60;
            newTimer = newTimer / 60;

            cmin1 = newTimer % 60;
            newTimer = newTimer / 60;

            chour1 = newTimer;

            String s ="";

            if (chour1 < 10){
                s="0"+chour1;
            }else {
                s=""+chour1;
            }
            if (cmin1 < 10){
                s=s+":0"+cmin1;
            } else {
                s=s+":"+cmin1;
            }
            if (csec1 < 10){
                s=s+":0"+csec1;
            } else {
                s=s+":"+csec1;
            }
            final String txt_time = s;
            RunOnUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainFragment.txt_log_time.setText(txt_time);
                }
            });
            //每次執行Thred只會執行一次，必須靠延遲1秒後執行Handler來進行重複執行
            clockHandler.postDelayed(ClockTask,1000);
        }
    };

    final SensorEventListener mysensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            tempCount = tempCount + 1;
            if(tempCount >= 100) {
                logDataCount = logDataCount + 1;
                tempCount = 1;
            }
            if(logDataCount >= 100000)
                logDataCount = 1;
            RunOnUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainFragment.txt_file_number.setText(String.valueOf(logDataCount));
                }
            });
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
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        file_gro.write(data.getBytes());
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        file_mag.write(data.getBytes());
                        break;

                    default:
                        Log.d("Tag", "unexcepted sensor type");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void fileupload(final String filepath,final String filename, final String CarType, final String CarAge) throws IOException {
        if (/*NetworkCheck.isNetworkConnected(getContext())*/true) {


            //not upload 0B file
            final File file = new File(filepath+"/"+filename);
            if(file.length() == 0)
            {
                file.delete();
                return;
            }

            //token_check();
            final String token = sharedPreferences.getString("token", null);
            API.upload_file(filepath, token,CarType, CarAge, filename, sharedPreferences.getString("account", null), new ResponseListener() {
                public void onResponse(JSONObject response) {
                    File file = new File(filepath+"/"+filename);
                    file.delete();
                    Toast.makeText(LogAndUploadService.this, "upload success@@", Toast.LENGTH_SHORT).show();
                }

                public void onErrorResponse(VolleyError error) {
                    //file already exists on server
                    if(error.networkResponse != null)
                    {
                        if(error.networkResponse.statusCode == 500 || error.networkResponse.statusCode == 401)
                        {
                            Log.d("Tag upload error","file already exists on server");
                            File file_duplicated = new File(filepath+"/"+filename);
                            file_duplicated.delete();
                        }
                    }
                    else {
                        Toast.makeText(LogAndUploadService.this, "something went wrong while uploading", Toast.LENGTH_SHORT).show();
                        JSONObject response = null;
                        Log.d("Tag status", error + ">>" + error.networkResponse + "\n");


                        try {
                            if (error.networkResponse != null) {
                                response = new JSONObject(new String(error.networkResponse.data));
                                Log.d("Tag", response.toString());
                                Log.d("Tag status code", String.valueOf(error.networkResponse.statusCode));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                        editor.putString(ts + "_type", CarType);
                        editor.putString(ts + "_age", CarAge);
                        editor.putString(ts + "_account", sharedPreferences.getString("account", null));
                        editor.commit();
                    }
                }
            });

        } else {
            Toast.makeText(LogAndUploadService.this, "no network aviable now, will upload later", Toast.LENGTH_SHORT).show();
            editor.putString(ts + "_type", CarType);
            editor.putString(ts + "_age", CarAge);
            editor.putString(ts + "_account", sharedPreferences.getString("account", null));
            editor.commit();
        }
    }

    private void folderfileupload(final String dirpath, final String CarType, final String CarAge) throws IOException {
        File file_dir = new File(dirpath);
        File[] files = file_dir.listFiles();
        if(files.length > 0) {
            for (int i = 0; i < files.length; ++i) {
                String filename = files[i].getName();
                fileupload(dirpath, filename, CarType, CarAge);
            }
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
        mgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (best != null)
            location = mgr.getLastKnownLocation(best);
        mgr.requestLocationUpdates(best, 1000, 0, locationlistener); // 讓locationlistener處理資料有變化時的事情
        mgr.addGpsStatusListener(GPSstatusListener);//to get GPS status
        Log.d("GPS", "GPS Ready");
        Toast.makeText(this, best, Toast.LENGTH_SHORT).show();
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



    public Runnable StopLogFileTask = new Runnable() {
        @Override
        public void run() {
            if(timer != null)
                timer.cancel();
            FileCount = 0;
            mSensorManager.unregisterListener(mysensorListener);
            try {
                if(mgr != null) {
                    mgr.removeUpdates(locationlistener);
                    mgr.removeGpsStatusListener(GPSstatusListener);
                }
            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
            int counter = sharedPreferences.getInt("CarInfoNow", 0);
            String t_cartype = sharedPreferences.getString("CarType" + counter, null);
            String t_carage = sharedPreferences.getString("CarAge" + counter, null);

            close_all();
            for (int i = 0; i < 3; i++) {
                SensorList[i] = null;
            }
            uploadFileHandler.removeCallbacks(UploadFileTask);
            uploadFileThread.quit();
            try {
                folderfileupload(LogAndUploadService.path, t_cartype, t_carage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("STOP","stop finished upload");

        }
    };

    public void startLog()
    {
        initialLocationManager();
         /*Start  listening*/
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
                Toast.makeText(LogAndUploadService.this, "ACC fileopen", Toast.LENGTH_SHORT).show();
            }
            if (SensorList[1] != null) {
                file_gro = new FileOutputStream(new File(path, (filename_gro)));
                Log.d("Tag", "GRO fileopen");
                Toast.makeText(LogAndUploadService.this, "GRO fileopen", Toast.LENGTH_SHORT).show();
            }
            if (SensorList[2] != null) {
                file_mag = new FileOutputStream(new File(path, (filename_mag)));
                Log.d("Tag", "MAG fileopen");
                Toast.makeText(LogAndUploadService.this, "MAG fileopen", Toast.LENGTH_SHORT).show();
            }
            file_gps = new FileOutputStream(new File(path, (filename_gps)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String threadname = "upload_"+String.valueOf(FileCount);
        uploadFileThread = new HandlerThread(threadname);
        uploadFileThread.start();
        uploadFileHandler = new Handler(uploadFileThread.getLooper());
        timer = new Timer(true);
        timer.schedule(new MyTimerTask(), 30000, 30000);
        RunOnUiHandler.post(new Runnable() {
            @Override
            public void run() {
                //MainFragment.txt_file_number.setText(String.valueOf(FileCount));
                MainFragment.txt_log_time.setText("00:00:00");
            }
        });
        timesec1 = 0;
        csec1 = 0;
        cmin1 = 0;
        chour1 = 0;
        clockThread = new HandlerThread("clock");
        clockThread.start();
        clockHandler = new Handler(clockThread.getLooper());
        clockHandler.post(ClockTask);
    }


    public void stopLog()
    {
        stopLogFileThread = new HandlerThread("stop");
        stopLogFileThread.start();
        stopLogFileHandler = new Handler(stopLogFileThread.getLooper());
        stopLogFileHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Tag","wait other threads for 3 sec");
            }}, 3000);
        stopLogFileHandler.post(StopLogFileTask);

        clockHandler.removeCallbacks(ClockTask);
        clockThread.quit();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy() executed");
        if(timer != null)
            timer.cancel();
        FileCount = 0;

        if(mSensorManager != null)
            mSensorManager.unregisterListener(mysensorListener);

        try {
            if(mgr != null) {
                mgr.removeUpdates(locationlistener);
                mgr.removeGpsStatusListener(GPSstatusListener);
            }
        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
        }
        int counter = sharedPreferences.getInt("CarInfoNow", 0);
        String t_cartype = sharedPreferences.getString("CarType" + counter, null);
        String t_carage = sharedPreferences.getString("CarAge" + counter, null);

        if(uploadFileHandler != null)
            uploadFileHandler.removeCallbacks(UploadFileTask);
        if(uploadFileThread != null)
            uploadFileThread.quit();
        try {
            close_all();
            for (int i = 0; i < 3; i++) {
                SensorList[i] = null;
            }
            if(path != null && !path.isEmpty())
                folderfileupload(path, t_cartype, t_carage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(clockHandler != null)
            clockHandler.removeCallbacks(ClockTask);
        if(clockThread != null)
            clockThread.quit();
    }
}
