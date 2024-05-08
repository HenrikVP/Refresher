package dk.tec.refresher;

import static android.Manifest.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dk.tec.refresher.Model.ToiletLocation;

public class MainActivity extends AppCompatActivity {

    public static List<ToiletLocation> toilets;
    Location location;
    FusedLocationProviderClient flpc;
    TextView txt_lon, txt_lat, txt_alt, txt_dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize GUI elements
        initGui();

        // Initialize permissions list
        ArrayList<String> permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));

        // Request necessary permissions
        askForPermissions(permissionsList);

        // Initialize FusedLocationProviderClient
        flpc = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        // Initialize food list and set button click listener
        toilets = new ArrayList<>();

        //mrButton.setOnClickListener(view -> foodlist.add(new Food(input.getText().toString())));
    }


    // Initialize GUI elements
    private void initGui() {
        txt_lat = findViewById(R.id.txt_lat);
        txt_lon = findViewById(R.id.txt_lon);
        txt_alt = findViewById(R.id.txt_alt);
        txt_dir = findViewById(R.id.txt_dir);

        findViewById(R.id.btn_savetoiletlocation).setOnClickListener(view -> {
            new ToiletLocation(location);

        });
    }

    // Request location updates
    private void getUpdates() {
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, handle it here or request permission again
            return;
        }

        flpc = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        flpc.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                location = locationResult.getLastLocation();

                // Update UI with location information
                txt_lat.setText("Latitude: " + location.getLatitude());
                txt_lon.setText("Longitude: " + location.getLongitude());
                txt_alt.setText("Altitude: " + location.getAltitude());
                txt_dir.setText("Direction: " + location.getBearing());
            }
        }, Looper.myLooper());
    }

    // Permissions handling
    ArrayList<String> permissionsList;
    String[] permissionsStr = {
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.CAMERA,
            permission.RECORD_AUDIO
    };

    // Register permission request launcher
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            int permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                    permissionsList.add(permissionsStr[i]);
                                } else if (!hasPermission(MainActivity.this, permissionsStr[i])) {
                                    permissionsCount++;
                                }
                            }
                            if (!permissionsList.isEmpty()) {
                                // Request permissions again for ones that were denied
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                // Show permission dialog for permissions that cannot be requested again
                                showPermissionDialog();
                            } else {
                                // All permissions granted, proceed with necessary actions
                                getUpdates();
                            }
                        }
                    });

    // Check if permission is granted
    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    // Request permissions
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        newPermissionStr = permissionsList.toArray(newPermissionStr);
        if (newPermissionStr.length > 0) {
            // Launch permission request
            permissionsLauncher.launch(newPermissionStr);
        } else {
            // Show dialog to guide user to app settings for enabling permissions
            showPermissionDialog();
        }
    }

    // Show permission dialog
    AlertDialog alertDialog;
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are needed to be allowed to use this app without any problems.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }
}
