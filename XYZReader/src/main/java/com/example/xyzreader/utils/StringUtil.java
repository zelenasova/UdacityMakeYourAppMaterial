package com.example.xyzreader.utils;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import com.example.xyzreader.data.ArticleLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by nixone on 14/07/17.
 */

public class StringUtil {

    public static Spanned getSubtitle(Cursor cursor, Date publishedDate, GregorianCalendar START_OF_EPOCH) {
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            return Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR));
        } else {
            return Html.fromHtml(
                    new SimpleDateFormat().format(publishedDate)
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR));
        }

    }

}
