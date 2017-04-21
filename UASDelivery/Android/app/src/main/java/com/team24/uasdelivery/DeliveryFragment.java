/*
 * created by Kirill Kultinov
 */
package com.team24.uasdelivery;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;



public class DeliveryFragment extends Fragment implements View.OnClickListener{

    View view;
    EditText loginField;
    EditText passwordField;

    private FirebaseAuth authFire;
    private AuthStateListener authListener;

    Button loginButton;

    public DeliveryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_delivery, container, false);

        loginButton = (Button)view.findViewById(R.id.loginButton);
        loginField  = (EditText) view.findViewById(R.id.loginEditText);
        passwordField  = (EditText) view.findViewById(R.id.passwordEditText);


        loginButton.setOnClickListener(this);
        //set up everything for Firebase authentication
        authFire = FirebaseAuth.getInstance();
        authListener = new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {

                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        return view;
    }




    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.loginButton:
                String email = loginField.getText().toString();
                email = email.replace(" ", "");
                String password = passwordField.getText().toString();
                authFire.signInWithEmailAndPassword(email, password).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                if (!task.isSuccessful()) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Invalid Input")
                                            .setMessage("The username and/or password are incorrect. Try Again!")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue with delete
                                                }
                                            }).show();
                                }else {

                                    Toast.makeText(getActivity(), "loged in ", Toast.LENGTH_SHORT).show();
                                    FragmentManager fragmentManager = getFragmentManager();
                                    fragmentManager.beginTransaction().add(R.id.frame, new DeliveryLogedInFragment(), "DeliveryLogedInFragment").commit();
                                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("DeliveryFragment")).commit();
                                }
                            }
                        });

                break;


        }
    }


    @Override
    public void onStart() {
        super.onStart();
        authFire.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            authFire.removeAuthStateListener(authListener);
        }
    }


}
