package com.example.xyzreader.ui.detail;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.common.ImageLoaderHelper;
import com.example.xyzreader.utils.StringUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailFragment";
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_POSITION = "position";
    public static final String ARG_ENTER_POSITION = "enter_position";

    @BindView(R.id.photo)
    ImageView mPhotoView;
    @BindView(R.id.detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_byline)
    TextView bylineView;
    @BindView(R.id.article_body)
    TextView bodyView;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.action_up)
    ImageButton ibActionUp;
    @BindView(R.id.meta_bar)
    LinearLayout llMetaBar;
    @BindView(R.id.fab_share)
    FloatingActionButton fabShare;

    private Cursor mCursor;
    private long mItemId;
    private int enterPosition;
    private int position;
    private View mRootView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int enterPosition, int position) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ARG_POSITION, position);
        arguments.putInt(ARG_ENTER_POSITION, enterPosition);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSharedElementReturnTransition(null);

        if (getArguments() != null) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
            position = getArguments().getInt(ARG_POSITION);
            enterPosition = getArguments().getInt(ARG_ENTER_POSITION);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);
        String transitionName = getString(R.string.transition) + position;
        mPhotoView.setTransitionName(transitionName);
        ibActionUp.setOnClickListener(view -> getActivity().onBackPressed());

        bindViews();
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        bylineView.setMovementMethod(new LinkMovementMethod());

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            collapsingToolbar.setTitle(null);
            titleView.setText(title);
            Date publishedDate = parsePublishedDate();

            bylineView.setText(StringUtil.getByline(mCursor, publishedDate, START_OF_EPOCH));
            final String body = mCursor.getString(ArticleLoader.Query.BODY).substring(0, 1800).
                    replaceAll("(\r\n|\n)", " ");
            bodyView.setText(Html.fromHtml(body));

            fabShare.setOnClickListener(view -> startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(title + "/n" + publishedDate + "/n" + body)
                    .getIntent(), getString(R.string.action_share))));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                int defaultColor = ContextCompat.getColor(getActivity(),
                                        R.color.cardview_dark_background);
                                Palette.from(bitmap).generate(p -> {
                                    llMetaBar.setBackgroundColor(p.getDarkMutedColor(defaultColor));
                                });
                                mPhotoView.setImageBitmap(bitmap);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        startPostponedEnterTransition();

        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    public void startPostponedEnterTransition() {
        if (position == enterPosition) {
            mPhotoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }

    }

    public ImageView getmPhotoView() {
        return mPhotoView;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
