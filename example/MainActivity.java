package com.test.pins1fre;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.keylo.pins1fre.PinS1fre;

/* Don't forget to add [compile 'com.tht.applocker:pins1fre:1.0:release@aar'] to your Gradle dependencies */


public class MainActivity extends AppCompatActivity {

    PinS1fre pinS1fre;
    Button btnDelPass, btnSetPass, btnChangePass;

    private void init(){
        pinS1fre = new PinS1fre(getApplicationContext());
        btnDelPass = (Button) findViewById(R.id.btnDelPass);
        btnSetPass = (Button) findViewById(R.id.btnSetPass);
        btnChangePass = (Button) findViewById(R.id.btnChangePass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        if(pinS1fre.haveLock()){
            pinS1fre.showLock(MainActivity.this, new PinS1fre.PasscodeEvent() {
                @Override
                public void onCorrectPass() {
                    Toast.makeText(MainActivity.this, "Giris yapildi.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onWrongPass() {
                    Toast.makeText(MainActivity.this, "Tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnDelPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pinS1fre.haveLock()){
                    pinS1fre.delLock(MainActivity.this);
                }
            }
        });

        btnSetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinS1fre.setLock(MainActivity.this);
            }
        });

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pinS1fre.haveLock()){
                    pinS1fre.changeLock(MainActivity.this);
                }
            }
        });

    }
}
