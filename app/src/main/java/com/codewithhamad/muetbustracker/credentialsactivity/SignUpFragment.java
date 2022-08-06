package com.codewithhamad.muetbustracker.credentialsactivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codewithhamad.muetbustracker.helper.FirebaseDatabaseCallBack;
import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.databinding.FragmentSignUpBinding;
import com.codewithhamad.muetbustracker.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUpFragment extends Fragment implements FirebaseDatabaseCallBack {

    FragmentSignUpBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog dialog;
    String androidId = null;
    private HelperClass helperClass;
    private boolean stateChanged= false;
    private User currentUser;
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            stateChanged= savedInstanceState.getBoolean("stateChanged");
        context= getContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);

        // anims
        if(!stateChanged)
            setAnim();

        auth= FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        helperClass= new HelperClass(getContext());

        dialog= new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Registering");
        dialog.setMessage("Creating User Account...");

        // getting android id
        androidId = helperClass.getAndroidId();
//        if(androidId != null  || !androidId.isEmpty())
//            binding.androidId.setText(androidId);


        // adding onClickListener to the register button
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName= binding.userName.getText().toString();
                String email= binding.email.getText().toString();
                String pass= binding.password.getText().toString();
                String confirmPass= binding.confirmPassword.getText().toString();

                if(!validateData(userName, email, pass, confirmPass))
                    return;

                // checking internet connectivity
                if(!helperClass.haveNetworkConnection()){
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!helperClass.isOnline()){
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Slow internet connection, try again later.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }, 2000);
                    return;
                }

                dialog.show();

                // check if user is already registered
                helperClass.getCurrentUser(database, androidId, SignUpFragment.this);

//                database.getReference().child("Users").child(androidId)
//                        .addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists())
//                                    currentUser = snapshot.getValue(User.class);
//
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        });

                if (currentUser != null) {
                    if (currentUser.getAndroidId().equals(androidId) &&
                            currentUser.getEmail().equals(email)) {
                        Toast.makeText(context, "Android Id and email address already exist",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else if (currentUser.getAndroidId().equals(androidId)) {
                        Toast.makeText(context, "Android Id already exists",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
                else {
                    createUser(userName, email, pass);
                }


//                dialog.dismiss();
            }
        });

        return binding.getRoot();
    }

    private void setAnim() {
        stateChanged= true;
        binding.userName.setTranslationX(800);
        binding.email.setTranslationX(800);
        binding.password.setTranslationX(800);
        binding.confirmPassword.setTranslationX(800);
        binding.registerBtn.setTranslationX(800);

        binding.userName.setAlpha(0f);
        binding.email.setAlpha(0f);
        binding.password.setAlpha(0f);
        binding.confirmPassword.setAlpha(0f);
        binding.registerBtn.setAlpha(0f);

        binding.userName.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        binding.email.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        binding.password.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        binding.confirmPassword.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        binding.registerBtn.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(1100).start();
    }

    private void createUser(String userName, String email, String pass) {
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                // sending user verification email
                Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();

                        Toast.makeText(getContext(), "Account Created Successfully.",
                                Toast.LENGTH_SHORT).show();

                        User user = new User(userName, email, pass, androidId, false);

                        database.getReference().child("Users").child(androidId).setValue(user);

                        resetViews();

                        AppCompatActivity appCompatActivity= (AppCompatActivity) getContext();
                        appCompatActivity.getSupportFragmentManager().beginTransaction().replace(R.id.activityCredentialsContainer,
                                new LoginFragment()).commit();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Log.d("check", "onFailure: " + e.getLocalizedMessage());
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Log.d("check", "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetViews() {
        binding.userName.setText("");
        binding.email.setText("");
        binding.password.setText("");
        binding.confirmPassword.setText("");
        binding.userName.clearFocus();
        binding.email.clearFocus();
        binding.password.clearFocus();
        binding.confirmPassword.clearFocus();
    }

    private boolean validateData(String userName, String email, String pass, String confirmPass) {
        if(userName.length()==0){
            binding.userName.setError("Username is required");
            binding.userName.requestFocus();
            return false;
        }
        else if(email.length()==0){
            binding.email.setError("Email is required");
            binding.email.requestFocus();
            return false;
        }
        else if(!email.contains("@students.muet.edu.pk")){
            binding.email.setError("Microsoft email required.");
            binding.email.requestFocus();
            return false;
        }
        else if(pass.length()==0){
            binding.password.setError("Password is required");
            binding.password.requestFocus();
            return false;
        }
        else if(confirmPass.length()==0){
            binding.confirmPassword.setError("Confirm the password");
            binding.confirmPassword.requestFocus();
            return false;
        }
        else if(!(pass.equals(confirmPass))){
            binding.confirmPassword.setError("Password doesn't match");
            binding.confirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stateChanged", stateChanged);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("stateChanged"))
                stateChanged = true;
        }
    }


    @Override
    public void currentUserCallBack(User currentUser) {
        if(currentUser != null)
            this.currentUser= currentUser;
    }
}