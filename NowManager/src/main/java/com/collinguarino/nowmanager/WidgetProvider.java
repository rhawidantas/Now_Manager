package com.collinguarino.nowmanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by Collin on 10/1/13.
 */
public class WidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_ITEM = "com.collinguarino.nowmanager.widget.EXTRA_ITEM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int i=0;i<appWidgetIds.length;i++){
            Intent intent = new Intent(context, Main.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent remoteAdapterIntent = new Intent(context, ListWidgetService.class);
            views.setRemoteAdapter(android.R.id.list, remoteAdapterIntent);
            views.setEmptyView(android.R.id.list, android.R.id.empty);


            // Click on the listview in the widget to open the app - NOT WORKING : Currently the listview is stealing the onclick
            PendingIntent appOpenIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widgetView, appOpenIntent);

            // Tap to add event button -- NOT WORKING : Currently does not add new event, only opens main.java
            PendingIntent addEventIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.addButton, addEventIntent);

            // Adds the above to the widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        super.onReceive(context, intent);
    }
}

