package ru.johnlife.lifetools.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.R;

public abstract class PagerFragment extends BaseAbstractFragment {
    private static final String STATE_INDEX = "index.state";

    public interface FragmentFactory {
        BaseAbstractFragment createFragment();
    }
    public class TabDescriptor {
        private int title;
        private FragmentFactory factory;

        public TabDescriptor(int title, FragmentFactory factory) {
            this.title = title;
            this.factory = factory;
        }
    }
    private static class FragmentPagerAdapter extends PagerAdapter {

        private final List<TabDescriptor> mFragmentList = new ArrayList<>();
        private final FragmentManager manager;
        private Context context;
        private FragmentTransaction mCurTransaction = null;
        private Fragment mCurrentPrimaryItem = null;

        public FragmentPagerAdapter(FragmentManager fm, Context context) {
            manager = fm;
            this.context = context;
        }

        @Override
        public void startUpdate(ViewGroup container) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mCurTransaction == null) {
                mCurTransaction = manager.beginTransaction();
            }
            BaseAbstractFragment fragment = createItem(position);
            mCurTransaction.add(container.getId(), fragment);
            if (fragment != mCurrentPrimaryItem) {
                fragment.setMenuVisibility(false);
                fragment.setUserVisibleHint(false);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = manager.beginTransaction();
            }
            mCurTransaction.remove((Fragment)object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment)object;
            if (fragment != mCurrentPrimaryItem) {
                if (mCurrentPrimaryItem != null) {
                    mCurrentPrimaryItem.setMenuVisibility(false);
                    mCurrentPrimaryItem.setUserVisibleHint(false);
                }
                if (fragment != null) {
                    fragment.setMenuVisibility(true);
                    fragment.setUserVisibleHint(true);
                }
                mCurrentPrimaryItem = fragment;
            }
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                manager.executePendingTransactions();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment)object).getView() == view;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        public BaseAbstractFragment createItem(int position) {
            return mFragmentList.get(position).factory.createFragment();
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(TabDescriptor tab) {
            mFragmentList.add(tab);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return context.getString(mFragmentList.get(position).title);
        }

    }

    protected ViewPager pager;
    private TabLayout tabLayout;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        pager = (ViewPager) view.findViewById(getPagerId());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), inflater.getContext());
        for (TabDescriptor tab : getTabDescriptors()) {
            adapter.addFragment(tab);
        }
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        setHasOptionsMenu(true);
        if (savedInstanceState != null && null != pager) {
            pager.setCurrentItem(savedInstanceState.getInt(STATE_INDEX, 0));
        }
        return view;
    }

    protected int getPagerId() {
        return R.id.pager;
    }

    protected int getLayoutId() {
        return R.layout.fragment_pager;
    }

    protected abstract TabDescriptor[] getTabDescriptors();

    protected void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        AppBarLayout value = createToolbarFrom(R.layout.toolbar_main);
        setTabLayout((TabLayout) value.findViewById(R.id.tabs));
        return value;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != pager) {
            outState.putInt(STATE_INDEX, pager.getCurrentItem());
        }
    }
}
