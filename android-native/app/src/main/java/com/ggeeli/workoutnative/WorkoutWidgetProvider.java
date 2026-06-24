package com.ggeeli.workoutnative;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WorkoutWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAll(context, appWidgetManager, appWidgetIds);
    }

    static void updateAll(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int count = WorkoutStore.todayCount(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_workout);
        views.setTextViewText(R.id.widgetTitle, "Workout Calendar");
        views.setTextViewText(
            R.id.widgetSubtitle,
            count == 0 ? "No workouts logged today" : count + " workout" + (count == 1 ? "" : "s") + " today"
        );

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}