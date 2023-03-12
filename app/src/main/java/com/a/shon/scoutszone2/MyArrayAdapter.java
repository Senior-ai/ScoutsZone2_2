package com.a.shon.scoutszone2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.a.shon.scoutszone2.enums.ZoneType;
import com.a.shon.scoutszone2.enums.isZonebusy;

import java.util.ArrayList;

public class MyArrayAdapter extends ArrayAdapter<Zone> {

    private final Context context;
    private final ArrayList<Zone> valuesList;

    TextView tvZonename;
    TextView tvSpace;
    TextView tvType;
    ImageView ivIsbusy;


    public MyArrayAdapter(Context _context, Zone[] _values)
    {
        super(_context, R.layout.zone_adapter, _values);
        this.context = _context;
        this.valuesList = null;
    }

    // Constructor for an ArrrayList
    public MyArrayAdapter(Context _context, ArrayList<Zone> _valuesList)
    {
        super(_context, R.layout.zone_adapter, _valuesList);
        this.context = _context;
        this.valuesList = _valuesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String Path2font = "Abraham-Regular.ttf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), Path2font);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.zone_adapter, parent, false);

        tvZonename = (TextView) rowView.findViewById(R.id.tvZonename);
        tvSpace = (TextView) rowView.findViewById(R.id.tvSpace);
        tvType = (TextView) rowView.findViewById(R.id.tvType);
        ivIsbusy = (ImageView) rowView.findViewById(R.id.ivIsbusy);
        tvZonename.setTypeface(tf);
        tvSpace.setTypeface(tf);
        tvType.setTypeface(tf);

        if (valuesList != null)
            handleArrayList(position);

        return rowView;
    }


    private void handleArrayList(int position)
    {
        Zone z = valuesList.get(position);
        tvZonename.setText("שם האזור- " + z.getZonename());
        tvSpace.setText("יכול להכיל עד- " + z.getSpace());
        ZoneType zt = z.getType();
        if (zt.toString().equals("Inside"))
            tvType.setText("בתוך השבט");
        if (zt.toString().equals("Outside"))
            tvType.setText("מחוץ לשבט");
        if (zt.toString().equals("Inside_a_room"))
            tvType.setText("בתוך חדר");


        isZonebusy g = z.getAvailability();
        setImage(g);
        // change the icon for Availablity

    }

    private void setImage(isZonebusy g)
    {
        switch (g)
        {
            case Halfbusy:
                ivIsbusy.setImageResource(R.drawable.halfbusy);
                break;
            case Busy:
                ivIsbusy.setImageResource(R.drawable.busy);
                break;
            case Available:
                ivIsbusy.setImageResource(R.drawable.available);
            default:
                ivIsbusy.setImageResource(R.drawable.available);
                break;
        }
    }

}

