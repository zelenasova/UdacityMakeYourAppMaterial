package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
    private Cursor mCursor;

    public ArticleAdapter(Cursor cursor) {
        mCursor = cursor;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        final ArticleViewHolder vh = new ArticleViewHolder(view);
        view.setOnClickListener(view1 -> {
            parent.getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                    ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
        });
        return vh;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(final ArticleViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        holder.subtitleView.setText(StringUtil.getSubtitle(mCursor, publishedDate, START_OF_EPOCH));

        ImageLoader imageLoader = ImageLoaderHelper.getInstance(holder.itemView.getContext()).getImageLoader();
        String imageUrl = mCursor.getString(ArticleLoader.Query.THUMB_URL);
        holder.thumbnailView.setImageUrl(imageUrl, imageLoader);
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                Bitmap bitmap = imageContainer.getBitmap();
                if (bitmap != null) {
                    final int defaultColor = ContextCompat.getColor(holder.itemView.getContext(),
                            R.color.cardview_dark_background);
                    Palette p = Palette.from(bitmap).generate();
                    int mMutedColor = p.getDarkMutedColor(defaultColor);
                    holder.itemView.setBackgroundColor(mMutedColor);
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        DynamicHeightNetworkImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;

        ArticleViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
        }
    }

}


