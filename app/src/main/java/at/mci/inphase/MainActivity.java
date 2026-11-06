package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDailyCheckin = findViewById(R.id.btnDailyCheckin);
        if (btnDailyCheckin != null) {
            btnDailyCheckin.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DailyReadinessActivity.class);
                startActivity(intent);
            });
        }
    }
}