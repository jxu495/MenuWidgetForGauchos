package com.example.ucsbmenuwidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class MenuWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MenuRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class MenuRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<String> menuItems;
    private Context context;
    private Intent intent;


    public MenuRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        System.out.println("test");
    }

    @Override
    public void onCreate() {
        menuItems = intent.getStringArrayListExtra("menuItems");
        //System.out.println("test");
        //System.out.println(menuItems);
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        views.setTextViewText(R.id.widget_list_item, menuItems.get(position));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
