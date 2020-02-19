package de.hsf.mobcomgroup1.runourway.View;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentHome;
import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentStatistic;
import de.hsf.mobcomgroup1.runourway.HelpfulClasses.Permission;
import de.hsf.mobcomgroup1.runourway.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public SectionStatePagerAdapter mSectionStatePagerAdapter;
    private LockableViewPager mViewPager;

    public static MainActivity activity;

    public boolean isCusSet;

    public Menu menu;

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        this.menu = menu;
        if(!Permission.askForPermissions(this)){
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(false);
        }else{
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(true);
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(true);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(Permission.askForPermissions(this)){
            mSectionStatePagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
            mViewPager = (LockableViewPager) findViewById(R.id.container);
            mViewPager.setSwipeable(false);
            setupViewPager(mViewPager);
        }
    }

   public void setupViewPager(ViewPager ViewPager) {
       mSectionStatePagerAdapter.addFragment(new FragmentHome(), "FragmentHome");
       mSectionStatePagerAdapter.addFragment(new FragmentStatistic(), "FragmentStatistic");
        ViewPager.setAdapter(mSectionStatePagerAdapter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
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
        switch (item.getItemId()){
            case R.id.actionHome:
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                activity.setViewPager(0);
                result = true;
                break;
            case R.id.actionGen:
                Toast.makeText(this, "Strecke generieren", Toast.LENGTH_SHORT).show();
                isCusSet = false;
                intent = new Intent(activity, SecondActivity.class);
                startActivity(intent);
                result = true;
                break;
            case R.id.actionCus:
                Toast.makeText(this,"Strecke erstellen",Toast.LENGTH_SHORT).show();
                isCusSet = true;
                intent = new Intent(activity, SecondActivity.class);
                startActivity(intent);
                result = true;
                break;
            case R.id.actionStatistic:
                Toast.makeText(this,"Statistic",Toast.LENGTH_SHORT).show();
                activity.setViewPager(1);
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }
}
