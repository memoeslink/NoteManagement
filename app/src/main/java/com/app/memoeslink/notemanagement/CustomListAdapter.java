package com.app.memoeslink.notemanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<Note> {
    private Context mContext;
    private int id;
    private List<Note> items;

    public CustomListAdapter(Context context, int textViewResourceId, List<Note> notes) {
        super(context, textViewResourceId, notes);
        mContext = context;
        id = textViewResourceId;
        items = notes;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View view = v;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }
        TextView text = (TextView) view.findViewById(R.id.textView);

        if (items != null)
            text.setText(Methods.fromHtml("<small><font color=\"#C5C5C5\">" + items.get(position).getDate() + "</font></small><br>" + Methods.getIdeogramByUnicode(items.get(position).isPersonal() == true ? 0x1F4D9 : 0x1F4D6) + " " + items.get(position).getTitle() + "<br><small>" + mContext.getString(R.string.text_by) + "<font color=\"#8C8ACC\"><b>" + items.get(position).getUsername() + "</b></font> " + Methods.generateIdeogram(items.get(position).getUsername()) + "</small>"));
        return view;
    }
}