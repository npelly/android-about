package org.npelly.android.about;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.TextManager;


public class AllAppsFragment extends Fragment implements View.OnClickListener, TextManager.Callback{

    // newInstance constructor for creating fragment with arguments
    public static AllAppsFragment newInstance() {
        AllAppsFragment fragmentFirst = new AllAppsFragment();
        return fragmentFirst;
    }

    public static String getPageTitle() {
        return "All Packages";
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        About.logd("AllAppsFragment onCreate()");

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        About.logd("AllAppsFragment onCreateView()");

        View view = inflater.inflate(R.layout.all_apps_fragment, container, false);

        ImageButton button = (ImageButton) view.findViewById(R.id.add_button);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        About.logd("AllAppsFragment onResume()");

        About.get().getTextManager().addCallback(this);
        onTextChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        About.logd("AllAppsFragment onPause()");

        About.get().getTextManager().removeCallback(this);
    }


    /**
     * Called when the add button is clicked
     */
    @Override
    public void onClick(View view) {
        About.logd("MobileActivity onClick()");

        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setTitle("Pin Package");
        final EditText input = new EditText(getView().getContext());

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextManager textManager = About.get().getTextManager();
                textManager.addPackage(input.getText().toString());
                textManager.generateText();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * Called when text content needs to be updated
     */
    @Override
    public void onTextChanged() {
        About.logd("AllAppsFragment onTextChanged()");

        TextView textView = (TextView) getView().findViewById(R.id.all_apps_text);

        if (textView != null) {
            textView.setText(About.get().getTextManager().getAllText());
        }
    }
}
