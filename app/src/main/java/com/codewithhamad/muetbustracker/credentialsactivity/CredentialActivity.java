package com.codewithhamad.muetbustracker.credentialsactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.databinding.ActivityCredentialBinding;
import com.codewithhamad.muetbustracker.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CredentialActivity extends AppCompatActivity {

    private ActivityCredentialBinding binding;
    private FirebaseDatabase database;
    private boolean isUserRegistered= false;
    private HelperClass helperClass;
    private String androidId= "";
    private boolean stateChanged= false;
    private CredentialActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding= ActivityCredentialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // initializing vars
        database= FirebaseDatabase.getInstance();
        helperClass= new HelperClass(this);
        androidId= helperClass.getAndroidId();
        isUserRegistered= checkUserRegistration();
        activity= this;


        if(savedInstanceState != null) {
            stateChanged = savedInstanceState.getBoolean("stateChanged");

            // showing logo at to horizontal
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) binding.pointLogoClickable.getLayoutParams();

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            binding.pointLogoClickable.setLayoutParams(layoutParams);
            binding.activityCredentialsContainer.setVisibility(View.VISIBLE);
        }

        // animating logo only once
        if(!stateChanged) {
            animateLogoToCenterHorizontal();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    binding.activityCredentialsContainer.setVisibility(View.VISIBLE);

                    if(!isFinishing()) {
                        if (isUserRegistered) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.activityCredentialsContainer, new LoginFragment());
                            transaction.commit();
                        } else {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.activityCredentialsContainer, new SignUpFragment());
                            transaction.commit();
                        }
                    }
                }
            }, 3000);

        }
    }

    private void animateLogoToCenterHorizontal() {
        stateChanged= true;
        // animating logo to the top
        ObjectAnimator animation = ObjectAnimator.ofFloat(binding.pointLogoClickable,
                "translationY", -500f);
        animation.setDuration(3000L);
        animation.start();
    }

    private boolean checkUserRegistration() {
        database.getReference().child("Users").child(androidId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isUserRegistered = true;
//                            Toast.makeText(getApplicationContext(), "registered", Toast.LENGTH_SHORT).show();
                        }
//                        else
//                            Toast.makeText(getApplicationContext(), "not registered", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        return isUserRegistered;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stateChanged", stateChanged);
    }

//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        stateChanged= true;
//    }
}