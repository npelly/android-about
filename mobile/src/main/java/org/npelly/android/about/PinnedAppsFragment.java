package org.npelly.android.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.TextManager;


public class PinnedAppsFragment extends Fragment implements TextManager.Callback {

    // newInstance constructor for creating fragment with arguments
    public static PinnedAppsFragment newInstance() {
        PinnedAppsFragment fragmentFirst = new PinnedAppsFragment();
        return fragmentFirst;
    }

    public static String getPageTitle() {
        return "Pinned Packages";
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        About.logd("PinnedAppsFragment onCreate()");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        About.logd("PinnedAppsFragment onCreateView()");


        View view = inflater.inflate(R.layout.pinned_apps_fragment, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        About.logd("PinnedAppsFragment onResume()");

        About.get().getTextManager().addCallback(this);
        onTextChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        About.logd("PinnedAppsFragment onPause()");

        About.get().getTextManager().removeCallback(this);
    }


    /**
     * Called when text content needs to be updated
     */
    @Override
    public void onTextChanged() {
        About.logd("PinnedAppsFragment onTextChanged()");

        TextView textView = (TextView) getView().findViewById(R.id.pinned_apps_text);

        if (textView != null) {
            textView.setText(About.get().getTextManager().getActivityText());
        }
    }
}
