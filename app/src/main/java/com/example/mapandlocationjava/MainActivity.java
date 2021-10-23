package com.example.mapandlocationjava;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static final int REQUEST_CODE = 9302;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng currentPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        String[] perms = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Location Permission", REQUEST_CODE, perms);
        } else {
            updateUI(savedInstanceState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "permission provided", Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    private void updateUI(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.DARK, style -> {
            this.mapboxMap = mapboxMap;
            enableLocationComponent(style);
            UiSettings uiSettings = mapboxMap.getUiSettings();
            uiSettings.setAllGesturesEnabled(true);
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPoint,15));
            LatLng point1 = new LatLng(37.784282779035216,-122.4232292175293);
            CameraPosition position = new CameraPosition.Builder()
                    .target(point1) // Sets the new camera position
                    .zoom(12) // Sets the zoom
                    .bearing(180) // Rotate the camera
                    .tilt(30) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 7000);
            mapboxMap.addOnMapClickListener(point -> {

                CameraPosition position1 = new CameraPosition.Builder()
                        .target(point) // Sets the new camera position
                        .zoom(17) // Sets the zoom
                        .bearing(180) // Rotate the camera
                        .tilt(45) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position1), 7000);
                return true;
            });
            style.addSource(
                    new VectorSource("population", "mapbox://peterqliu.d0vin3el")
            );
            addFillsLayer(style);
            addExtrusionsLayer(style);
        }));

    }
    private void addFillsLayer(@NonNull Style loadedMapStyle) {
        FillLayer fillsLayer = new FillLayer("fills", "population");
        fillsLayer.setSourceLayer("outgeojson");
        fillsLayer.setFilter(all(lt(get("pkm2"), literal(300000))));
        fillsLayer.withProperties(
                fillColor(interpolate(exponential(1f), get("pkm2"),
                        stop(0, rgb(22, 14, 35)),
                        stop(14500, rgb(0, 97, 127)),
                        stop(145000, rgb(85, 223, 255)))));
        loadedMapStyle.addLayerBelow(fillsLayer, "water");
    }

    private void addExtrusionsLayer(@NonNull Style loadedMapStyle) {
        FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer("extrusions", "population");
        fillExtrusionLayer.setSourceLayer("outgeojson");
        fillExtrusionLayer.setFilter(all(gt(get("p"), 1), lt(get("pkm2"), 300000)));
        fillExtrusionLayer.withProperties(
                fillExtrusionColor(interpolate(exponential(1f), get("pkm2"),
                        stop(0, rgb(22, 14, 35)),
                        stop(14500, rgb(0, 97, 127)),
                        stop(145000, rgb(85, 233, 255)))),
                fillExtrusionBase(0f),
                fillExtrusionHeight(interpolate(exponential(1f), get("pkm2"),
                        stop(0, 0f),
                        stop(1450000, 20000f))));
        loadedMapStyle.addLayerBelow(fillExtrusionLayer, "airport-label");
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "App cannot work", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                .pulseEnabled(true)
                .accuracyAnimationEnabled(true)
                .pulseColor(Color.GREEN)
                .build();

        // Get an instance of the component
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build());

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);
        Location loc = locationComponent.getLastKnownLocation();
        currentPoint = new LatLng(loc.getLatitude(), loc.getLongitude());

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}