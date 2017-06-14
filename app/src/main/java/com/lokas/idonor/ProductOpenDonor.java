package com.lokas.idonor;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
import java.util.Iterator;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductOpenDonor extends Fragment {


    DataHelper1 dh;
    //DataAccess da;
    SessionManager manager;
    RecyclerView r1;
    MyAsyncTask1  MY = null;
    MyAsyncTaskOBids  MYOB = null;
    DataBaseHelper myDbHelper;
    private String cusUID;
    ShareDialog shareDialog;

    ArrayList<String> Bids_List = new ArrayList<String>();
    ArrayList<String> BidId = new ArrayList<String>();
    ArrayList<String> CusId = new ArrayList<String>();
    // ArrayList<String> CusIds = new ArrayList<String>();
    ArrayList<String> ProdCusId = new ArrayList<String>();
    ArrayList<String> ProdUserID = new ArrayList<String>();
    ArrayList<String> CategId = new ArrayList<String>();
    // ArrayList<String> CreatedDate = new ArrayList<String>();
    // ArrayList<String> ModiDate = new ArrayList<String>();
    // ArrayList<String> Status = new ArrayList<String>();
    // ArrayList<String> Flag = new ArrayList<String>();
    ArrayList<String> ProdID = new ArrayList<String>();
    ArrayList<String> ProdSold = new ArrayList<String>();
    ArrayList<String> ProdAward = new ArrayList<String>();


    ArrayList<String> PRDS_List = new ArrayList<String>();
    ArrayList<String> PRDS_id = new ArrayList<String>();
    ArrayList<String> PRDS_ids = new ArrayList<String>();
    ArrayList<String> PRDS_name = new ArrayList<String>();
    ArrayList<String> PRDS_desc = new ArrayList<String>();
    ArrayList<String> PRDS_img = new ArrayList<String>();

    ArrayList<String> PRODUCT_LIST = new ArrayList<String>();
    ArrayList<String> ProdLISTID = new ArrayList<String>();
    ArrayList<String> CusIds = new ArrayList<String>();
    ArrayList<String> ProdId = new ArrayList<String>();
    ArrayList<String> CatID = new ArrayList<String>();
    ArrayList<String> ProdTitle = new ArrayList<String>();
    ArrayList<String> ProdDesc = new ArrayList<String>();
    ArrayList<String> ProdImg = new ArrayList<String>();
    ArrayList<String> CreatedDate = new ArrayList<String>();
    ArrayList<String> ModiDate = new ArrayList<String>();
    ArrayList<String> Status = new ArrayList<String>();
    ArrayList<String> Flag = new ArrayList<String>();
    ArrayList<String> BidsOpt = new ArrayList<String>();

    ArrayList<String> PRO_BIDS_LIST = new ArrayList<String>();
    ArrayList<String> PRO_BID_ID = new ArrayList<String>();
    ArrayList<String> PROCUSID = new ArrayList<String>();
    ArrayList<String> PROUSERID = new ArrayList<String>();


    public ProductOpenDonor() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*dh = new DataHelper1(getActivity());

        try {
            dh.openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        da = new DataAccess();*/

        FacebookSdk.sdkInitialize(getActivity());
        myDbHelper = new DataBaseHelper(getActivity());

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

        manager = new SessionManager();
        String result=manager.getPreferences(getActivity(),"cusID");
        cusUID= result.replaceAll("[^a-zA-Z0-9]+","");
        //Toast.makeText(getActivity(), cusUID, Toast.LENGTH_LONG).show();
        shareDialog = new ShareDialog(getActivity());

        View view = inflater.inflate(R.layout.fragment_product_open_donor, container, false);

        r1 = (RecyclerView) view.findViewById(R.id.opendnr);

        MY= new MyAsyncTask1();
        MY.execute();

        MYOB= new MyAsyncTaskOBids();
        MYOB.execute();

        // Inflate the layout for this fragment
        return view;
    }



    class MyAsyncTask1 extends AsyncTask<String,String,Void> {

        private ProgressDialog progressDialog = new ProgressDialog(getActivity());
        InputStream inputStream = null;
        String result = "";
        String result1 = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Loading  data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyAsyncTask1.this.cancel(true);
                }
            });

            //PB.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... params) {

            String value1 = "";	 String encodedURL="";

            String url_select ="";



            url_select = "http://lokas.in/ngoapp/product_user_get.php";





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

                    /*String BIDID = jObject.getString("pro_bid_id");
                    String CUSIDB = jObject.getString("cus_id");
                    String PRODCUSID = jObject.getString("pro_cus_id");
                    String PRODUSERID = jObject.getString("pro_user_id");
                    String CATEGID = jObject.getString("cat_id");
                    String BCRTDDATE = jObject.getString("crtd_date");
                    String BMODIDATE = jObject.getString("modi_date");
                    String BSTATUS = jObject.getString("status");
                    String BFLAG = jObject.getString("flag");
                    String PRODID = jObject.getString("pro_id");
                    String PRODSOLD = jObject.getString("pro_sold");
                    String PRODAWARD = jObject.getString("pro_awarded");*/

                    String PUID = jObject.getString("pro_user_id");
                    String CUSID = jObject.getString("cus_id");
                    String PID = jObject.getString("pro_id");
                    String CATID = jObject.getString("cat_id");
                    String PUTITLE = jObject.getString("pro_user_title");
                    String PUDESC = jObject.getString("pro_user_desc");
                    String PUIMG = jObject.getString("pro_user_img");
                    String PUCRTDDATE = jObject.getString("crdt");
                    String PUMODIDATE = jObject.getString("dndt");
                    String PUSTATUS = jObject.getString("status");
                    String PUFLAG = jObject.getString("flag");
                    String PUBIDS = jObject.getString("bids_opt");




                    String QUERY1="";
                    String QUERY="";

                   /* myDbHelper.ExecStatement("DELETE FROM product_bids WHERE pro_bid_id='" + BIDID + "'");

                    QUERY1="INSERT INTO product_bids (pro_bid_id,cus_id,pro_cus_id,pro_user_id,cat_id,crtd_date,modi_date,status,flag,pro_id,pro_sold,pro_awarded) " + "VALUES " +
                            "('"+BIDID+"','"+CUSIDB+"','"+PRODCUSID+"','"+PRODUSERID+"','"+CATEGID+"','"+BCRTDDATE+"','"+BMODIDATE+"','"+BSTATUS+"','"+BFLAG+"','"+PRODID+"','"+PRODSOLD+"','"+PRODAWARD+"')";
                    myDbHelper.ExecStatement(QUERY1);*/
                    myDbHelper.ExecStatement("DELETE FROM product_user WHERE pro_user_id='" + PUID + "'");

                    QUERY="INSERT INTO product_user(pro_user_id,cus_id,pro_id,cat_id,pro_user_title,pro_user_desc,pro_user_img,crdt,dndt,status,flag,bids_opt) " + "VALUES ('"+PUID+"','"+CUSID+"','"+PID+"','"+CATID+"','"+PUTITLE+"','"+PUDESC+"','"+PUIMG+"','"+PUCRTDDATE+"','"+PUMODIDATE+"','"+PUSTATUS+"','"+PUFLAG+"','"+PUBIDS+"')";

                    myDbHelper.ExecStatement(QUERY);




                   /* dh.ExecStatement("DELETE FROM EEREST WHERE RECODE='" + SRECODE + "'");

                    QUERY="INSERT INTO EEREST (RECODE,REREID,RENAME,READD1,READD2,READD3,RELDMK,RECITY,RESTAT,RESUBR,REPHN1,REPHN2,REPHN3,REWEBS,REMAIL,REPNAM,REPPHN,REPMAI,REMEMS,RELONG,RELATI,REPICT,REOFID,REOFD1,REOFD2,REOFD3,REOFIM,REOFVD,REOFBO,REOFST,REOFRE,RECRBY,RECRDT,REDNBY,REDNDT,RESYDT,RERCST,RELOCK) " + "VALUES ('"+SRECODE+"','"+SREREID +"','"+SRENAME+"','"+SREADD1+"','"+SREADD2+"','"+SREADD3+"','"+SRELDMK+"','"+SRECITY+"','"+SRESTAT+"','"+SRESUBR+"','"+SREPHN1+"','"+SREPHN2+"','"+SREPHN3+"','"+SREWEBS+"','"+SREMAIL+"','"+SREPNAM+"','"+SREPPHN+"','"+SREPMAI+"','"+SREMEMS+"','"+SRELONG+"','"+SRELATI+"','"+SREPICT+"','"+SREOFID+"','"+SREOFD1+"','"+SREOFD2+"','"+SREOFD3+"','"+SREOFIM+"','"+SREOFVD+"','"+SREOFBO+"','"+SREOFST+"','"+SREOFRE+"','"+SRECRBY+"','"+SRECRDT+"','"+SREDNBY+"','"+SREDNDT+"','"+SRESYDT+"','"+SRERCST+"','"+SRELOCK+"')";

                    dh.ExecStatement(QUERY);*/



                } // End Loop

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)


            final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            r1.setLayoutManager(layoutManager);
            //  r1.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            ///r1.setLayoutManager(new LinearLayoutManager(getActivity()));
            // r1.setItemAnimator(new FlipInBottomXAnimator());
            r1.setHasFixedSize(true);
            Myadapter adap = new Myadapter(getActivity());
            //  AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adap);
            //  ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
            r1.setAdapter(adap);
            //r1.setItemAnimator(new DefaultItemAnimator());


            Log.d("id", String.valueOf(cusUID));


            try {
                PRODUCT_LIST.clear();
                ProdLISTID.clear();
                CusIds.clear();
                ProdId.clear();
                CatID.clear();
                ProdTitle.clear();
                ProdDesc.clear();
                ProdImg.clear();
                CreatedDate.clear();
                ModiDate.clear();
                Status.clear();
                Flag.clear();
                BidsOpt.clear();

                /*CUSTOMER_LIST = dh
                        .selectList(
                                "Select cus_id,cus_name,cus_email,cus_phone,crtd_date,modi_date,status,flag from customer WHERE RLCRBY='"
                                        + usercode + "' ORDER BY RLRNAM", null, 8);*/
               PRODUCT_LIST = myDbHelper
                        .selectList(
                                "Select * from product_user where  cus_id='"+cusUID+"' and flag = 1 ORDER BY pro_user_id DESC", null, 12);
                for (Iterator<String> i = PRODUCT_LIST.iterator(); i.hasNext();) {
                    String rowValue = (String) i.next();
                    String[] parser = rowValue.split("%");
                    ProdLISTID.add(parser[0].trim().replace("null", ""));
                    CusIds.add(parser[1].trim().replace("null", ""));
                    ProdId.add(parser[2].trim().replace("null", ""));
                    CatID.add(parser[3].trim().replace("null", ""));
                    ProdTitle.add(parser[4].trim().replace("null", ""));
                    ProdDesc.add(parser[5].trim().replace("null", ""));
                    ProdImg.add(parser[6].trim().replace("null", ""));
                    CreatedDate.add(parser[7].trim().replace("null", ""));
                    ModiDate.add(parser[8].trim().replace("null", ""));
                    Status.add(parser[9].trim().replace("null", ""));
                    Flag.add(parser[10].trim().replace("null", ""));
                    BidsOpt.add(parser[11].trim().replace("null", ""));

                    /*String Date ="";
                    if(parser[7].trim().replace("null", "").length()==10)
                    {
                        Date=parser[7].trim().replace("null", "").substring(0, 4)+parser[7].trim().replace("null", "").substring(5, 7)+parser[7].trim().replace("null", "").substring(8, 10);
                    }

                    dateofbirth.add(da.getDateFormat(Date));*/

                }
            } catch (Exception e) {
                String error = e.toString().trim();

                myDbHelper.Toastinfo(getActivity(), error);
            }


            progressDialog.dismiss();
        }




    }

    class MyAsyncTaskOBids extends AsyncTask<String,String,Void> {

        private ProgressDialog progressDialog = new ProgressDialog(getActivity());
        InputStream inputStream = null;
        String result = "";
        String result1 = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Retrieving  data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyAsyncTaskOBids.this.cancel(true);
                }
            });

            //PB.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... params) {

            String value1 = "";	 String encodedURL="";

            String url_select ="";



            url_select = "http://lokas.in/ngoapp/product_bids_get.php";





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

                    String BIDID = jObject.getString("pro_bid_id");
                    String CUSID = jObject.getString("cus_id");
                    String PRODCUSID = jObject.getString("pro_cus_id");
                    String PRODUSERID = jObject.getString("pro_user_id");
                    String CATEGID = jObject.getString("cat_id");
                    String BCRTDDATE = jObject.getString("crtd_date");
                    String BMODIDATE = jObject.getString("modi_date");
                    String BSTATUS = jObject.getString("status");
                    String BFLAG = jObject.getString("flag");
                    String PRODID = jObject.getString("pro_id");
                    String PRODSOLD = jObject.getString("pro_sold");
                    String PRODAWARD = jObject.getString("pro_awarded");





                    String QUERY="";

                    myDbHelper.ExecStatement("DELETE FROM product_bids WHERE pro_bid_id='" + BIDID + "'");

                    QUERY="INSERT INTO product_bids (pro_bid_id,cus_id,pro_cus_id,pro_user_id,cat_id,crtd_date,modi_date,status,flag,pro_id,pro_sold,pro_awarded) " + "VALUES " +
                            "('"+BIDID+"','"+CUSID+"','"+PRODCUSID+"','"+PRODUSERID+"','"+CATEGID+"','"+BCRTDDATE+"','"+BMODIDATE+"','"+BSTATUS+"','"+BFLAG+"','"+PRODID+"','"+PRODSOLD+"','"+PRODAWARD+"')";
                    myDbHelper.ExecStatement(QUERY);




                   /* dh.ExecStatement("DELETE FROM EEREST WHERE RECODE='" + SRECODE + "'");

                    QUERY="INSERT INTO EEREST (RECODE,REREID,RENAME,READD1,READD2,READD3,RELDMK,RECITY,RESTAT,RESUBR,REPHN1,REPHN2,REPHN3,REWEBS,REMAIL,REPNAM,REPPHN,REPMAI,REMEMS,RELONG,RELATI,REPICT,REOFID,REOFD1,REOFD2,REOFD3,REOFIM,REOFVD,REOFBO,REOFST,REOFRE,RECRBY,RECRDT,REDNBY,REDNDT,RESYDT,RERCST,RELOCK) " + "VALUES ('"+SRECODE+"','"+SREREID +"','"+SRENAME+"','"+SREADD1+"','"+SREADD2+"','"+SREADD3+"','"+SRELDMK+"','"+SRECITY+"','"+SRESTAT+"','"+SRESUBR+"','"+SREPHN1+"','"+SREPHN2+"','"+SREPHN3+"','"+SREWEBS+"','"+SREMAIL+"','"+SREPNAM+"','"+SREPPHN+"','"+SREPMAI+"','"+SREMEMS+"','"+SRELONG+"','"+SRELATI+"','"+SREPICT+"','"+SREOFID+"','"+SREOFD1+"','"+SREOFD2+"','"+SREOFD3+"','"+SREOFIM+"','"+SREOFVD+"','"+SREOFBO+"','"+SREOFST+"','"+SREOFRE+"','"+SRECRBY+"','"+SRECRDT+"','"+SREDNBY+"','"+SREDNDT+"','"+SRESYDT+"','"+SRERCST+"','"+SRELOCK+"')";

                    dh.ExecStatement(QUERY);*/



                } // End Loop

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)


            progressDialog.dismiss();
        }




    }






    public class Myadapter extends RecyclerView.Adapter<Myadapter.ViewHolder> implements View.OnClickListener {

        Context context;

        public Myadapter(Context context) {

            this.context = context;
        }

        @Override
        public void onClick(View v) {
            int pos = getItemCount();

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_open,parent, false);
            ViewHolder VH1 = new ViewHolder(itemview);
            return VH1;
        }

        public void onBindViewHolder(ViewHolder holder, int position) {

            Log.d("Products",ProdLISTID.get(position));
            ///String prDetails = String.valueOf(myDbHelper.selectList("Select * from product_user where pro_user_id='"+ProdUserID.get(position)+"'ORDER BY pro_user_id", null, 11));
            // Log.d("Products",prDetails);
            Log.d("id1", String.valueOf(cusUID));
            /*String BIDIDD=myDbHelper.selectViewName("SELECT pro_user_id FROM product_bids where pro_cus_id='"+cusUID+"' and pro_user_id='"+ProdLISTID.get(position)+"'", null);
            if(BIDIDD==null)
            {
                BIDIDD="";
            }
            if(BIDIDD != ""){
                holder.prdbidimg.setImageDrawable(getResources().getDrawable(R.drawable.bidsopened));
            }

            Log.d("BidIdsss", String.valueOf(BIDIDD));*/
            try {
                PRO_BIDS_LIST.clear();
                PRO_BID_ID.clear();
                PROCUSID.clear();
                PROUSERID.clear();

                PRO_BIDS_LIST = myDbHelper
                        .selectList(
                                "Select * from product_bids where pro_cus_id='"+cusUID+"' and pro_user_id='"+ProdLISTID.get(position)+"'", null, 11);
                for (Iterator<String> i = PRO_BIDS_LIST.iterator(); i.hasNext();) {
                    String rowValue = (String) i.next();
                    String[] parser = rowValue.split("%");
                    PRO_BID_ID.add(parser[0].trim().replace("null", ""));
                    PROCUSID.add(parser[1].trim().replace("null", ""));
                    PROUSERID.add(parser[3].trim().replace("null", ""));
                    /*Log.d("prouserid", String.valueOf(PROUSERID));
                    if(String.valueOf(PROUSERID) !=""){
                        Log.d("prouserid1", String.valueOf(PROUSERID));
                        holder.Pname.setText(ProdTitle.get(position));
                        //holder.prdbidimg.setImageDrawable(getResources().getDrawable(R.drawable.bidsopened));
                    }*/
                    //Toast.makeText(getActivity(), (CharSequence) PRDS_name,Toast.LENGTH_LONG).show();


                }
            } catch (Exception e) {
                String error = e.toString().trim();

                myDbHelper.Toastinfo(getActivity(), error);
            }

            /*String strbidOpen = String.valueOf(PROUSERID);
            strbidOpen = strbidOpen.replaceAll("\\[", "").replaceAll("\\]","");
            if(strbidOpen != ""){
                Log.d("detopen", strbidOpen);
                if(ProdTitle.get(position) == strbidOpen){
                    holder.Pname.setAllCaps(true);
                    holder.Pname.setText(ProdTitle.get(position));
                }

            }*/

            Picasso.with(getActivity()).load("http://lokas.in/ngoapp/productImage/"+ProdImg.get(position)).placeholder(R.drawable.placeholderone).error(R.drawable.placeholderone).into(holder.primg);

            holder.Pname.setAllCaps(true);
            holder.Pname.setText(ProdTitle.get(position));
            Picasso.with(getActivity()).load("http://lokas.in/ngoapp/productImage/open/"+BidsOpt.get(position)).placeholder(R.drawable.placeholderone).into(holder.prdbidimg);
            /*Log.d("BGOpen", String.valueOf(BidsOpt.get(position)));
            if(BidsOpt.get(position).equalsIgnoreCase("1")){
               holder.prdbidimg.setImageDrawable(getResources().getDrawable(R.drawable.bidsopened));

            }*/

            /*String BGOpen=myDbHelper.selectViewName("Select pro_user_id from product_bids where pro_cus_id='"+cusUID+"' and pro_user_id='"+ProdLISTID.get(position)+"'", null);
            if(BGOpen==null)
            {
                BGOpen="";
            }
            Log.d("BGOpen", String.valueOf(BGOpen));
            if(String.valueOf(BGOpen) !=""){
                Log.d("BGOpen1", String.valueOf(BGOpen));
                Log.d("BGOpen2", ProdLISTID.get(position));
                if(ProdLISTID.get(position).equalsIgnoreCase(String.valueOf(BGOpen))) {
                    Log.d("BGOpen3", String.valueOf(BGOpen));
                    holder.Pname.setAllCaps(true);
                    holder.Pname.setText(ProdTitle.get(position));
                }
                *//*if(ProdLISTID.get(position) == String.valueOf(BGOpen)){
                    Log.d("BGOpen4", String.valueOf(BGOpen));
                    holder.Pname.setAllCaps(true);
                    holder.Pname.setText(ProdTitle.get(position));
                }*//*
            }*/

        }


        public class ViewHolder extends RecyclerView.ViewHolder  {
            TextView Pname;
            ImageView primg,prdbidimg,Pstat;


            public ViewHolder(final View View1) {
                super(View1);
                Pname = (TextView) View1.findViewById(R.id.openprd_name);
                Pstat =  (ImageView) View1.findViewById(R.id.openprd_stat);
                prdbidimg = (ImageView) View1.findViewById(R.id.openprd_bidimg);

                primg = (ImageView) View1.findViewById(R.id.openprd_img);


                Pstat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getActivity(), "test fb", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getActivity(), ProdImg.get(getPosition()), Toast.LENGTH_LONG).show();

                        ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentTitle("iDonor").setContentDescription(ProdTitle.get(getPosition()))
                                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.lokas.idonor&hl=en"))
                                .setImageUrl(Uri.parse("http://lokas.in/ngoapp/productImage/"+ProdImg.get(getPosition())))
                                .build();
                        shareDialog.show(linkContent);
                    }
                });

                View1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.d("ProductID",String.valueOf(ProdLISTID.get(getPosition())));
                        Log.d("Bidid",String.valueOf(PROUSERID));
                        String BID=myDbHelper.selectViewName("SELECT pro_user_id FROM product_bids WHERE pro_user_id='" + ProdLISTID.get(getPosition()) + "'", null);
                        if(BID==null)
                        {
                            BID="";

                        }
                        if(BID==""){
                            Toast.makeText(getActivity(), "No Bids Are Entered", Toast.LENGTH_LONG).show();
                        }else{
                            BidsCusList newFragment = new BidsCusList();
                            Bundle args = new Bundle();
                            args.putInt("prodID", Integer.parseInt(String.valueOf(ProdLISTID.get(getPosition()))));
                            newFragment.setArguments(args);
                            // consider using Java coding conventions (upper first char class names!!!)
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack
                            transaction.replace(R.id.frame_container, newFragment);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();
                        }
                        //Toast.makeText(getActivity(), BID+"value", Toast.LENGTH_LONG).show();

                        //Toast.makeText(getActivity(), String.valueOf(PRO_BID_ID.get(getPosition())), Toast.LENGTH_LONG).show();
                        /*Intent Cusprod = new Intent(getActivity(),BidsCusList.class);
                        Cusprod.putExtra("ProdId",String.valueOf(ProdUserID.get(getPosition())));
                        startActivity(Cusprod);*/
                        //if(ProdLISTID.get(getPosition()) != "") {
                            /*BidsCusList newFragment = new BidsCusList();
                            Bundle args = new Bundle();
                            args.putInt("prodID", Integer.parseInt(String.valueOf(ProdLISTID.get(getPosition()))));
                            newFragment.setArguments(args);
                            // consider using Java coding conventions (upper first char class names!!!)
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack
                            transaction.replace(R.id.frame_container, newFragment);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();*/
                        //   Toast.makeText(getActivity(), "No Bids Are Entered1", Toast.LENGTH_LONG).show();
                        //}else{

                        // }*/

                    }
                });


            }


        }

        @Override
        public int getItemCount() {
            return  ProdTitle.size();
        }
    }


}
