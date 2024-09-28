package com.example.stepappv1;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private int count = 0;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.show_count);
    }

    public void startCount(View view){
        if(count == 0){
            Toast toast = Toast.makeText(this, R.string.counterStarted, Toast.LENGTH_LONG);
            toast.show();
        }else {
            count = 0;
            tv.setText(String.format("%d", count));
            Toast toast = Toast.makeText(this, R.string.counterReset, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void incrementCount(View view){
        count++;
        tv.setText(String.format("%d", count));
        Toast toast = Toast.makeText(this, R.string.counterIncremented, Toast.LENGTH_LONG);
        toast.show();
    }
}