/*
 * created by Kirill Kultinov
 */
package com.team24.uasdelivery;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;



public class AboutFragment extends Fragment {

    View view;
    public AboutFragment() {
        // Required empty public constructor
    }

    boolean scrollViewHidden = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tv1 = (TextView) view.findViewById(R.id.aboutTextView1);
        TextView tv2 = (TextView) view.findViewById(R.id.aboutTextView2);
        TextView tv3 = (TextView) view.findViewById(R.id.aboutTextView3);
        Button userManual = (Button) view.findViewById(R.id.userManualButton);
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.userManualScrollView);

        final TextView step1 = (TextView) view.findViewById(R.id.step1);
        final TextView step1Description = (TextView) view.findViewById(R.id.step1Description);

        final TextView step2 = (TextView) view.findViewById(R.id.step2);
        final TextView step2Description = (TextView) view.findViewById(R.id.step2Description);

        final TextView step3 = (TextView) view.findViewById(R.id.step3);
        final TextView step3Description = (TextView) view.findViewById(R.id.step3Description);

        final TextView step4 = (TextView) view.findViewById(R.id.step4);
        final TextView step4Description = (TextView) view.findViewById(R.id.step4Description);

        final TextView step5 = (TextView) view.findViewById(R.id.step5);
        final TextView step5Description = (TextView) view.findViewById(R.id.step5Description);

        //onClick listener for userManual button
        userManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(scrollViewHidden){
                    scrollViewHidden = false;
                }
                else{
                    scrollViewHidden = true;
                }
                if(scrollViewHidden){
                    step1.setText("");
                    step1Description.setText("");
                    step2.setText("");
                    step2Description.setText("");
                    step3.setText("");
                    step3Description.setText("");
                    step4.setText("");
                    step4Description.setText("");
                    step5.setText("");
                    step5Description.setText("");
                }
                else{
                    step1.setText("Step 1:");
                    step1Description.setText("Login into your account on Delivery Status page. You will see delivery status of your package" +
                            " along with Drone's name that deliveres your package and a verification code that will be used for pairing the drone " +
                            "with your device.");

                    step2.setText("Step 2:");
                    step2Description.setText("When package has arrived, remember the verification code provided inside Delivery Status tab.");

                    step3.setText("Step 3:");
                    step3Description.setText("Press Pair button in order to open Bluetooth settings. Choose BLuetooth device " +
                            "that has Drone's name. Use the verification code for pairing");

                    step4.setText("Step 4:");
                    step4Description.setText("Go back to the application. Now verify button will be enabled.");

                    step5.setText("Step 5:");
                    step5Description.setText("Press verify button. Wait a couple of seconds until the delivery of your package is verified.");
                }
            }
        });

        return view;
    }

}
