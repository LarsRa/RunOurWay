package de.hsf.mobcomgroup1.runourway.View;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentCustomRoute;
import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentGeneration;
import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentMap;
import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentRun;
import de.hsf.mobcomgroup1.runourway.R;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";

    private SectionStatePagerAdapter mSectionStatePagerAdapter;
    private LockableViewPager viewPagerMap;
    private LockableViewPager viewPagerRun;

    private OnBackPressed onBackPressed;

    public static SecondActivity activity;

    public void setMenuListener(MenuClickListener menuListener) {
        this.menuListener = menuListener;
    }

    private MenuClickListener menuListener;

    public Menu menu;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity_layout);

        intializeFragments();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        viewPagerMap = (LockableViewPager) findViewById(R.id.mapContainer);
        viewPagerMap.setSwipeable(false);
        setupViewPagerMap(viewPagerMap);
        viewPagerRun = (LockableViewPager) findViewById(R.id.runContainer);
        setupViewPagerRun(viewPagerRun);
        viewPagerRun.setSwipeable(false);

        if (MainActivity.activity.isCusSet) {
            setViewPagerRun(1);
        }
    }

    public void setupViewPagerMap(ViewPager ViewPager) {
        SectionStatePagerAdapter adapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(getFragment(FragmentMap.class), "FragmentMap");
        ViewPager.setAdapter(adapter);
    }

    public void setupViewPagerRun(ViewPager ViewPager) {
        SectionStatePagerAdapter adapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(getFragment(FragmentGeneration.class), "FragmentGeneration");
        adapter.addFragment(getFragment(FragmentCustomRoute.class), "FragmentCustomRoute");
        adapter.addFragment(getFragment(FragmentRun.class), "FragmentRun");
        ViewPager.setAdapter(adapter);
    }

    public void setViewPagerRun(int fragmentNumber) {
        viewPagerRun.setCurrentItem(fragmentNumber);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean result = false;
        Intent intent;
        if(menuListener!=null){
            menuListener.menuItemClickedSingleCallback(item);
            setMenuListener(null);
        }
        switch (item.getItemId()) {
            case R.id.actionHome:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                activity.finish();
                MainActivity.activity.setViewPager(0);
                result = true;
                break;
            case R.id.actionGen:
                Toast.makeText(this, "Strecke generieren", Toast.LENGTH_SHORT).show();
                FragmentMap.setMapActor(SecondActivity.activity.getFragmentWithMapInteraction(FragmentGeneration.class));
                FragmentCustomRoute fragment = getFragment(FragmentCustomRoute.class);
                fragment.removeOldPath();
                activity.setViewPagerRun(0);
                break;
            case R.id.actionCus:
                Toast.makeText(this, "Strecke erstellen", Toast.LENGTH_SHORT).show();
                activity.setViewPagerRun(1);
                FragmentMap.setMapActor(SecondActivity.activity.getFragmentWithMapInteraction(FragmentCustomRoute.class));
                FragmentGeneration fragmentGeneration = getFragment(FragmentGeneration.class);
                fragmentGeneration.removeOldPath();
                result = true;
                break;
            case R.id.actionStatistic:
                Toast.makeText(this, "Statistic", Toast.LENGTH_SHORT).show();
                activity.finish();
                MainActivity.activity.setViewPager(1);
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    private List<Fragment> fragments = new ArrayList<>();

    private void intializeFragments() {
        fragments.add(new FragmentMap());
        fragments.add(new FragmentGeneration());
        fragments.add(new FragmentCustomRoute());
        fragments.add(new FragmentRun());
    }


    public <T extends Fragment> T getFragment(Class fragmentType) {
        T result = null;
        for (Fragment f : fragments) {
            if (fragmentType.isInstance(f)) {
                result = (T) f;
                break;
            }
        }
        return result;
    }

    /**
     * @param fragmentType
     * @param <T>          generic type extends Fragment and also implements MapInteraction
     * @return
     */
    public <T extends Fragment & MapInteraction> T getFragmentWithMapInteraction(Class fragmentType) {
        T result = null;
        for (Fragment f : fragments) {
            if (fragmentType.isInstance(f) && MapInteraction.class.isInstance(f)) {
                result = (T) f;
                break;
            }
        }
        return result;
    }



    @Override
    public void onBackPressed(){
        if(this.onBackPressed == null) {
            super.onBackPressed();
        }else{
            this.onBackPressed.handleOnBackPressed();
        }
    }

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }
}


