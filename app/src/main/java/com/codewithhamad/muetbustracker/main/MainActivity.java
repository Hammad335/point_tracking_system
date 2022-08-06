package com.codewithhamad.muetbustracker.main;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.TextView;

import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.main.notificationfragment.NotificationsFragment;
import com.codewithhamad.muetbustracker.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    User currentUser= null;
    String androidId= "";
    SharedPreferences sharedPreferences;

    public static DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    public static NavigationView navigationView;

    TextView navBarUserName;
    TextView navBarEmail;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // init views
        database= FirebaseDatabase.getInstance();
        drawerLayout=findViewById(R.id.drawer);
        toolbar=findViewById(R.id.toolBar);
        navigationView=findViewById(R.id.nav_view);
        androidId= getAndroidId();
        sharedPreferences = getPreferences(MODE_PRIVATE);
        currentUser= getCurrentUserFromSharePref();

        setSupportActionBar(toolbar);
        navigationView.setItemIconTintList(null);

        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // default home fragment
        loadFragment(new HomeFragment());
        navigationView.setCheckedItem(R.id.home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id=menuItem.getItemId();
                Fragment fragment;

                switch (id) {
                    case R.id.home:
                        fragment= new HomeFragment();
                        loadFragment(fragment);
//                        getSupportActionBar().setTitle("Home");
                        break;

                    case R.id.selectPoint:
                        fragment=new SelectPointFragment();
                        loadFragment(fragment);
//                        getSupportActionBar().setTitle("Select Point");
                        break;

                    case R.id.myPoint:
                        fragment=new MyPointFragment();
                        loadFragment(fragment);
//                        getSupportActionBar().setTitle("My Point");
                        break;

                    case R.id.notifications:
                        fragment=new NotificationsFragment();
                        loadFragment(fragment);
//                        getSupportActionBar().setTitle("Notifications");
                        break;
                    case R.id.about:
                        fragment=new AboutFragment();
                        loadFragment(fragment);
//                        getSupportActionBar().setTitle("About");
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });


        // setting navHeader textViews
        if(currentUser != null) {
            navBarUserName = navigationView.getHeaderView(0).findViewById(R.id.navBarUserName);
            navBarUserName.setText(currentUser.getUserName());
            navBarEmail= navigationView.getHeaderView(0).findViewById(R.id.navBarUserEmail);
            navBarEmail.setText(currentUser.getEmail());
        }
    }

    private User getCurrentUserFromSharePref() {
        // getting current user from sharedPref
        SharedPreferences sharedPreferences= getSharedPreferences("sharedPref", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("currentUser", null);
        return gson.fromJson(json, User.class);
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
        fragmentTransaction.addToBackStack(null);
    }




    private String getAndroidId() {
        return android.provider.Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


}