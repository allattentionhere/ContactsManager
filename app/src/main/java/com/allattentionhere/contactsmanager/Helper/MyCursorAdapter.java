package com.allattentionhere.contactsmanager.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.allattentionhere.contactsmanager.Activities.DetailsActivity;


public class MyCursorAdapter extends CursorAdapter implements SectionIndexer {

    AlphabetIndexer mAlphabetIndexer;
    Activity _act;

    public MyCursorAdapter(Context context,
                           Cursor cursor, Activity act) {
        super(context, cursor);
        _act = act;
        mAlphabetIndexer = new AlphabetIndexer(cursor, cursor.getColumnIndex("name"), " ABCDEFGHIJKLMNOPQRTSUVWXYZ");
        mAlphabetIndexer.setCursor(cursor);//Sets a new cursor as the data set and resets the cache of indices.
    }

    /**
     * Performs a binary search or cache lookup to find the first row that matches a given section's starting letter.
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    /**
     * Returns the section index for a given position in the list by querying the item and comparing it with all items
     * in the section array.
     */
    @Override
    public int getSectionForPosition(int position) {
        return mAlphabetIndexer.getSectionForPosition(position);
    }

    /**
     * Returns the section array constructed from the alphabet provided in the constructor.
     */
    @Override
    public Object[] getSections() {
        return mAlphabetIndexer.getSections();
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final TextView txtView = (TextView) view.findViewById(android.R.id.text1);
        if (cursor.getPosition() == getPositionForSection(getSectionForPosition(cursor.getPosition()))) {
            //section header
            Spannable spannable = new SpannableString((cursor.getString(cursor.getColumnIndex("name"))).substring(0, 1).toUpperCase() + "  " + cursor.getString(cursor.getColumnIndex("name")));
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtView.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            txtView.setText(cursor.getString(cursor.getColumnIndex("name")));
            txtView.setTextColor(Color.WHITE);
        }


    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     */
    @Override
    public View newView(final Context context, final Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View newView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return newView;
    }
}