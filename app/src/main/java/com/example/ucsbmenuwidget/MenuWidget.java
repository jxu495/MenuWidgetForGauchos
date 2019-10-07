package com.example.ucsbmenuwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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

    ArrayList<String> menuItems = new ArrayList<>();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
            new MenuGetter(context, views).execute();
            //views.setRemoteAdapter(R.id.widget_list, intent);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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

        private RemoteViews views;
        private Context context;

        public MenuGetter(Context context, RemoteViews views) {
            this.views = views;
            this.context = context;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect("https://appl.housing.ucsb.edu/menu/day/").get();
                Elements meals = doc.select("#carrillo-body" + " > * > div.panel-body");
                //Elements mealTitles = doc.select("#carrillo-body" + " > * > div.panel-heading");
                for(Element meal : meals) {
                    menuItems.add(meal.text());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //tests if menuitems is parsed correctly.
            System.out.println(menuItems);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(context, MenuWidgetService.class);
            intent.putStringArrayListExtra("menuItems", menuItems);
            views.setRemoteAdapter(R.id.widget_list, intent);
            super.onPostExecute(aVoid);
        }
    }
}

