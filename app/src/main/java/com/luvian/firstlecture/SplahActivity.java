package com.luvian.firstlecture;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Thread.sleep;

public class SplahActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        //setContentContentView는 하지 않습니다.
        startActivity(new Intent(this, MainActivity.class));
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
    }


}
