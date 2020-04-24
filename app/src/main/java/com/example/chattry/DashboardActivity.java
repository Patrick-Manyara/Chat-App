package com.example.chattry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    TextView mProfileTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Profile");

        firebaseAuth=FirebaseAuth.getInstance();

        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        actionBar.setTitle("Home");
        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();


    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener= new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            ActionBar actionBar=getSupportActionBar();
            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment fragment1=new HomeFragment();
                    FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content,fragment1,"");
                    ft1.commit();
                return true;

                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment2=new ProfileFragment();
                    FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content,fragment2,"");
                    ft2.commit();
                    return true;

                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment fragment3=new UsersFragment();
                    FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content,fragment3,"");
                    ft3.commit();
                    return true;

                case R.id.nav_chats:
                    actionBar.setTitle("Chats");
                    ChatListFragment fragment4=new ChatListFragment();
                    FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content,fragment4,"");
                    ft4.commit();
                    return true;


            }
            return false;
        }
    };

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null){


        }

        else {
            Intent myintent=new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(myintent);
        }

    }







}
