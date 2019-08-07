package com.example.smilescare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.view.View.VISIBLE;

public class ThankYou extends AppCompatActivity {

    RelativeLayout thankYouLayout;
    TextView txtOrderId;
    ImageButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        Intent intent = getIntent();

        thankYouLayout = findViewById(R.id.thankYouLayout);
        txtOrderId = findViewById(R.id.txtOrderId);
        btnContinue = findViewById(R.id.btnContinue);

        txtOrderId.setText("ORDER ID : "+intent.getStringExtra("OrderId"));
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ThankYou.this,Home.class);
                startActivity(intent1);
            }
        });

    }
}
