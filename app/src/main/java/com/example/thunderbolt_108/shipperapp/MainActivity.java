package com.example.thunderbolt_108.shipperapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.thunderbolt_108.shipperapp.Common.Common;
import com.example.thunderbolt_108.shipperapp.Model.Shipper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    FButton btn_sign_in;
    MaterialEditText edt_phone,edt_password;

    FirebaseDatabase database;
    DatabaseReference shippers;

    private long backPressedTime;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Vahika.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        btn_sign_in=(FButton)findViewById(R.id.btnSignIn);
        edt_phone=(MaterialEditText)findViewById(R.id.edtPhone);
        edt_password=(MaterialEditText)findViewById(R.id.edtPassword);

        //Firebase
        database=FirebaseDatabase.getInstance();
        shippers=database.getReference(Common.SHIPPER_TABLE);

        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_phone.getText().toString().trim().length()==0 ||
                        edt_phone.getText().toString().trim().length()==0)
                {
                    Toast.makeText(MainActivity.this, "Please Enter UserName & Password !!!", Toast.LENGTH_SHORT).show();
                }
                else
                    login(edt_phone.getText().toString(),edt_password.getText().toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            return;
        }
        else {
            Toast.makeText(this, "Press Back Again To Exit !!!", Toast.LENGTH_SHORT).show();
        }
        backPressedTime=System.currentTimeMillis();
    }

    private void login(String phone, final String password) {
        final AlertDialog dialog=new SpotsDialog.Builder().setContext(MainActivity.this).build();
        dialog.show();
        dialog.setMessage("Please Wait . . .");
        dialog.setCancelable(false);
        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            dialog.dismiss();
                            Shipper shipper=dataSnapshot.getValue(Shipper.class);
                            if (shipper.getPassword().equals(password))
                            {
                                //Login Succeed
                                Toast.makeText(MainActivity.this, "User Logged In Successfully !!!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                Common.currentShipper=shipper;
                                finish();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Incorrect Password !!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Shpper Doesn't Exists !!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
