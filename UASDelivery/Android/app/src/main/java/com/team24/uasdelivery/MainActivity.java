/*
 * created by Kirill Kultinov
 */
package com.team24.uasdelivery;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //set the first fragment

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame, new DeliveryFragment(), "DeliveryFragment").commit();
        setTitle("Delivery Status");
        navigationView.setCheckedItem(R.id.delivery_fragment);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.action_logout) {
            if(fragmentManager.findFragmentByTag("DeliveryLogedInFragment") != null) {
                //remove the fragment when the user is logging out
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("DeliveryLogedInFragment")).commit();
                //show the login page fragment
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("DeliveryFragment")).commit();
                FirebaseAuth.getInstance().signOut();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //MenuItem menuItem = (MenuItem) findViewById(R.id.action_logout);
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.delivery_fragment) {

            if(fragmentManager.findFragmentByTag("DeliveryFragment") != null) {
                //if the fragment exists, show it.
                if(fragmentManager.findFragmentByTag("DeliveryLogedInFragment") != null){
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("DeliveryLogedInFragment")).commit();

                }else {
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("DeliveryFragment")).commit();

                }
            } else {
                //if the fragment does not exist, add it to fragment manager.
                fragmentManager.beginTransaction().add(R.id.frame, new DeliveryFragment(), "DeliveryFragment").commit();

            }
            if(fragmentManager.findFragmentByTag("AboutFragment") != null){
                //if the other fragment is visible, hide it.
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("AboutFragment")).commit();
            }
            setTitle("Delivery Status");

        } else if (id == R.id.about_fragment) {

            if(fragmentManager.findFragmentByTag("AboutFragment") != null) {
                //if the fragment exists, show it.
                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("AboutFragment")).commit();
            } else {
                //if the fragment does not exist, add it to fragment manager.
                fragmentManager.beginTransaction().add(R.id.frame, new AboutFragment(), "AboutFragment").commit();
            }
            if(fragmentManager.findFragmentByTag("DeliveryFragment") != null){
                //if the other fragment is visible, hide it.
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("DeliveryFragment")).commit();
            }
            if(fragmentManager.findFragmentByTag("DeliveryLogedInFragment") != null){
                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("DeliveryLogedInFragment")).commit();
            }
            setTitle("About");


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag("DeliveryFragment") != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("DeliveryFragment")).commit();
        }
        if(fragmentManager.findFragmentByTag("AboutFragment") != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("AboutFragment")).commit();
        }
    }
}
