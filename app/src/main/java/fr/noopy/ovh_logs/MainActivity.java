package fr.noopy.ovh_logs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogFragment fragment = new LogFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }


}
