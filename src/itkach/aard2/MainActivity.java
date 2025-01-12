package itkach.aard2;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Objects;

import itkach.slob.Slob;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppSectionsPagerAdapter appSectionsPagerAdapter;
    private ViewPager viewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Application app = (Application)getApplication();
        app.installTheme(this);
        setContentView(R.layout.activity_main);

        appSectionsPagerAdapter = new AppSectionsPagerAdapter(
                getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(appSectionsPagerAdapter.getCount());
        viewPager.setAdapter(appSectionsPagerAdapter);

        final String[] subtitles = new String[] {
                getString(R.string.subtitle_lookup),
                getString(R.string.subtitle_bookmark),
                getString(R.string.subtitle_history),
                getString(R.string.subtitle_dictionaries),
                getString(R.string.subtitle_settings),
        };

        viewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                        actionBar.setSubtitle(subtitles[position]);
                    }
                });

        Drawable[] tabIcons = new Drawable[5];
        tabIcons[0] = IconMaker.INSTANCE.tab(this, IconMaker.IC_SEARCH);
        tabIcons[1] = IconMaker.INSTANCE.tab(this, IconMaker.IC_BOOKMARK);
        tabIcons[2] = IconMaker.INSTANCE.tab(this, IconMaker.IC_HISTORY);
        tabIcons[3] = IconMaker.INSTANCE.tab(this, IconMaker.IC_DICTIONARY);
        tabIcons[4] = IconMaker.INSTANCE.tab(this, IconMaker.IC_SETTINGS);
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < appSectionsPagerAdapter.getCount(); i++) {
            Tab tab = actionBar.newTab();
            tab.setTabListener(this);
            tab.setIcon(tabIcons[i]);
            actionBar.addTab(tab);
        }

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            if (app.dictionaries.size() == 0) {
                viewPager.setCurrentItem(3);
            }
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int currentSection = savedInstanceState.getInt("currentSection");
        viewPager.setCurrentItem(currentSection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentSection", viewPager.getCurrentItem());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        Fragment frag = appSectionsPagerAdapter.getItem(tab.getPosition());
        if (frag instanceof BaseListFragment) {
            ((BaseListFragment)frag).finishActionMode();
        }
        if (tab.getPosition() == 0) {
            View v = this.getCurrentFocus();
            if (v != null){
                InputMethodManager mgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Application app = (Application) getApplication();
            Slob.Blob blob = app.random();
            if (blob == null) {
                Toast.makeText(this,
                        R.string.article_collection_nothing_found,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this,
                    ArticleCollectionActivity.class);
            intent.setData(Uri.parse(app.getUrl(blob)));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // Looks like shown soft input sometimes causes a system ui visibility
        // Change event that breaks article activity launched from here out of full screen mode.
        // Hiding it appears to reduce that.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        }
        catch (Exception e) {
            Log.w(TAG, "Hiding soft input failed", e);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        Fragment frag = appSectionsPagerAdapter.getItem(currentItem);
        Log.d(TAG, "current tab: " + currentItem);
        if (frag instanceof BlobDescriptorListFragment bdFrag) {
            if (bdFrag.isFilterExpanded()) {
                Log.d(TAG, "Filter is expanded");
                bdFrag.collapseFilter();
                return;
            }
        }
        super.onBackPressed();
    }

    public static final class BookmarksFragment extends
            BlobDescriptorListFragment {
        @Override
        public String getItemClickAction() {
            return "showBookmarks";
        }

        @Override
        public BlobDescriptorList getDescriptorList() {
            Application app = (Application) getActivity().getApplication();
            return app.bookmarks;
        }

        @Override
        public char getEmptyIcon() {
            return IconMaker.IC_BOOKMARK;
        }

        @Override
        public String getEmptyText() {
            return getString(R.string.main_empty_bookmarks);
        }

        @Override
        public int getDeleteConfirmationItemCountResId() {
            return R.plurals.confirm_delete_bookmark_count;
        }

        @Override
        public String getPreferencesNS() {
            return "bookmarks";
        }
    }

    public static class HistoryFragment extends BlobDescriptorListFragment {
        @Override
        public String getItemClickAction() {
            return "showHistory";
        }

        @Override
        public BlobDescriptorList getDescriptorList() {
            Application app = (Application) getActivity()
                    .getApplication();
            return app.history;
        }

        @Override
        public char getEmptyIcon() {
            return IconMaker.IC_HISTORY;
        }

        @Override
        public String getEmptyText() {
            return getString(R.string.main_empty_history);
        }

        @Override
        public int getDeleteConfirmationItemCountResId() {
            return R.plurals.confirm_delete_history_count;
        }

        @Override
        public String getPreferencesNS() {
            return "history";
        }

    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] fragments;
        LookupFragment tabLookup;
        BlobDescriptorListFragment tabBookmarks;
        BlobDescriptorListFragment tabHistory;
        DictionariesFragment tabDictionaries;
        SettingsFragment tabSettings;

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            tabLookup = new LookupFragment();
            tabBookmarks = new BookmarksFragment();
            tabHistory = new HistoryFragment();
            tabDictionaries = new DictionariesFragment();
            tabSettings = new SettingsFragment();
            fragments = new Fragment[] { tabLookup, tabBookmarks, tabHistory,
                    tabDictionaries, tabSettings };
        }

        @Override
        public Fragment getItem(int i) {
            return fragments[i];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!autoPaste()) {
            Log.d(TAG, "Auto-paste is off");
            return;
        }
        if (!hasFocus) {
            Log.d(TAG, "has no focus");
            return;
        }
        CharSequence text = Clipboard.INSTANCE.peek(this);
        if (text != null) {
            viewPager.setCurrentItem(0);
            invalidateOptionsMenu();
        }
    }

    private boolean useVolumeForNav() {
        Application app = (Application)getApplication();
        return app.useVolumeForNav();
    }

    private boolean autoPaste() {
        Application app = (Application)getApplication();
        return app.autoPaste();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (event.isCanceled()) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!useVolumeForNav()) {
                return false;
            }
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
            else {
                viewPager.setCurrentItem(appSectionsPagerAdapter.getCount() - 1);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!useVolumeForNav()) {
                return false;
            }
            int current = viewPager.getCurrentItem();
            if (current < appSectionsPagerAdapter.getCount() - 1) {
                viewPager.setCurrentItem(current + 1);
            }
            else {
                viewPager.setCurrentItem(0);
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return useVolumeForNav();
        }
        return super.onKeyDown(keyCode, event);
    }

}
