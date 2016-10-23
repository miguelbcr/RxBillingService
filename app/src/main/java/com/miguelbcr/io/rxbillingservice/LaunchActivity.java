package com.miguelbcr.io.rxbillingservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LaunchActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.launch_activity);

    findViewById(R.id.bt_activity).setOnClickListener(view -> {
      startActivity(new Intent(LaunchActivity.this, MainActivity.class));
    });

    findViewById(R.id.bt_fragment).setOnClickListener(view -> {
      startActivity(new Intent(LaunchActivity.this, HostActivityFragment.class));
    });
  }
}
