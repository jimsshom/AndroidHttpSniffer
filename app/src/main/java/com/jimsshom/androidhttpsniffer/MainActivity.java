package com.jimsshom.androidhttpsniffer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartProxy(View view) {
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), "com.jimsshom.androidhttpsniffer.ProxyService");
        startService(intent);
    }

    public void onOpenBaidu(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
        startActivity(i);
    }
}
