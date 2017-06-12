package com.example.paul.all_sensor_logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.fragment;

/**
 * Created by Andersen on 2016/12/8.
 */

public class EditProfile extends Activity implements View.OnClickListener{
    private int pos;
    private EditText nameEditText ;
    private EditText carTypeEditText ;
    private EditText carAgeEditText ;
    private Button cancelBtn;
    private Button sendBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        initView();
    }


    void initView()
    {
        nameEditText = (EditText) findViewById(R.id.name_data);
        carTypeEditText = (EditText) findViewById(R.id.cartype_data);
        carAgeEditText = (EditText) findViewById(R.id.carage_data);
        TextView device = (TextView) findViewById(R.id.device_data);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        sendBtn = (Button) findViewById(R.id.btn_send_profile);
        cancelBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final String Name = bundle.getString(VolleyController.CAR_INFO_NAME);
        final String CarType = bundle.getString(VolleyController.CAR_INFO_CARTYPE);
        final String CarAge = bundle.getString(VolleyController.CAR_INFO_CARAGE);

        String Device = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;


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

        pos = bundle.getInt(VolleyController.LIST_VIEW_POSITION);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_send_profile:
                if(NetworkCheck.isNetworkConnected(this)) {
                    final Intent intent = getIntent();
                    final Bundle bundle = new Bundle();
                    final String new_name = nameEditText.getText().toString();
                    final String new_car_type= carTypeEditText.getText().toString();
                    final String new_car_age = carAgeEditText.getText().toString();
                    bundle.putString(VolleyController.CAR_INFO_NAME, new_name);
                    bundle.putString(VolleyController.CAR_INFO_CARTYPE, new_car_type);
                    bundle.putString(VolleyController.CAR_INFO_CARAGE, new_car_age);
                    int position = pos;
                    bundle.putInt(VolleyController.LIST_VIEW_POSITION, position);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else
                    Toast.makeText(this, "No internet, can't edit setting", Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }

}
