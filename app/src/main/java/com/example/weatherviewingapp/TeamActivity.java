package com.example.weatherviewingapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TeamActivity - Hiển thị thông tin nhóm phát triển và giới thiệu ứng dụng
 */
public class TeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        
        // Thiết lập ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông Tin");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
