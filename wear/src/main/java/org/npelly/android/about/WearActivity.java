package org.npelly.android.about;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.PackageDetailManager;

public class WearActivity extends Activity implements PackageDetailManager.Callback {
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        About.logd("WearActivity onCreate()");
        About.get().getPackageDetailManager().addCallback(this);

        setContentView(R.layout.activity_wear);
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textview = (TextView) stub.findViewById(R.id.activity_text);
                textview.setText(About.get().getPackageDetailManager().getActivityText());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        About.logd("WearActivity onDestroy()");
        About.get().getPackageDetailManager().removeCallback(this);
    }

    @Override
    public void onTextChanged() {
        About.logd("WearActivity onTextChanged()");

        if (textview != null) {
            textview.setText(About.get().getPackageDetailManager().getActivityText());
        }
    }
}
