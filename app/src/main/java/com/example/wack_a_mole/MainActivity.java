package com.example.wack_a_mole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //hides top-menu

        ImageButton button1 = findViewById(R.id.startbuttonpixel);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, gameActivity.class);
                startActivity(intent);
            }
        });
        ImageButton button2 = findViewById(R.id.scoreboardbutton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, scoreboardActivity.class);
                startActivity(intent);
            }
        });
        ImageView gifImageView = findViewById(R.id.imageView2);
        Glide.with(this)
                .load(R.drawable.molebig)
                .into(gifImageView);

        ImageView gifImageView3 = findViewById(R.id.imageView5);
        Glide.with(this)
                .load(R.drawable.cloudstrimmed)
                .into(gifImageView3);

    }

}