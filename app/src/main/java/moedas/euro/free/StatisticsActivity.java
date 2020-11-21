package moedas.euro.free;


import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.MenuItem;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;


public class StatisticsActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    Functions classFunctions = new Functions(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setTitle(R.string.title_menu_statistics);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Start ads**/
        classFunctions.startAds();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_stats_tab1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_stats_tab2));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        mViewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle fragArgument = new Bundle();

            switch (position) {
                case 0:
                    fragArgument.clear();
                    fragArgument.putInt("tab1", 1);
                    StatisticsFragment tab1 = new StatisticsFragment();
                    tab1.setArguments(fragArgument);
                    return tab1;
                case 1:
                    fragArgument.clear();
                    fragArgument.putInt("tab2", 2);
                    StatisticsFragment tab2 = new StatisticsFragment();
                    tab2.setArguments(fragArgument);
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}