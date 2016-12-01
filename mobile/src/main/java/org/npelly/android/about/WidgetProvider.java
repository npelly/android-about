package org.npelly.android.about;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.TextManager;

import java.util.Arrays;

/**
 * Implement widget callbacks.
 *
 * Note: Widget not working on Samsung S6? Reboot. Seems to be a phone bug.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final TextManager.Callback CALLBACK = new TextManager.Callback() {
        @Override
        public void onTextChanged() {
            About.logd("WidgetProvider onTextChanged()");

            Context context = About.get().getContext();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, WidgetProvider.class));
            refreshWidgets(context, appWidgetManager, appWidgetIds);
        }
    };

    /**
     * Called when a widget is first shown, and a widget needs periodic update.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        About.logd("WidgetProvider onUpdate()");

        refreshWidgets(context, appWidgetManager, appWidgetIds);
    }

    private static void refreshWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        About.logd("WidgetProvider refreshWidgets() ids=%s", Arrays.toString(appWidgetIds));

        // Create intent to launch Activity
        Intent intent = new Intent(context, MobileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the RemoteViews for this widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        // Update text
        TextManager textManager = About.get().getTextManager();
        views.setTextViewText(R.id.widget_text, textManager.getWidgetText());
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        // Perform updates on all widgets
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
