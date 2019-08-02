package com.example.ucsbmenuwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MenuTemplateApp extends Activity {
    TextView menuView;
    String menuText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menutemplateapp);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        // Here we turn your string.xml in an array
        String[] myKeys = getResources().getStringArray(R.array.dining_commons);

        TextView myTextView = findViewById(R.id.my_textview);
        myTextView.setText(myKeys[position]);
        menuView = findViewById(R.id.menuview);
        new menuGetter().execute();
    }

    //change method to run in async task
    private class menuGetter extends AsyncTask<Void, Void, Void> {
        private void getMenu() {
            try {
                //Connect to the website
                Document document = Jsoup.connect("https://appl.housing.ucsb.edu/menu/day/").get();
                //find selected diningcommon by checking string in my_textview
                String diningCommon = ((TextView) findViewById(R.id.my_textview)).getText().toString();
                switch (diningCommon) {
                    case "Carrillo":
                        diningCommon = "carrillo-body";
                        break;
                    case "De La Guerra":
                        diningCommon = "de-la-guerra-body";
                        break;
                    case "Ortega":
                        diningCommon = "ortega-body";
                        break;
                    case "Portola":
                        diningCommon = "portola-body";
                        break;
                    default:
                        diningCommon = "Invalid";
                        break;
                }
                /* this line finds the menu for a dining common using the id tag (#), then
                * it goes through all its children, and looks through the children's children
                * for elements with class panel-body. This creates a list of menus based on meal time*/
                Elements meals = document.select("#" + diningCommon + " > * > div.panel-body");
                Elements mealTitles = document.select("#" + diningCommon + " > * > div.panel-heading");
                int idx = 0;
                //This for loop adds text to menuText in a format such that meal times, meal items are properly spaced
                for (Element e : mealTitles) {
                    menuText += e.text();
                    menuText += "\n";
                    Elements formatMeals = meals.get(idx).select("> * > *");
                    for (Element meal : formatMeals) {
                        menuText += meal.text();
                        menuText += "\n";
                    }
                    idx++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getMenu();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            menuView.setText(menuText);
        }
    }
}