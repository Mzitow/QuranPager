package com.example.quranforinjaziandroid;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SpinAdapter extends ArrayAdapter<SpinAdapter.SpinItems> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private List<SpinItems> values;

    public SpinAdapter(Context context, int textViewResourceId, List<SpinItems> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public SpinItems getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        //TextView label = (TextView) super.getView(position, convertView, parent);
        @SuppressLint("ViewHolder")
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item, parent, false);
        final TextView label=(TextView)row.findViewById(R.id.verse_surah);
        label.setTextColor(Color.WHITE);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).getSpinnerItem());

        // And finally return your dynamic (or custom) view for each spinner item
        return row;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item, parent, false);
        final TextView label=(TextView)row.findViewById(R.id.verse_surah);
        final ImageView dropDownIcon = (ImageView) row.findViewById(R.id.dropDown);

        dropDownIcon.setVisibility(View.INVISIBLE);
        label.setTextColor(Color.WHITE);
        label.setText(values.get(position).getSpinnerItem());
        return row;
    }


    public static class SpinItems{

        String spinnerItem;
        int id;

        public SpinItems() {
            this.spinnerItem = "";
            this.id = 0;
        }

        public SpinItems(int i, String spinnerItem) {
            this.spinnerItem = spinnerItem;
            this.id = i;
        }

        public String getSpinnerItem() {
            return spinnerItem;
        }

        public void setSpinnerItem(String spinnerItem) {
            this.spinnerItem = spinnerItem;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }
}