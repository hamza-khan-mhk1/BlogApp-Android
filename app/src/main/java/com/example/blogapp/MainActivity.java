package com.example.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
   // private FloatingActionButton btn_floatingAction;

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    static String user_id, user_name, user_image_url;
    private DatabaseReference usersRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // manage tool bar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Blog");

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        // manage floating button
       /* btn_floatingAction = (FloatingActionButton) findViewById(R.id.btn_floatingAction);
        btn_floatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("btn", "onClick: floating clicked");
                newPost();
            }
        });*/

        // manage bottom navigation
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.item_home:
                        HomeFragment homeFragment = new HomeFragment();
                        replaceFragment(homeFragment);
                        break;

                    case R.id.item_notifications:
                        NotificationsFragment notificationsFragment = new NotificationsFragment();
                        replaceFragment(notificationsFragment);
                        break;

                    case R.id.item_account:
                        AccountFragment accountFragment = new AccountFragment();
                        replaceFragment(accountFragment);
                        break;
                    default:
                        HomeFragment home = new HomeFragment();
                        replaceFragment(home);
                        break;
                }
                return false;
            }
        });
        HomeFragment home = new HomeFragment();
        replaceFragment(home);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();

    }

    private void newPost() {
        Intent addPostIntent = new Intent(MainActivity.this, AddPostActivity.class);
        startActivity(addPostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){ login(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        usersRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("user_name").getValue().toString();
                user_image_url = dataSnapshot.child("user_image").getValue().toString();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error Retreiving User Name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_logout_btn:
                logout();
                return true;

            case R.id.menu_account_settings_btn:
                settings();
                return true;
        }
        return false;
    }

    private void settings() {
        Intent settingsIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
        startActivity(settingsIntent );
    }

    private void login() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        login();
    }
}
