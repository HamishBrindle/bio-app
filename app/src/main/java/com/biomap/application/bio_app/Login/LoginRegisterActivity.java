package com.biomap.application.bio_app.Login;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.biomap.application.bio_app.R;

public class LoginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        TextView mDescriptionText = (TextView) findViewById(R.id.login_register_description_text);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");
        mDescriptionText.setTypeface(font);
    }
}
