package com.example.ucsbmenuwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class MenuWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            new MenuGetter(appWidgetId, appWidgetManager, context).execute();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MenuWidget.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private class MenuGetter extends AsyncTask<Void, Void, Void> {

        private Context context;
        private int id;
        private AppWidgetManager appWidgetManager;
        ArrayList<String> menuItems;

        public MenuGetter(int appWidgetID, AppWidgetManager appWidgetManager, Context context) {
            this.id = appWidgetID;
            this.appWidgetManager = appWidgetManager;
            this.context = context;
            menuItems = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect("https://appl.housing.ucsb.edu/menu/day/").get();
                Elements meals = doc.select("#carrillo-body" + " > * > div.panel-body");
                Elements mealTitles = doc.select("#carrillo-body" + " > * > div.panel-heading");
                for(int i = 0; i < mealTitles.size(); i++) {
                    menuItems.add(mealTitles.get(i).text());
                    Elements formatMeals = meals.get(i).select("> * > *");
                    for(Element meal : formatMeals) {
                        menuItems.add(meal.text());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                menuItems.add("No connectivity, check internet");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intentSync = new Intent(context, MenuWidget.class);
            intentSync.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
            Intent intent = new Intent(context, MenuWidgetService.class);
            //This line is to bypass the caching of the remoteviewsfactory, it changes the appwidgetid so that
            //android doesnt use a cached factory with old data. This is a temporary fix, because theres a 0,1% chance
            //that the data refreshes
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id + Math.round(Math.random() * 1000));
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            intent.putStringArrayListExtra("menuItems", menuItems);
            views.setRemoteAdapter(id, R.id.widget_list, intent);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.widget_list);
            appWidgetManager.updateAppWidget(id, views);
            super.onPostExecute(aVoid);
        }
    }
}

