package com.cesarparent.netnotes.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;

import org.w3c.dom.Text;

/**
 * Created by cesar on 05/03/2016.
 * 
 * Adapter that provides ListViews with the data contained in Model.
 */
public class NotesAdapter extends BaseAdapter {
    
    private LayoutInflater _inflater;

    /**
     * Creates an Adapter.
     * @param context       The activity that displays the AdapterView.
     */
    public NotesAdapter(@NonNull Context context) {
        _inflater = LayoutInflater.from(context);
    }

    /**
     * Returns the number of notes in the collection.
     * @return  The number of notes in the collection.
     */
    @Override
    public int getCount() {
        return Model.getNotesCount();
    }


    /**
     * Returns the title of the note at a given position.
     * @param position      The position of the note to return.
     * @return  The title of the note at position.
     */
    @Override
    public Object getItem(int position) {
        return Model.getHandleAtIndex(position).title;
    }

    /**
     * Returns the the note at a given position.
     * @param position      The position of the note to return.
     * @return  The note at position.
     */
    @Override
    public long getItemId(int position) {
        return Model.getHandleAtIndex(position).hashCode();
    }

    /**
     * Returns a view filled with the data from one note.
     * @param position          The position of the note to display.
     * @param convertView       If it exists, the view to recycle.
     * @param parent            The ViewGroup that the ListView belongs to.
     * @return  An inflated view_note_list_item filled with the data of the note at position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null) {
            view = _inflater.inflate(R.layout.view_note_list_item, null);
        }
        TextView titleView = (TextView)view.findViewById(R.id.noteTitle);
        titleView.setText(Model.getHandleAtIndex(position).title);
        return view;
    }
}
