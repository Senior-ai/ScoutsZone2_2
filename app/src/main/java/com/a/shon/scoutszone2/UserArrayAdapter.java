package com.a.shon.scoutszone2;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<UserManager> {

    private final Context context2;
    private final ArrayList<UserManager> valuesList;

    TextView tvFirstname;
    TextView tvLastname;
    TextView tvEmail;
    TextView tvRole;
    Button btnApprove;

    public UserArrayAdapter(Context _context, UserManager[] _values)
    {
        super(_context, R.layout.approveuser_adapter, _values);
        this.context2 = _context;
        this.valuesList = null;
    }

    // Constructor for an ArrrayList
    public UserArrayAdapter(Context _context, ArrayList<UserManager> _valuesList)
    {
        super(_context, R.layout.approveuser_adapter, _valuesList);
        this.context2 = _context;
        this.valuesList = _valuesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String Path2font = "Abraham-Regular.ttf";
        Typeface tf = Typeface.createFromAsset(context2.getAssets(), Path2font);
        LayoutInflater inflater = (LayoutInflater) context2
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.approveuser_adapter, parent, false);

        tvFirstname = (TextView) rowView.findViewById(R.id.tvFirstname);
        tvLastname = (TextView) rowView.findViewById(R.id.tvLastname);
        tvEmail = (TextView) rowView.findViewById(R.id.tvEmail);
        tvRole = (TextView) rowView.findViewById(R.id.tvRole);
        btnApprove = (Button) rowView.findViewById(R.id.btnApprove);
        tvFirstname.setTypeface(tf);
        tvLastname.setTypeface(tf);
        tvRole.setTypeface(tf);

        if (valuesList != null)
            handleArrayList(position);

        return rowView;
    }

    private void handleArrayList(int position)
    {
        UserManager u = valuesList.get(position);
        tvFirstname.setText(" שם: " + u.getName());
        tvLastname.setText(u.getLastName());
        tvEmail.setText( u.getEmail());
        if (u.getRole().toString().equals("א_פעילים"))
            tvRole.setText("תפקיד: א פעילים");
        else
            tvRole.setText("תפקיד: " + u.getRole().toString());
    }


}
