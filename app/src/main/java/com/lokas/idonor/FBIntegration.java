package com.lokas.idonor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Bala on 21-09-2016.
 */
public class FBIntegration extends AppCompatActivity {

    CallbackManager callbackManager;
    Button share, details;
    ShareDialog shareDialog;
    LoginButton login;
    ProfilePictureView profile;
    Dialog details_dialog;
    TextView details_txt;

    // GPSTracker class
    GPSTracker gps;
    SessionManager manager;
    private static String custkid;
    double latitude = 0;
    double longitude = 0;
    MyAsyncTask121  MYC = null;
    DataHelper1 dh;
    DataBaseHelper myDbHelper;

    ArrayList<String> CUSTOMER_LIST = new ArrayList<String>();
    ArrayList<String> CusId = new ArrayList<String>();
    ArrayList<String> CusIds = new ArrayList<String>();
    ArrayList<String> CusUserId = new ArrayList<String>();
    ArrayList<String> Name = new ArrayList<String>();
    ArrayList<String> Email = new ArrayList<String>();
    ArrayList<String> Phone = new ArrayList<String>();
    ArrayList<String> CusPwd = new ArrayList<String>();
    ArrayList<String> CusCPwd = new ArrayList<String>();
    ArrayList<String> CusRole = new ArrayList<String>();
    ArrayList<String> CreatedDate = new ArrayList<String>();
    ArrayList<String> ModiDate = new ArrayList<String>();
    ArrayList<String> Status = new ArrayList<String>();
    ArrayList<String> Flag = new ArrayList<String>();
    ArrayList<String> NGOLink = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.fb_main);

        manager = new SessionManager();
        String status = manager.getPreferences(FBIntegration.this, "tkID");
        Log.d("status", status);
        custkid = status;
        Toast.makeText(getApplicationContext(), custkid, Toast.LENGTH_LONG).show();

        myDbHelper = new DataBaseHelper(getApplicationContext());

        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){

            throw sqle;

        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarm);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("FaceBook Login");
        actionBar.setDisplayHomeAsUpEnabled(true);

        gps = new GPSTracker(FBIntegration.this);

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        callbackManager = CallbackManager.Factory.create();
        login = (LoginButton) findViewById(R.id.login_button);
        profile = (ProfilePictureView) findViewById(R.id.picture);
        shareDialog = new ShareDialog(this);
        share = (Button) findViewById(R.id.share);
        details = (Button) findViewById(R.id.details);
        login.setReadPermissions("public_profile email");
        share.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        details_dialog = new Dialog(this);
        details_dialog.setContentView(R.layout.fb_dialog);
        details_dialog.setTitle("Details");
        details_txt = (TextView) details_dialog.findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                details_dialog.show();
            }
        });

        if (AccessToken.getCurrentAccessToken() != null) {
            RequestData();
            //share.setVisibility(View.VISIBLE);
            //details.setVisibility(View.VISIBLE);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    share.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    profile.setProfileId(null);
                }
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareLinkContent content = new ShareLinkContent.Builder().build();
                shareDialog.show(content);

            }
        });
        login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                if (AccessToken.getCurrentAccessToken() != null) {
                    RequestData();
                    //share.setVisibility(View.VISIBLE);
                    //details.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception) {
            }
        });

    }

    public void RequestData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                JSONObject json = response.getJSONObject();
                System.out.println(json);
                try {
                    if (json != null) {
                        String fbname = json.getString("name");
                        String fbemail = json.getString("email");
                        String fbrole = "Non-NGO";
                        String text = "<b>Name :</b> " + json.getString("name") + "<br><br><b>Email :</b> " + json.getString("email") + "<br><br><b>Profile link :</b> " + json.getString("link");
                        details_txt.setText(Html.fromHtml(text));
                        profile.setProfileId(json.getString("id"));
                        new FBSignup(fbname, fbemail, latitude, longitude, fbrole, custkid).execute();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private class FBSignup extends AsyncTask<Void, Void, String> {

        private ProgressDialog progressDialog = new ProgressDialog(FBIntegration.this);
        InputStream inputStream = null;
        String result = "";
        String result1 = "";
        String Resp = "";
        String mess = "";
        String regfbName, regfbEmail, regfbRole, tkId;
        double lati, longi;


        public FBSignup(String fbname, String fbemail, double latitude, double longitude, String fbrole, String ctkid) {
            this.regfbName = fbname;
            this.regfbEmail = fbemail;
            this.lati = latitude;
            this.longi = longitude;
            this.regfbRole = fbrole;
            this.tkId = ctkid;
        }


        protected void onPreExecute() {
            progressDialog.setMessage("Sending  the Request...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    FBIntegration.FBSignup.this.cancel(true);
                }
            });
        }


        protected String doInBackground(Void... params) {

            String value1 = "";
            String encodedURL = "";
            //String url_select = "http://182.18.160.121/IbharAppointment/api/APLOCAs";

            String url_select = "http://lokas.in/ngoapp/customer_fbinsert.php";


            long timestamp = System.currentTimeMillis() / 1000L;

            String HMAC_SHA1_ALGORITHM = "HmacSHA1";
            String Unique = UUID.randomUUID().toString();
            String Appid = "4d53bce03ec34c0a911182d4c228ee6c";
            String Method = "POST";
            String Time = String.valueOf(timestamp);
            String key = "A93reRTUJHsCuQSHR+L3GxqOJyDmQpCgps102ciuabc=";
            String format = "UTF-8";


            try {
                encodedURL = URLEncoder.encode(url_select, "UTF-8").toLowerCase();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            String BodyEncrpt = "";
            // BodyEncrpt=getMd5Hash(jsonobj.toString());
            String stringToSign = "";
            stringToSign = Appid + Method + encodedURL + Time + Unique + BodyEncrpt;

            try {


                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                param.clear();

                ArrayList<String> stringData = new ArrayList<String>();
                DefaultHttpClient httpClient = new DefaultHttpClient();
                ResponseHandler<String> resonseHandler = new BasicResponseHandler();
                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setHeader("Content-Type", "application/json");
                //httpPost.setEntity(new ByteArrayEntity(jsonobj.toString().getBytes("UTF8")));
                httpPost.setHeader("Authorization", "amx " + Appid + ":" + result1 + ":" + Unique + ":" + Time);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                Resp = httpResponse.getStatusLine().toString();

                mess = "";


                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();

                //}


            } catch (UnsupportedEncodingException e1) {
                Log.e("UnsupportedEncodin", e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }

            System.out.print(regfbName + " " + regfbEmail + " " + String.valueOf(lati) + " " + String.valueOf(longi) + " " + regfbRole
                    + " " + tkId);
            HashMap<String, String> data = new HashMap<String, String>();
            data.put("rfbname", regfbName);
            data.put("rfbemail", regfbEmail);
            data.put("rlati", String.valueOf(lati));
            data.put("rlongi", String.valueOf(longi));
            data.put("rfbrole", regfbRole);
            data.put("rtkid", tkId);

            try {
                //convert this HashMap to encodedUrl to send to php file
                String dataToSend = hashMapToUrl(data);
                //make a Http request and send data to saveImage.php file
                String response = Request.post(url_select, dataToSend);

                //return the response
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                //Log.e(TAG,"ERROR  "+e);
                return null;
            }


        }


        protected void onPostExecute(String v) {


            super.onPostExecute(v);
            manager = new SessionManager();
            //Toast.makeText(getApplicationContext(),v,Toast.LENGTH_LONG).show();
            System.out.println("Postvalue" + v);
            String s = v.trim();
            if (s.equalsIgnoreCase("null")) {
                Toast.makeText(getApplicationContext(), "Invalid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Successfully Registered FB", Toast.LENGTH_LONG).show();
                manager.setPreferences(FBIntegration.this, "status", "1");
                manager.setPreferences(FBIntegration.this, "cusID", v);


                String status = manager.getPreferences(FBIntegration.this, "status");
                String cstatus = manager.getPreferences(FBIntegration.this, "cusID");
                Log.d("status fbfbfb", status);
                MYC= new MyAsyncTask121();
                MYC.execute();
                //Toast.makeText(getApplicationContext(), cstatus, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(FBIntegration.this, Dash.class);
                startActivity(intent);
            }

            /*int jsonResult = returnParsedJsonObject(v);

            if(jsonResult == 0){
                //Toast.makeText(getApplicationContext(),v,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),"Phone number or email already exist",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(),"Successfully Registered",Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(FBIntegration.this, MainActivity.class);
                startActivity(loginIntent);
            }*/


            this.progressDialog.dismiss();

        }


    }


    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    private int returnParsedJsonObject(String result) {

        JSONObject resultObject = null;

        int returnedResult = 0;

        try {

            resultObject = new JSONObject(result);

            returnedResult = resultObject.getInt("success");

            //Toast.makeText(getApplicationContext(),returnedResult,Toast.LENGTH_LONG).show();
        } catch (JSONException e) {

            e.printStackTrace();

        }
        Log.d("jsonreturn", String.valueOf(returnedResult));
        return returnedResult;

    }


    class MyAsyncTask121 extends AsyncTask<String,String,Void> {

        private ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
        InputStream inputStream = null;
        String result = "";
        String result1 = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Refreshing  data...");
            //progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyAsyncTask121.this.cancel(true);
                }
            });

            //PB.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... params) {

            String value1 = "";	 String encodedURL="";

            String url_select ="";



            url_select = "http://lokas.in/ngoapp/customer_get.php";





            try {
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


            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            try {
                // Set up HTTP post
                try {

                    result1 = getHmacMD5(key, stringToSign);
                }
                catch(Exception e)
                {

                }

                DefaultHttpClient httpClient = new DefaultHttpClient();

                //HttpPost httpPost = new HttpPost(url_select);
                HttpGet httpget = new HttpGet(url_select);
                //httpget.setHeader("Authorization","amx "+Appid+":"+result1+":"+Unique+":"+Time);
                //httpPost.setEntity(new UrlEncodedFormEntity(param));
                httpget.setHeader("Authorization","amx "+Appid+":"+result1+":"+Unique+":"+Time);
                HttpResponse httpResponse = httpClient.execute(httpget);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();
            } catch (UnsupportedEncodingException e1) {
                Log.e("UnsupportedEncodingExc", e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("StringBuildi & Buffer", "Error converting result " + e.toString());
            }


            try
            {

                if(result != null) {

                    String data = result;
                    Object json = new JSONTokener(data).nextValue();
                    if (json instanceof JSONObject)
                    {
                        Log.i("JSON", "is OBJECT");
                    }
                    //you have an object
                    else if (json instanceof JSONArray)
                    {
                        Log.i("JSON", "is Array");

                    }

                }

            }
            catch (Exception e)
            {
                Log.e("Finson obj or array", "Error  " + e.toString());
            }

            return null;

        } // protected Void doInBackground(String... params)


        public String getHmacMD5(String privateKey, String input) throws Exception{
            String algorithm = "HmacSHA256";
            String Ret="";
            byte[] keyBytes = Base64.decode(privateKey, Base64.NO_WRAP);
            Key key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);



            try {


                byte[] bytes = mac.doFinal(input.getBytes("UTF-8"));

                Ret=Base64.encodeToString(bytes,Base64.URL_SAFE|Base64.NO_WRAP).replace('-','+').replace('_', '/');;

            }
            catch(Exception e)
            {

            }
            return Ret;
        }




        protected void onPostExecute(Void v) {
            //parse JSON data


            try {


                JSONArray jArray = new JSONArray(result);
                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    System.out.println(jObject);
                    Log.d("data", String.valueOf(jObject));

                    Iterator iterator = jObject.keys();

                    String CICUSID = jObject.getString("cus_id");
                    String CICUSUSERID = jObject.getString("cus_userId");
                    String CICUSNAME = jObject.getString("cus_name");
                    String CICUSEMAIL = jObject.getString("cus_email");
                    String CICUSPHONE = jObject.getString("cus_phone");
                    String CICUSPWD = jObject.getString("cus_pwd");
                    String CICUSCPWD = jObject.getString("cus_cpwd");
                    String CICUSROLE = jObject.getString("cus_role");
                    String CICUSCRTDDATE = jObject.getString("crtd_date");
                    String CICUSMODIIDATE = jObject.getString("modi_date");
                    String CICUSSTATUS = jObject.getString("status");
                    String CICUSFLAG = jObject.getString("flag");
                    String CICUSNGOLINK = jObject.getString("ngo_link");





                    String QUERY="";

                    myDbHelper.ExecStatement("DELETE FROM customer WHERE cus_id='" + CICUSID + "'");

                    QUERY="INSERT INTO customer (cus_id,cus_userId,cus_name,cus_email,cus_phone,cus_pwd,cus_cpwd,cus_role,crtd_date,modi_date,status,flag,ngo_link) " + "VALUES " +
                            "('"+CICUSID+"','"+CICUSUSERID+"','"+CICUSNAME+"','"+CICUSEMAIL+"','"+CICUSPHONE+"','"+CICUSPWD+"','"+CICUSCPWD+"','"+CICUSROLE+"','"+CICUSCRTDDATE+"','"+CICUSMODIIDATE+"','"+CICUSSTATUS+"','"+CICUSFLAG+"','"+CICUSNGOLINK+"')";
                    myDbHelper.ExecStatement(QUERY);




                   /* dh.ExecStatement("DELETE FROM EEREST WHERE RECODE='" + SRECODE + "'");

                    QUERY="INSERT INTO EEREST (RECODE,REREID,RENAME,READD1,READD2,READD3,RELDMK,RECITY,RESTAT,RESUBR,REPHN1,REPHN2,REPHN3,REWEBS,REMAIL,REPNAM,REPPHN,REPMAI,REMEMS,RELONG,RELATI,REPICT,REOFID,REOFD1,REOFD2,REOFD3,REOFIM,REOFVD,REOFBO,REOFST,REOFRE,RECRBY,RECRDT,REDNBY,REDNDT,RESYDT,RERCST,RELOCK) " + "VALUES ('"+SRECODE+"','"+SREREID +"','"+SRENAME+"','"+SREADD1+"','"+SREADD2+"','"+SREADD3+"','"+SRELDMK+"','"+SRECITY+"','"+SRESTAT+"','"+SRESUBR+"','"+SREPHN1+"','"+SREPHN2+"','"+SREPHN3+"','"+SREWEBS+"','"+SREMAIL+"','"+SREPNAM+"','"+SREPPHN+"','"+SREPMAI+"','"+SREMEMS+"','"+SRELONG+"','"+SRELATI+"','"+SREPICT+"','"+SREOFID+"','"+SREOFD1+"','"+SREOFD2+"','"+SREOFD3+"','"+SREOFIM+"','"+SREOFVD+"','"+SREOFBO+"','"+SREOFST+"','"+SREOFRE+"','"+SRECRBY+"','"+SRECRDT+"','"+SREDNBY+"','"+SREDNDT+"','"+SRESYDT+"','"+SRERCST+"','"+SRELOCK+"')";

                    dh.ExecStatement(QUERY);*/



                } // End Loop

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)







            try {
                CUSTOMER_LIST.clear();
                CusId.clear();
                CusIds.clear();
                CusUserId.clear();
                Name.clear();
                Email.clear();
                Phone.clear();
                CusPwd.clear();
                CusCPwd.clear();
                CusRole.clear();
                CreatedDate.clear();
                ModiDate.clear();
                Status.clear();
                Flag.clear();
                NGOLink.clear();

                /*CUSTOMER_LIST = dh
                        .selectList(
                                "Select cus_id,cus_name,cus_email,cus_phone,crtd_date,modi_date,status,flag from customer WHERE RLCRBY='"
                                        + usercode + "' ORDER BY RLRNAM", null, 8);*/
                CUSTOMER_LIST = myDbHelper
                        .selectList(
                                "Select * from customer ORDER BY cus_id", null, 13);
                for (Iterator<String> i = CUSTOMER_LIST.iterator(); i.hasNext();) {
                    String rowValue = (String) i.next();
                    String[] parser = rowValue.split("%");
                    CusId.add(parser[0].trim().replace("null", ""));
                    CusIds.add(parser[0].trim().replace("null", ""));
                    // AUDIT2.add(parser[1].trim().replace("null", ""));
                    CusUserId.add(parser[1].trim().replace("null", ""));
                    Name.add(parser[2].trim().replace("null", ""));
                    Email.add(parser[3].trim().replace("null", ""));
                    Phone.add(parser[4].trim().replace("null", ""));
                    CusPwd.add(parser[5].trim().replace("null", ""));
                    CusCPwd.add(parser[6].trim().replace("null", ""));
                    CusRole.add(parser[7].trim().replace("null", ""));
                    CreatedDate.add(parser[8].trim().replace("null", ""));
                    ModiDate.add(parser[9].trim().replace("null", ""));
                    Status.add(parser[10].trim().replace("null", ""));
                    Flag.add(parser[11].trim().replace("null", ""));
                    NGOLink.add(parser[12].trim().replace("null", ""));

                    /*String Date ="";
                    if(parser[7].trim().replace("null", "").length()==10)
                    {
                        Date=parser[7].trim().replace("null", "").substring(0, 4)+parser[7].trim().replace("null", "").substring(5, 7)+parser[7].trim().replace("null", "").substring(8, 10);
                    }

                    dateofbirth.add(da.getDateFormat(Date));*/

                }
            } catch (Exception e) {
                String error = e.toString().trim();

                myDbHelper.Toastinfo(getApplicationContext(), error);
            }


        }




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
