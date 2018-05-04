package com.example.xyzreader.ui.detail;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_PALETTE_COLOR = "extra_palette_color";
    public static final String EXTRA_EXIT_POSITION = "exit_position";
    public static final String EXTRA_ENTER_POSITION = "enter_position";

    public static void startArticleDetailActivity(Context context, long id, int startPosition, int paletteColor, ActivityOptions options) {
        Intent intent = new Intent(context, ArticleDetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_ENTER_POSITION, startPosition);
        intent.putExtra(EXTRA_PALETTE_COLOR, paletteColor);
        context.startActivity(intent, options.toBundle());
    }

    private Cursor mCursor;
    private long mStartId;
    private int enterPosition;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ArticleDetailFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        postponeEnterTransition();

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
            }
        });

        enterPosition = getIntent().getIntExtra(EXTRA_ENTER_POSITION, 0);

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                mStartId = getIntent().getLongExtra(EXTRA_ID, 0);
            }
        }
    }

    @Override
    public void finishAfterTransition() {
        int currentPosition = mPager.getCurrentItem();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ENTER_POSITION, enterPosition);
        intent.putExtra(EXTRA_EXIT_POSITION, currentPosition);
        setResult(RESULT_OK, intent);
        if (enterPosition != currentPosition) {
            setSharedElementCallback();
        }
        super.finishAfterTransition();
    }

    private void setSharedElementCallback() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                ImageView sharedElement = currentFragment.getmPhotoView();
                names.clear();
                sharedElements.clear();
                names.add(sharedElement.getTransitionName());
                sharedElements.put(sharedElement.getTransitionName(), sharedElement);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }



    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            currentFragment = (ArticleDetailFragment) object;

        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID),
                    enterPosition, position);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
