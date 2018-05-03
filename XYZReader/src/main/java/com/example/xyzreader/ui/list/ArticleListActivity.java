package com.example.xyzreader.ui.list;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.xyzreader.ui.detail.ArticleDetailActivity.EXTRA_ENTER_POSITION;
import static com.example.xyzreader.ui.detail.ArticleDetailActivity.EXTRA_EXIT_POSITION;

public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.main_content)
    CoordinatorLayout clMainContent;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private boolean mIsRefreshing = false;
    private int exitPosition;
    private int enterPosition;
    boolean isReenter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);
        getLoaderManager().initLoader(0, null, this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setExitSharedElementCallback(mCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            enterPosition = data.getIntExtra(EXTRA_ENTER_POSITION, enterPosition);
            exitPosition = data.getIntExtra(EXTRA_EXIT_POSITION, enterPosition);
            isReenter = true;
        }
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() == 0) {
            loadDataFromServer();
        }
        ArticleAdapter adapter = new ArticleAdapter(cursor);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }


    @Override
    public void onRefresh() {
        loadDataFromServer();
    }

    public void loadDataFromServer() {
        if (NetworkUtils.isConnected(this)) {
            startService(new Intent(this, UpdaterService.class));
        } else {
            Snackbar.make(clMainContent, R.string.error_network, Snackbar.LENGTH_LONG).show();
            mIsRefreshing = false;
            updateRefreshingUI();
        }
    }

    private final android.app.SharedElementCallback mCallback = new android.app.SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (isReenter && exitPosition != enterPosition) {
                String newTransitionName = getString(R.string.transition) + exitPosition;
                View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);
                if (newSharedElement != null) {
                    names.clear();
                    names.add(newTransitionName);
                    sharedElements.clear();
                    sharedElements.put(newTransitionName, newSharedElement);
                }
                isReenter = false;
            }
        }
    };

}
