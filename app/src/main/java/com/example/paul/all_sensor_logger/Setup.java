package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.paul.all_sensor_logger.views.CarInfoItem;
import com.example.paul.all_sensor_logger.views.CarInfoListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by NOL on 2016/3/7.
 */
public class Setup extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
    private MainActivity mMainActivity;

    private String a;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int pos;
    EditText nameEditText ;
    EditText carTypeEditText ;
    EditText carAgeEditText ;
    private Button btn_save;
    private Button btn_new_profile;
    private ListView mListView;
    private CarInfoListAdapter mAdapter;
    private List<CarInfoItem> CarInfoItemList = new ArrayList<>();;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        //取得MainActivity的方法，將文字放入text字串
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //導入Tab分頁的Fragment Layout
        return inflater.inflate(R.layout.fragment_layout_setup_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new CarInfoListAdapter(getContext(),CarInfoItemList);
        mListView = (ListView) getActivity().findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor=sharedPreferences.edit();


        final ArrayList<String> CarInfo = new ArrayList<>();
        String temp = "";

        check_CarInfo();

        int counter = sharedPreferences.getInt("CarInfoCounter",0);
        int Now_CarInfo = sharedPreferences.getInt("CarInfoNow",0);

        if(counter == 0)
        {
            Log.v("Tag", "Something wrong in profile 1");
        }
        else
        {
            for(int i = 1; i <= counter;i++)
            {
                temp = "";
                String c = "Name" + i;
                String a = "CarType" + i;
                String b = "CarAge" + i;
                String d = "Device";
//                String e = "ID";
                String t_name = sharedPreferences.getString(c, null);
                String t_cartype = sharedPreferences.getString(a, null);
                String t_carage = sharedPreferences.getString(b, null);
                //String t_device = sharedPreferences.getString(d, null);
                String t_device = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
//                String t_device = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
//                String t_id = sharedPreferences.getString(e, null);

                if("".equals(t_cartype))
                {
                    Log.v("Tag", "Something wrong in profile 2");
                }
                else if("".equals(t_carage)){
                    Log.v("Tag", "Something wrong in profile 3");
                }
                else if("".equals(t_name)){
                    Log.v("Tag", "Something wrong in profile new");
                }

                else
                {
                    //temp = t_name+","+t_cartype+","+t_carage+","+t_device;
                    temp = t_cartype;
//                    temp = t_id;
                    CarInfo.add(temp);

                    CarInfoItem newCarInfo = new CarInfoItem(t_name,t_cartype,t_carage);
                    if(Now_CarInfo == i)
                        newCarInfo.setIsSelected(true);
                    else
                        newCarInfo.setIsSelected(false);
                    CarInfoItemList.add(newCarInfo);

                }
            }
            mAdapter.notifyDataSetChanged();
        }

//        btn_save = (Button) getView().findViewById(R.id.send_profile);
        btn_new_profile = (Button) getView().findViewById(R.id.new_profile);

/*
        ShowCarInfo();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                              @Override
                                              public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                                  Toast.makeText(getContext(),String.valueOf(position), Toast.LENGTH_LONG).show();
                                                  position++;
                                                  pos = position;
                                                  editor.putInt("CarInfoNow", pos);
                                                  editor.commit();
                                                  ShowCarInfo();

                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> arg0) {
                                              }
                                          }

        );

        btn_save.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           String new_name = nameEditText.getText().toString();
                                           String new_car_type= carTypeEditText.getText().toString();
                                           String new_car_age = carAgeEditText.getText().toString();
                                           String new_device_name = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
                                           Update_User(new_name, new_car_type, new_car_age, new_device_name);
                                       }
                                   }

        );
*/
        btn_new_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

                //語法一：new AlertDialog.Builder(主程式類別).XXX.XXX.XXX;
                new AlertDialog.Builder(getActivity())
                        .setTitle("Please key in new car info")
                        .setView(v1)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String Pop_name = ((EditText) v1.findViewById(R.id.pop_name)).getText().toString();
                                String Pop_cartype = ((EditText) v1.findViewById(R.id.pop_cartype)).getText().toString();
                                String Pop_carage = ((EditText) v1.findViewById(R.id.pop_carage)).getText().toString();

                                if ("".equals(Pop_cartype)) {
                                    Toast.makeText(getContext(), "Car type can't be empty", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if ("".equals(Pop_carage)) {
                                    Toast.makeText(getContext(), "Car age can't be empty", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if ("".equals(Pop_name)){
                                    Toast.makeText(getContext(), "name can't be empty", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                if (CarInfoCounter == 0) {
                                    Log.v("Tag", "Something wrong in profile 2");
                                } else {
                                    CarInfoCounter++;

                                    String a = "Name" + CarInfoCounter;
                                    String b = "CarAge" + CarInfoCounter;
                                    String c = "CarType" + CarInfoCounter;


                                    editor.putInt("CarInfoCounter", CarInfoCounter);
                                    editor.putString(a, Pop_name);
                                    editor.putString(b, Pop_carage);
                                    editor.putString(c, Pop_cartype);
                                    editor.putString("Device", android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
                                    editor.commit();
                                    String t_id = sharedPreferences.getString("ID", null);
                                    CreateUser(t_id, Pop_name, Pop_cartype, Pop_carage, android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
                                    CarInfoItem newCarInfo = new CarInfoItem(Pop_name,Pop_cartype,Pop_carage);
                                    CarInfoItemList.add(newCarInfo);
                                    mAdapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(), "CarInfo add success, please refresh is page", Toast.LENGTH_LONG).show();

                                }

                            }
                        })
                        .show();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CarInfoItem object = mAdapter.getData().get(position);
        final String name = object.getName();
        final String carType = object.getCarType();
        final String carAge = object.getCarAge();
        Bundle bundle = new Bundle();
        bundle.putString(VolleyController.CAR_INFO_NAME, name);
        bundle.putString(VolleyController.CAR_INFO_CARTYPE, carType);
        bundle.putString(VolleyController.CAR_INFO_CARAGE, carAge);
        bundle.putInt(VolleyController.LIST_VIEW_POSITION, position);
        Intent intent = new Intent(getActivity(), EditProfile.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, VolleyController.REQUEST_CODE_EDIT);
    }

    @Override
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id)
    {
        editor.putInt("CarInfoNow",position+1);
        editor.commit();
        for(int i=0; i<mAdapter.getCount(); i++)
        {
            CarInfoItem object = mAdapter.getData().get(i);
            if(i == position)
                object.setIsSelected(true);
            else
                object.setIsSelected(false);
        }
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VolleyController.REQUEST_CODE_EDIT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    final String name = bundle.getString(VolleyController.CAR_INFO_NAME);
                    final String carType = bundle.getString(VolleyController.CAR_INFO_CARTYPE);
                    final String carAge = bundle.getString(VolleyController.CAR_INFO_CARAGE);
                    final int position = bundle.getInt(VolleyController.LIST_VIEW_POSITION);
                    CarInfoItem object = mAdapter.getData().get(position);
                    object.setName(name);
                    object.setCarType(carType);
                    object.setCarAge(carAge);
                    mAdapter.notifyDataSetChanged();
                    String t_device = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
                    Update_User(name,carType,carAge,t_device,position+1);
                }
                break;
        }

    }


    private String CreateUser(final String id, final String name, final String cartype, final String carage, final String device){
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~andersen/InsertUser.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), String.valueOf(response), Toast.LENGTH_SHORT).show();
                if(response.equals("success")){
                    Toast.makeText(getActivity(), "Add Success!", Toast.LENGTH_SHORT).show();


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
                return map;
            }
        };
        mQueue.add(stringRequest);

        return "";

    }

    private String Update_User(final String new_name, final String new_car_type, final String new_car_age, final String new_device_name, final int position){
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~andersen/UpdateUser.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), String.valueOf(response), Toast.LENGTH_SHORT).show();
                if(response.equals("success")){
                    Toast.makeText(getActivity(), "Add Success!", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getActivity(), "Add Fail!", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Add Fail", Toast.LENGTH_SHORT).show();
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                //Integer position = sharedPreferences.getInt("CarInfoNow", -1);

                String a = "Name" + position;
                String b = "CarType" + position;
                String c = "CarAge" + position;
                String d = "Device";
                String t_name = sharedPreferences.getString(a, null);
                String t_cartype = sharedPreferences.getString(b, null);
                String t_carage = sharedPreferences.getString(c, null);
                String t_device = sharedPreferences.getString(d, null);

                String t_id = sharedPreferences.getString("ID", null);

                editor.putString(a, new_name);
                editor.putString(b, new_car_type);
                editor.putString(c, new_car_age);
                editor.putString(d, new_device_name);
                editor.commit();

                map.put("ID", t_id );
                map.put("NAME", new_name);
                map.put("CAR_TYPE", new_car_type);
                map.put("CAR_AGE", new_car_age);
                map.put("DEVICE_NAME", new_device_name);
                map.put("OLD_NAME", t_name);
                map.put("OLD_CAR_TYPE", t_cartype);
                map.put("OLD_CAR_AGE", t_carage);
                map.put("OLD_DEVICE_NAME", t_device);
                return map;
            }
        };
        mQueue.add(stringRequest);

        return "";
    }


    void ShowCarInfo()
    {
        int Now_CarInfo = sharedPreferences.getInt("CarInfoNow",0);
        nameEditText = (EditText) getView().findViewById(R.id.name_data);
        carTypeEditText = (EditText) getView().findViewById(R.id.cartype_data);
        carAgeEditText = (EditText) getView().findViewById(R.id.carage_data);
        TextView device = (TextView) getView().findViewById(R.id.device_data);

        String Name = "";
        String CarType = "";
        String CarAge = "";
        String Device = "";

        String a = "Name" + Now_CarInfo;
        String b = "CarType" + Now_CarInfo;
        String c = "CarAge" + Now_CarInfo;
        String d = "Device";

        Name = sharedPreferences.getString(a, null);
        CarType = sharedPreferences.getString(b, null);
        CarAge = sharedPreferences.getString(c, null);
        //Device = sharedPreferences.getString(d, null);
        Device = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;

        Log.v("Tag", String.valueOf(Now_CarInfo));
        Log.v("Tag", Name);
        Log.v("Tag", CarType);
        Log.v("Tag", CarAge);
        Log.v("Tag", Device);

        if (CarType != null && CarAge != null && Name != null && Device != null) {
            carTypeEditText.setText(CarType);
            carTypeEditText.setTextSize(20);
            carAgeEditText.setText(CarAge);
            carAgeEditText.setTextSize(20);
            nameEditText.setText(Name);
            nameEditText.setTextSize(20);
            device.setText(Device);
            device.setTextSize(20);
        } else {
            Log.v("Tag", "Something wrong in profile");
        }

        Log.v("Tag", "Car info in progile" + Now_CarInfo);
    }



    void check_CarInfo()
    {
        String a = "Name1";
        String b = "CarType1";
        String c = "CarAge1";
        //String d = "device1";
        String t_name = sharedPreferences.getString(a,null);
        String t_cartype = sharedPreferences.getString(b,null);
        String t_carage = sharedPreferences.getString(c,null);
        //String t_device = sharedPreferences.getString(d,null);|| t_device ==null

        if(t_carage == null || t_cartype ==null || t_name ==null)
        {
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

                            } else{
                                int CarInfoCounter = sharedPreferences.getInt("CarInfoCounter", 0);
                                if (CarInfoCounter == 0) {
                                    Log.v("Tag", "Something wrong in main 1");
                                } else {
                                    CarInfoCounter++;
                                    String a = "Name1";
                                    String b = "CarType1";
                                    String c = "CarAge1";
                                    editor.putString(a, Pop_name);
                                    editor.putString(b, Pop_cartype);
                                    editor.putString(c, Pop_carage);
                                    editor.commit();

                                    String t_id = sharedPreferences.getString("ID", null);
                                    CreateUser(t_id, Pop_name, Pop_cartype, Pop_carage, android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
                                    CarInfoItem newCarInfo = new CarInfoItem(Pop_name,Pop_cartype,Pop_carage);
                                    CarInfoItemList.add(newCarInfo);
                                    mAdapter.notifyDataSetChanged();

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

        }
    }



    public void kill_all(File dir)
    {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    kill_all(file);
                    file.delete();
                } else {
                    // do something here with the file
                    file.delete();
                }
            }
        }
    }
}
