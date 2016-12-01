package org.npelly.android.about;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.TextManager;

// TODO: A "download updates from go/android-about" banner
public class MobileActivity extends AppCompatActivity implements View.OnClickListener, TextManager.Callback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        About.logd("MobileActivity onCreate()");

        setContentView(R.layout.activity_mobile);
        onTextChanged();

        About.get().getTextManager().addCallback(this);

        ImageButton button = (ImageButton)findViewById(R.id.add_button);
        button.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        About.logd("MobileActivity onDestroy()");

        About.get().getTextManager().removeCallback(this);
    }

    /**
     * Called when text content needs to be updated
     */
    @Override
    public void onTextChanged() {
        About.logd("MobileActivity onTextChanged()");

        TextView textView = (TextView)findViewById(R.id.activity_text);
        textView.setText(About.get().getTextManager().getActivityText());
    }

    /**
     * Called when the add button is clicked
     */
    @Override
    public void onClick(View view) {
        About.logd("MobileActivity onClick()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Package");
        final EditText input = new EditText(this);

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
}
