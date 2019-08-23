package com.example.ucsbmenuwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuTemplateApp extends Activity {
    ExpandableListView expandMenu;
    ExpandableListAdapter expandMenuAdapter;
    List<String> menuTitles;
    HashMap<String, List<String>> menuDetails;
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
        expandMenu = findViewById(R.id.expandmenu);
        expandMenuAdapter = new CustomExpandableListAdapter(this, menuTitles, menuDetails);
        expandMenu.setAdapter(expandMenuAdapter);
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
                String diningCommonID;
                switch (diningCommon) {
                    case "Carrillo":
                        diningCommonID = "carrillo-body";
                        break;
                    case "De La Guerra":
                        diningCommonID = "de-la-guerra-body";
                        break;
                    case "Ortega":
                        diningCommonID = "ortega-body";
                        break;
                    case "Portola":
                        diningCommonID = "portola-body";
                        break;
                    default:
                        diningCommonID = "Invalid";
                        break;
                }
                /* this line finds the menu for a dining common using the id tag (#), then
                * it goes through all its children, and looks through the children's children
                * for elements with class panel-body. This creates a list of menus based on meal time*/
                Elements meals = document.select("#" + diningCommonID + " > * > div.panel-body");
                Elements mealTitles = document.select("#" + diningCommonID + " > * > div.panel-heading");
                int idx = 0;
                //This for loop adds text to menuText in a format such that meal times, meal items are properly spaced
                for (Element e : mealTitles) {
                    menuTitles.add(e.text());
                    menuText += e.text();
                    menuText += "\n";
                    Elements formatMeals = meals.get(idx).select("> * > *");
                    List<String> mealList = new ArrayList<String>();
                    for (Element meal : formatMeals) {
                        mealList.add(meal.text());
                        menuText += meal.text();
                        menuText += "\n";
                    }
                    menuDetails.put(e.text(), mealList);
                    idx++;
                }
                if(menuText == "") {
                    menuText = diningCommon + " is closed.";
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