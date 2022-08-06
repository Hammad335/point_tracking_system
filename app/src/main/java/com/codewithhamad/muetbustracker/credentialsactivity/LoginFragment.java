package com.codewithhamad.muetbustracker.credentialsactivity;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codewithhamad.muetbustracker.helper.FirebaseDatabaseCallBack;
import com.codewithhamad.muetbustracker.helper.HelperClass;
import com.codewithhamad.muetbustracker.main.MainActivity;
import com.codewithhamad.muetbustracker.R;
import com.codewithhamad.muetbustracker.databinding.FragmentLoginBinding;
import com.codewithhamad.muetbustracker.models.Point;
import com.codewithhamad.muetbustracker.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class LoginFragment extends Fragment implements FirebaseDatabaseCallBack {

    FragmentLoginBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
    AlertDialog.Builder resetPassAlertDialog;
    LayoutInflater layoutInflater;
    String androidId = "";
    User currentUser = null;
    private HelperClass helperClass;
    private boolean stateChanged= false;
    private  Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            stateChanged= savedInstanceState.getBoolean("stateChanged");
        context= getContext();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = FragmentLoginBinding.inflate(inflater, container, false);


        // setting anim
         if(!stateChanged)
             setAnim();


        // init variables
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        resetPassAlertDialog = new AlertDialog.Builder(getContext());
        helperClass= new HelperClass(getContext());
        layoutInflater = this.getLayoutInflater();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        androidId = helperClass.getAndroidId();
        binding.androidId.setText(androidId);


        // getting current user
        helperClass.getCurrentUser(database, androidId, this);

        // on text change of password
        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUser();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // on text change of email
        binding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUser();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // adding onClick listener to loginBtn
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    progressDialog.setTitle("User Login");
                    progressDialog.setMessage("Login to your account...");

                    String email = binding.email.getText().toString();
                    String pass = binding.password.getText().toString();

                    if (!(validateData(email, pass))) {
//                    progressDialog.dismiss();
                        return;
                    }

                    // checking internet connectivity
                    if (!helperClass.haveNetworkConnection()) {
                        Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if (!helperClass.isOnline()) {
                        progressDialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Slow internet connection, try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }, 3000);

                        return;
                    }

                    // now the data is validated
                    progressDialog.show();

                    if (currentUser != null)
                        currentUser.setPermissionGranted(true);
                    else
                        return;

                    if (currentUser.isPermissionGranted() && pass.equals(currentUser.getPassword()) &&
                        email.equals(currentUser.getEmail())) {

                        try {
                            auth.signInWithEmailAndPassword(currentUser.getEmail(), pass)
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            // check if email is not verified
                                            if (auth != null && !(auth.getCurrentUser().isEmailVerified())) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "Kindly verify your gmail account through " +
                                                                "the mail we have sent you.",
                                                        Toast.LENGTH_SHORT).show();
                                                auth.signOut();
                                                return;
                                            }

                                            // save currentUser to sharedPref
                                            updatePointsToDatabase();
                                            saveToSharedPref();
                                            showToastMessage(progressDialog, "Login Successfully");

                                            startActivity(new Intent(getContext(), MainActivity.class));
                                            requireActivity().finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        catch (Exception e) {
                            progressDialog.dismiss();
                            Log.d("check", "onDataChange: " + e.getLocalizedMessage());
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    else if (currentUser != null && !pass.equals(currentUser.getPassword()))
                        showToastMessage(progressDialog, "Incorrect Password, try again");
                    else if (currentUser != null && !currentUser.isPermissionGranted())
                        showToastMessage(progressDialog, "Admin has not granted the permission");
                    else if (currentUser == null)
                        showToastMessage(progressDialog,
                            "Kindly register your account before login.");

                }
                catch (Exception e){
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    private void updatePointsToDatabase() {
        ArrayList<Point> allPoint= helperClass.getFavPointFromSharedPref("all");
        if(allPoint != null) {
            for (Point p : allPoint) {
                database.getReference().child("Points").child(p.getPointNumber()).setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void validateUser() {
        if (currentUser != null) {
            if (currentUser.getEmail().equals(binding.email.getText().toString()) &&
                    currentUser.getPassword().equals(binding.password.getText().toString())) {
                binding.loginBtn.setSelected(true);
                binding.loginBtn.setClickable(true);
                binding.loginBtn.setEnabled(true);
                binding.loginBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
            }
            else {
                binding.loginBtn.setSelected(false);
                binding.loginBtn.setClickable(false);
                binding.loginBtn.setEnabled(false);
                binding.loginBtn.setBackground(getResources().getDrawable(R.drawable.button_background_disabled));
            }
        }
    }

    private void showToastMessage(ProgressDialog progressDialog, String toastMessage) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            }
        }, 3000);
    }

    private void saveToSharedPref() {
        if(currentUser != null) {
            SharedPreferences sharedPreferences= getContext().getSharedPreferences("sharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(currentUser);
            editor.putString("currentUser", json);
            editor.apply();
        }
    }

    private void setAnim() {
        stateChanged= true;
        binding.androidId.setTranslationX(800);
        binding.email.setTranslationX(800);
        binding.password.setTranslationX(800);
        binding.forgotPass.setTranslationX(800);
        binding.loginBtn.setTranslationX(800);

        binding.androidId.setAlpha(0f);
        binding.email.setAlpha(0f);
        binding.password.setAlpha(0f);
        binding.forgotPass.setAlpha(0f);
        binding.loginBtn.setAlpha(0f);

        binding.androidId.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        binding.email.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        binding.password.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        binding.forgotPass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        binding.loginBtn.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(1100).start();
    }

    private boolean validateData(String email, String pass) {
        if (email.length() == 0) {
            binding.email.setError("Email is required");
            binding.email.requestFocus();
            return false;
        }
        else if (pass.length() == 0) {
            binding.password.setError("Password is required");
            binding.password.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stateChanged", stateChanged);
        if (currentUser != null) {
            if (currentUser.getPassword().equals(binding.password.getText().toString()))
                outState.putBoolean("isPassEqual", true);

        }
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            if(savedInstanceState.getBoolean("stateChanged"))
                stateChanged = true;

//            if(savedInstanceState.getBoolean("isPassEqual"))
//                binding.androidId.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                        R.drawable.pass_check, 0);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(progressDialog != null && progressDialog.isShowing())
        progressDialog.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.cancel();
    }

    @Override
    public void currentUserCallBack(User currentUser) {
        if(currentUser != null)
            this.currentUser= currentUser;
    }
}
