package com.example.paul.all_sensor_logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.example.paul.all_sensor_logger.bt.BTSerialPortCommunicationService;
import com.nullwire.trace.ExceptionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.nullwire.trace.ExceptionHandler.register;

public class LoginPage extends AppCompatActivity {
    //login page, this page should only be seen by uses that
    //  1.loged out 2.haven't logged in before 3.password change after last login
    // in other cases, user will be redircted to main page
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String result="";
    private Lock mLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = new Intent(this,NetworkCheckService.class);
        startService(intent);
        //intent = new Intent(this,BTSerialPortCommunicationService.class);
        //startService(intent);
        ExceptionHandler.register(this, "http://nol.cs.nctu.edu.tw/~pstsao/server.php");
        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME),0);
        editor=sharedPreferences.edit();

        if(NetworkCheck.isNetworkConnected(this)){

            String account=sharedPreferences.getString("account",null);
            String passwd=sharedPreferences.getString("passwd",null);
            if((account!=null)&&(passwd!=null))
            {
                //test auto login
                //login(account,passwd);
                Log.d("TAG","online ");
                Intent i = new Intent(getApplicationContext(), FakeLogin.class);
                startActivity(i);
                //if fail , wait for user login
            }
        }
        else{
            //test fake login
            if(offline_login())
            {
                Log.d("TAG","offline login");
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
            else
            {
                Log.d("TAG","offline ");
                Toast.makeText(getApplicationContext(), "please connect to network and try again" + result, Toast.LENGTH_LONG).show();
              //  ( (Button)findViewById(R.id.loginbtn)).setEnabled(false);

            }
        }

        // sPreference

           editor.putString("token" , "tokentest");
           editor.commit();
        //

        Auth0 auth0 = new Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain));
        mLock = Lock.newBuilder(auth0, mCallback)
                //Add parameters to the build
                .build(this);
        String idToken = sharedPreferences.getString(VolleyController.AUTH0_idToken,null);
        if(idToken == null)
        {
            startActivity(mLock.newIntent(this));
        }
        else
        {
            AuthenticationAPIClient aClient = new AuthenticationAPIClient(auth0);
            aClient.tokenInfo(idToken)
                    .start(new BaseCallback<UserProfile, AuthenticationException>() {
                        @Override
                        public void onSuccess(final UserProfile payload) {
                            API.get_token( new ResponseListener() {
                                public void onResponse(JSONObject response) {
                                    try {
                                        result = response.getString("result");
                                        if (result.equals("login succeed")) {
                                            //login success
                                            String token = response.getJSONObject("data").getString("token");
                                            editor.putString("token", token);
                                            editor.commit();

                                            //auto login
                                            LoginPage.this.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(LoginPage.this, "Automatic Login Success", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();

                                        } else {
                                            Toast.makeText(getApplicationContext(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Tag", "response error");
                                    Log.d("Tag", error.toString());
                                    Toast.makeText(getApplicationContext(), "Get Token fail, please login again", Toast.LENGTH_LONG).show();
                                    Intent intent_restart = new Intent();
                                    intent_restart.setClass(LoginPage.this,LoginPage.class);
                                    startActivity(intent_restart);
                                    finish();
                                    //((EditText) findViewById(R.id.passwd)).setText("");
                                }
                            });

                        }

                        @Override
                        public void onFailure(AuthenticationException error) {
                            LoginPage.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(LoginPage.this, "Session Expired, please Log In", Toast.LENGTH_SHORT).show();
                                }
                            });
                            editor.putString(VolleyController.AUTH0_idToken,null);
                            editor.commit();
                            startActivity(mLock.newIntent(LoginPage.this));
                        }
                    });
        }
    }

    boolean offline_login()
    {
        String account = sharedPreferences.getString("account",null);
        String passwd=sharedPreferences.getString("passwd",null);
        Log.d("Tag","fake login");
        return !((account==null)||(passwd==null));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        mLock.onDestroy(this);
        mLock = null;
    }

    private final LockCallback mCallback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(final Credentials credentials) {
            Toast.makeText(LoginPage.this, "Log In - Success", Toast.LENGTH_SHORT).show();

            API.get_token( new ResponseListener() {
                public void onResponse(JSONObject response) {
                    try {
                        result = response.getString("result");
                        if (result.equals("login succeed")) {
                            //login success
                            String token = response.getJSONObject("data").getString("token");
                            editor.putString("token", token);
                            editor.putString(VolleyController.AUTH0_idToken,credentials.getIdToken());
                            editor.commit();
                            VolleyController.getInstance().setUserCredentials(credentials);
                            startActivity(new Intent(LoginPage.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                public void onErrorResponse(VolleyError error) {
                    Log.d("Tag", "response error");
                    Log.d("Tag", error.toString());
                    Toast.makeText(getApplicationContext(), "Get Token fail, please login again", Toast.LENGTH_LONG).show();
                    Intent intent_restart = new Intent();
                    intent_restart.setClass(LoginPage.this,LoginPage.class);
                    startActivity(intent_restart);
                    finish();
                    //((EditText) findViewById(R.id.passwd)).setText("");
                }
            });





        }




        @Override
        public void onCanceled() {
            Toast.makeText(LoginPage.this, "Log In - Cancelled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(LockException error) {
            Toast.makeText(LoginPage.this, "Log In - Error Occurred", Toast.LENGTH_SHORT).show();
        }
    };
}
