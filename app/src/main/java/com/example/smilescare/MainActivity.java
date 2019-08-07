package com.example.smilescare;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smilescare.Common.Common;
import com.example.smilescare.Model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 7171;
    Button btnContinue;
    EditText edtName;
    EditText edtMail;
    FirebaseDatabase database;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);

        btnContinue = findViewById(R.id.btn_continue);
        edtName = findViewById(R.id.edtName);
        edtMail = findViewById(R.id.edtMail);
        btnContinue = (Button)findViewById(R.id.btn_continue);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginSystem();
            }
        });

        if (AccountKit.getCurrentAccessToken()!=null){
            final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).build();
            waitingDialog.show();
            waitingDialog.setMessage("Please Wait");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User localUser = dataSnapshot.getValue(User.class);
                                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);
                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }
    }

    private void startLoginSystem() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError()!=null){
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if (result.wasCancelled()){
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                if (result.getAccessToken()!=null){
                    final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).build();
                    waitingDialog.show();
                    waitingDialog.setMessage("Please Wait");
                    waitingDialog.setCancelable(false);

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone = account.getPhoneNumber().toString();
                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userPhone).exists()){
                                                User newUser = new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName(edtName.getText().toString());
                                                newUser.setEmail(edtMail.getText().toString());

                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                    Toast.makeText(MainActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();

                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);
                                                                                waitingDialog.dismiss();
                                                                                finish();
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                            else {
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                waitingDialog.dismiss();
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}
