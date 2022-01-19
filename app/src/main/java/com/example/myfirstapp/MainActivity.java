package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.configuration.SessionConfiguration;
import com.snowplowanalytics.snowplow.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.network.HttpMethod;

import com.snowplowanalytics.snowplow.event.ScreenView;

import com.snowplowanalytics.snowplow.controller.TrackerController;
import com.snowplowanalytics.snowplow.network.RequestCallback;
import com.snowplowanalytics.snowplow.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.LoggerDelegate;
import com.snowplowanalytics.snowplow.util.TimeMeasure;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements LoggerDelegate {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkConfiguration networkConfig = new NetworkConfiguration("http://10.0.2.2:9090", HttpMethod.POST);
        EmitterConfiguration emitterConfig = new EmitterConfiguration()
                .requestCallback(getRequestCallback())
                .threadPoolSize(20)
                .emitRange(500)
                .byteLimitPost(52000);

        TrackerConfiguration trackerConfig = new TrackerConfiguration("appId")
                .platformContext(false)
                .screenContext(false)
                .applicationContext(false)
                .deepLinkContext(false)
                .screenViewAutotracking(false)
                .lifecycleAutotracking(false)
                .installAutotracking(false)
                .exceptionAutotracking(false);


        SessionConfiguration sessionConfig = new SessionConfiguration(
                new TimeMeasure(30, TimeUnit.SECONDS),
                new TimeMeasure(30, TimeUnit.SECONDS)
        );
        TrackerController tracker = Snowplow.createTracker(getApplicationContext(),
                "androidAppTracker",
                networkConfig,
                trackerConfig,
                sessionConfig,
                emitterConfig
        );

        ScreenView event = new ScreenView("MainActivity", UUID.randomUUID());

        tracker.track(event);
        Log.d("SP", "ScreenView Event");

    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void sendEvent(View view) {
        TrackerController tracker = Snowplow.getTracker("androidAppTracker");
        ScreenView event = new ScreenView("MainActivity", UUID.randomUUID());

        Map<String, String> contextProps = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
        contextProps.put("dateTime", sdf.format(new Date()));
        event.customContexts.add(
                new SelfDescribingJson("iglu:com.myvendor/example-custom-context/jsonschema/1-0-0",
                        contextProps
                )
        );
        tracker.track(event);
    }

    /**
     * Returns the Emitter Request Callback.
     */
    private RequestCallback getRequestCallback() {
        return new RequestCallback() {
            @Override
            public void onSuccess(int successCount) {
                Log.d("SP",
                        "Emitter Send Success:\n " +
                                "- Events sent: " + successCount + "\n"
                );
            }
            @Override
            public void onFailure(int successCount, int failureCount) {
                Log.e("SP", "Emitter Send Failure:\n " +
                        "- Events sent: " + successCount + "\n " +
                        "- Events failed: " + failureCount + "\n"
                );
            }
        };
    }

    @Override
    public void error(@NonNull String tag, @NonNull String msg) {
        Log.e("[$tag]", msg);
    }

    @Override
    public void debug(@NonNull String tag, @NonNull String msg) {
        Log.d("[$tag]", msg);
    }

    @Override
    public void verbose(@NonNull String tag, @NonNull String msg) {
        Log.v("[$tag]", msg);
    }
}