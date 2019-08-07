package com.example.smilescare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smilescare.Common.Common;
import com.example.smilescare.Model.Request;
import com.facebook.accountkit.AccountKit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.view.View.VISIBLE;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference donations;

    EditText edtAmount;
    EditText edtMessage;
    EditText edtVolunteerId;
    Spinner useSpinner;
    Button btnSubmit;
    RelativeLayout donationLayout;
    private static final int STORAGE_CODE = 1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        edtAmount = findViewById(R.id.edtAmount);
        edtMessage = findViewById(R.id.edtMessage);
        edtVolunteerId = findViewById(R.id.edtVolunteerId);
        useSpinner = findViewById(R.id.useSpinner);
        btnSubmit = findViewById(R.id.btnSubmit);
        donationLayout = findViewById(R.id.relative_layout);

        database = FirebaseDatabase.getInstance();
        donations = database.getReference("Donations");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Home.this,android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.choiceList));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        useSpinner.setAdapter(arrayAdapter);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(useSpinner.getSelectedItem().toString().equalsIgnoreCase("How can we use your donation?"))){
                    final String M_id = "ONJfgy18452202903072";
                    final String customer_id = Common.currentUser.getPhone().replace('+','@');
                    final String order_id = String.valueOf(System.currentTimeMillis());
                    String url = "https://sunproof-surge.000webhostapp.com/paytm/generateChecksum.php";
                    final String callBackUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
                    RequestQueue requestQueue = Volley.newRequestQueue(Home.this);
                    StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.has("CHECKSUMHASH")){
                                    String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                                    PaytmPGService paytmPGService = PaytmPGService.getStagingService();
                                    HashMap<String, String> paramMap = new HashMap<>();
                                    paramMap.put( "MID" , M_id);
                                    paramMap.put( "ORDER_ID" , order_id);
                                    paramMap.put( "CUST_ID" , customer_id);
                                    paramMap.put( "CHANNEL_ID" , "WAP");
                                    paramMap.put( "TXN_AMOUNT" , edtAmount.getText().toString());
                                    paramMap.put( "WEBSITE" , "WEBSTAGING");
                                    paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                                    paramMap.put( "CALLBACK_URL", callBackUrl);
                                    paramMap.put("CHECKSUMHASH",CHECKSUMHASH);

                                    PaytmOrder paytmOrder = new PaytmOrder(paramMap);

                                    paytmPGService.initialize(paytmOrder,null);
                                    paytmPGService.startPaymentTransaction(Home.this, true, true, new PaytmPaymentTransactionCallback() {
                                        @Override
                                        public void onTransactionResponse(Bundle inResponse) {
                                            //Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                                            if (inResponse.getString("STATUS").equals("TXN_SUCCESS")||inResponse.getString("STATUS").equals("PENDING")){
                                                Intent intent = new Intent(Home.this,ThankYou.class);
                                                intent.putExtra("OrderId", inResponse.getString("ORDERID"));
                                                startActivity(intent);
                                                Toast.makeText(Home.this, "Order Successfully placed!", Toast.LENGTH_SHORT).show();
                                                Request request = new Request(
                                                        Common.currentUser.getPhone(),
                                                        edtAmount.getText().toString(),
                                                        edtMessage.getText().toString(),
                                                        edtVolunteerId.getText().toString(),
                                                        useSpinner.getSelectedItem().toString(),
                                                        inResponse.getString("ORDERID"),
                                                        Common.currentUser.getName()
                                                );

                                                String order_number = inResponse.getString("ORDERID");
                                                donations.child(order_number)
                                                        .setValue(request);

                                                finish();
                                            }
                                        }

                                        @Override
                                        public void networkNotAvailable() {
                                            Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void clientAuthenticationFailed(String inErrorMessage) {
                                            Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage, Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void someUIErrorOccurred(String inErrorMessage) {
                                            Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                            Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onBackPressedCancelTransaction() {
                                            Toast.makeText(getApplicationContext(), "Transaction cancelled" , Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                            Toast.makeText(getApplicationContext(), "Transaction Cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Home.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> paramMap = new HashMap<>();
                            paramMap.put( "MID" , M_id);
                            paramMap.put( "ORDER_ID" , order_id);
                            paramMap.put( "CUST_ID" , customer_id);
                            paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                            paramMap.put( "CHANNEL_ID" , "WAP");
                            paramMap.put( "TXN_AMOUNT" , edtAmount.getText().toString());
                            paramMap.put( "WEBSITE" , "WEBSTAGING");
                            paramMap.put( "CALLBACK_URL", callBackUrl);
                            return paramMap;
                        }
                    };

                    requestQueue.add(stringRequest);
                }
            }
        });





        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            Intent history = new Intent(Home.this,History.class);
            startActivity(history);
        }  else if (id == R.id.nav_profile) {
            Intent profile = new Intent(Home.this,Profile.class);
            startActivity(profile);
        } else if (id == R.id.nav_log_out) {
            AccountKit.logOut();

            Intent signIn = new Intent(Home.this,MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
