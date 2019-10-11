package com.example.ucsbmenuwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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

    ArrayList<String> menuItems = new ArrayList<>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            new MenuGetter(appWidgetId, appWidgetManager, context).execute();
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

        private Context context;
        private int id;
        private AppWidgetManager appWidgetManager;

        public MenuGetter(int appWidgetID, AppWidgetManager appWidgetManager, Context context) {
            this.id = appWidgetID;
            this.appWidgetManager = appWidgetManager;
            this.context = context;
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
            //tests if menuitems is parsed correctly.
            //System.out.println(menuItems); this works
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Intent intent = new Intent(context, MenuWidgetService.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.menu_widget);
            Intent intent = new Intent(context, MenuWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            intent.putStringArrayListExtra("menuItems", menuItems);
            ArrayList<String> test = intent.getStringArrayListExtra("menuItems");
            //System.out.println(test.get(0));
            views.setRemoteAdapter(R.id.widget_list, intent);
            appWidgetManager.updateAppWidget(id, views);
            super.onPostExecute(aVoid);
        }
    }
}

