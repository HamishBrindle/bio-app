package com.biomap.application.bio_app.Login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.biomap.application.bio_app.R;
import com.biomap.application.bio_app.Utility.SplashActivity;

public class BeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);

        Button mBegin = (Button) findViewById(R.id.button_begin);
        final Intent splashIntent = new Intent(this, SplashActivity.class);

        mBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(splashIntent);
                finish();
            }
        });

    }
}
