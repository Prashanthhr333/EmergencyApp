package com.example.sos;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private SmsManager smsManager;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        smsManager = SmsManager.getDefault();
        db = new DatabaseHelper(this);

        // Request necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
        }

        findViewById(R.id.sos_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSOS();
            }
        });
    }

    private void triggerSOS() {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                String locationUrl;
                if (location != null) {
                    locationUrl = "https://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                } else {
                    locationUrl = "Unable to Find Location :(";
                }

                // Send SMS to all contacts
                sendSMSToContacts(locationUrl);
            }
        });
    }

    private void sendSMSToContacts(String locationUrl) {
        ArrayList<ContactModel> contacts = db.fetchData();
        SharedPreferences sp = getSharedPreferences("message", MODE_PRIVATE);
        String defaultMessage = "I am in DANGER! Please urgently help me. My location:\n";

        String customMessage = sp.getString("msg", null);
        String message = customMessage != null ? customMessage : defaultMessage;

        for (ContactModel contact : contacts) {
            String finalMessage = "Hey " + contact.getName() + ", " + message + "\n" + locationUrl;
            smsManager.sendTextMessage(contact.getNumber(), null, finalMessage, null, null);
        }

        Toast.makeText(this, "SOS messages sent!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
