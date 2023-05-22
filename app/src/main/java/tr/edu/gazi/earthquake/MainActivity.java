package tr.edu.gazi.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tr.edu.gazi.earthquake.databinding.ActivityMainBinding;
import tr.edu.gazi.earthquake.models.Earthquake;
import tr.edu.gazi.earthquake.service.EarthquakeService;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private List<Earthquake> earthquakes;
    private MapView map;
    private SharedPreferences sharedPreferences;
    private EarthquakeService earthquakeService;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        sharedPreferences = getSharedPreferences("Main", Context.MODE_PRIVATE);
        Configuration.getInstance().load(getApplicationContext(), sharedPreferences);
        map = binding.map;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(6.5);
        map.getController().setCenter(new GeoPoint(39.0570, 34.4641));
        earthquakeService = new EarthquakeService();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                binding.loadingSpinner.setVisibility(View.VISIBLE);
                getLastEarthquakes();
                handler.postDelayed(this, 60000);
            }
        };

        handler.post(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    public void getLastEarthquakes() {
        CompletableFuture.supplyAsync(() -> {
            List<Earthquake> earthquakes = new ArrayList<>();
            try {
                return earthquakeService.getLastEarthquakes();
            } catch (IOException e) {
                Toast.makeText(this, "Son depremler alınırken hata ile karşılaşıldı!",
                        Toast.LENGTH_SHORT).show();
            }
            return earthquakes;
        }).thenAcceptAsync(earthquakes -> {
            double north = Double.NEGATIVE_INFINITY;
            double south = Double.POSITIVE_INFINITY;
            double east = Double.NEGATIVE_INFINITY;
            double west = Double.POSITIVE_INFINITY;
            map.getOverlays().clear();

            for (Earthquake earthquake : earthquakes) {
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(earthquake.getLatitude(),
                        earthquake.getLongitude()));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(earthquake.getLocation());
                marker.setSubDescription(earthquake.toString());
                map.getOverlays().add(marker);
                north = Math.max(earthquake.getLatitude(), north);
                south = Math.min(earthquake.getLatitude(), south);
                east = Math.max(earthquake.getLongitude(), east);
                west = Math.min(earthquake.getLongitude(), west);
            }

            BoundingBox boundingBox = new BoundingBox(north, east + 0.5, south, west - 0.5);
            map.zoomToBoundingBox(boundingBox, true);

            this.earthquakes = earthquakes;
            Toast.makeText(this, "Son depremler güncellendi!", Toast.LENGTH_SHORT).show();
            binding.loadingSpinner.setVisibility(View.GONE);
        }, ContextCompat.getMainExecutor(this));
    }
}
