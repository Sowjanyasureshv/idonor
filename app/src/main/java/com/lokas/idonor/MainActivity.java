package com.lokas.idonor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Creating a broadcast receiver for gcm registration
    BroadcastReceiver mRegistrationBroadcastReceiver;

    SessionManager manager;
    Button btnSignup,btnLogin;
    EditText email,pwd;
    String getEmail,getPwd;
    LoginTask  MY = null;
    public static final String USER_NAME = "USERNAME";
    private Context context;

    // GPSTracker class
    GPSTracker gps;

    //SharedPreferences sharedpreferences;
   // public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        email = (EditText) findViewById(R.id.enter_email);
        pwd = (EditText) findViewById(R.id.enter_pwd);

        btnLogin = (Button) findViewById(R.id.login);
        btnSignup = (Button) findViewById(R.id.signup);



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userEmail = email.getText().toString().trim();
                String userPwd = pwd.getText().toString().trim();

                if(userEmail.length()==0){
                    email.setError("Email id is required");
                    email.requestFocus();
                }else if(userPwd.length()==0){
                    pwd.setError("Password is required");
                    pwd.requestFocus();
                }else{
                    MY= new LoginTask();
                    MY.execute(userEmail, userPwd);
                }

                /*if(email.getText().toString().equals("admin") && pwd.getText().toString().equals("admin")){
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                    Intent dashInt = new Intent(MainActivity.this,Dash.class);
                    startActivity(dashInt);
                }else{
                    Toast.makeText(getApplicationContext(),"Invalid Login",Toast.LENGTH_LONG).show();
                }

                Log.d("EdEmail",email.getText().toString());*/
            }
        });
        gps = new GPSTracker(MainActivity.this);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try {
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                }

                try {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception ex) {
                }

                if(!gps_enabled && !network_enabled) {
                    // notify user
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                    // Setting Dialog Title
                    alertDialog.setTitle("GPS is settings");

                    // Setting Dialog Message
                    alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

                    // On pressing Settings button
                    alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            MainActivity.this.startActivity(intent);
                        }
                    });

                    // on pressing cancel button
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();
                }else {

                    Intent regIN = new Intent(MainActivity.this, Register.class);
                    //startActivity(regIN);
                    regIN.putExtra("process", 2);
                    startActivityForResult(regIN, 1);

                }
            }
        });

        if (!isNetworkAvailable()){
            //Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setMessage("Check Your Internet Connection");

            alertDialogBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });

            alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

       /* if (isNetworkStatusAvailable(getApplicationContext())) {
            //Toast.makeText(getActivity(), "Internet Available", Toast.LENGTH_LONG).show();

        } else {
            //Toast.makeText(getActivity(), "Internet not available", Toast.LENGTH_LONG).show();

            Log.d("NO Internet","ERRRR");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
            alertDialogBuilder.setMessage("Check Your Internet Connection");

            alertDialogBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });

            alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }*/



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if (resultCode == RESULT_OK) {
        Log.e("hfh", "OnActivityResult");
        Intent refresh = new Intent(this, Register.class);
        startActivity(refresh);
        //this.finish();
        // }
    }
    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(MainActivity.this);
    }
    class LoginTask extends AsyncTask<String,Void,String> {


        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        //InputStream inputStream = null;
        String result = "";
        String result1 = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Processing.....");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    LoginTask.this.cancel(true);
                }
            });

            //PB.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(String... params) {
            manager = new SessionManager();
            String statustk=manager.getPreferences(MainActivity.this,"tkID");
            //Log.d("status", status);
            final String custkid= statustk;
            //Toast.makeText(getApplicationContext(),custkid, Toast.LENGTH_LONG).show();

            String value1 = "";
            String encodedURL = "";

            String url_select = "http://lokas.co.in/ngoapp/customer_login.php";


            String uname = params[0];
            String pass = params[1];
            System.out.println(uname + "" + pass);
            InputStream is = null;
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("password", pass));
            nameValuePairs.add(new BasicNameValuePair("custknid", custkid));
            String result = null;






           /* try {
                encodedURL = URLEncoder.encode(url_select, "UTF-8").toLowerCase();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            long timestamp = System.currentTimeMillis() / 1000L;
            String Unique= UUID.randomUUID().toString();
            String Appid="4d53bce03ec34c0a911182d4c228ee6c";
            String Method="GET";
            String Time=String.valueOf(timestamp);
            String stringToSign=Appid+Method+encodedURL+Time+ Unique;
            String key="A93reRTUJHsCuQSHR+L3GxqOJyDmQpCgps102ciuabc=";
            String format="UTF-8";


            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();*/


                // Set up HTTP post
              /*  try {

                    result1 = getHmacMD5(key, stringToSign);
                }
                catch(Exception e)
                {

                }

                DefaultHttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url_select);
                //HttpGet httpget = new HttpGet(url_select);
                //httpget.setHeader("Authorization","amx "+Appid+":"+result1+":"+Unique+":"+Time);
                //httpPost.setEntity(new UrlEncodedFormEntity(param));
                //httpget.setHeader("Authorization","amx "+Appid+":"+result1+":"+Unique+":"+Time);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();*/

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url_select);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;

            }
        // protected Void doInBackground(String... params)





        protected void onPostExecute(String v) {

            manager = new SessionManager();
            //parse JSON data
            super.onPostExecute(v);
            String s = v.trim();
            //Toast.makeText(getApplicationContext(), v, Toast.LENGTH_LONG).show();
            System.out.println("Postvalue"+v);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            if(s.equalsIgnoreCase("null")){
                Toast.makeText(getApplicationContext(), "Invalid User Name or Password", Toast.LENGTH_LONG).show();
            }else{
                /*SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString("CusId", v);
                //editor.putString(Phone, ph);
                //editor.putString(Email, e);
                editor.commit();

                Intent intent = new Intent(MainActivity.this, Dash.class);
                //intent.putExtra(USER_NAME, una);
                //finish();
                startActivity(intent);*/

                manager.setPreferences(MainActivity.this, "status", "1");
                manager.setPreferences(MainActivity.this, "cusID", v);


                String status=manager.getPreferences(MainActivity.this,"status");
                String cstatus=manager.getPreferences(MainActivity.this,"cusID");
                Log.d("status", status);
                Toast.makeText(getApplicationContext(), cstatus, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, Dash.class);
                startActivity(intent);
            }
            /*if(s.equalsIgnoreCase("success")){
                Toast.makeText(getApplicationContext(), v, Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString("CusId", v);
                //editor.putString(Phone, ph);
                //editor.putString(Email, e);
                editor.commit();

                Intent intent = new Intent(MainActivity.this, Dash.class);
                //intent.putExtra(USER_NAME, una);
                //finish();
                startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(), "Invalid User Name or Password", Toast.LENGTH_LONG).show();
            }*/
            this.progressDialog.dismiss();
        }




    }

    /*public static boolean isNetworkStatusAvailable(Context contxt) {

        ConnectivityManager connectivityManager = (ConnectivityManager) contxt.getSystemService(contxt.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null){
                if (netInfos.isConnected()){
                    return true;
                }else {
                    Toast.makeText(contxt, "No Internet connection!", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(contxt, "No Internet connection111111!", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }*/
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check
        // if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

}
