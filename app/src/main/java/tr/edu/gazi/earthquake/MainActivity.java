package tr.edu.gazi.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.concurrent.CompletableFuture;

import tr.edu.gazi.earthquake.databinding.ActivityMainBinding;
import tr.edu.gazi.earthquake.models.Earthquake;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private ArrayList<Earthquake> earthquakes;
    private MapView map;
    private SharedPreferences sharedPreferences;

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
        getLastEarthquakes();
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
            ArrayList<Earthquake> earthquakes = new ArrayList<>();

            try {
                Document doc = Jsoup.connect("https://kandillirasathanesi.com/index.php").get();
                Elements rows = doc.select("table > tbody > tr");
                int maxRowNum = Math.min(rows.size(), 20);

                for (int i = 0; i < maxRowNum; i++) {
                    Earthquake earthquake = new Earthquake();
                    earthquake.setLocation(rows.get(i).select("td:eq(0)").text());
                    earthquake.setMagnitude(Double.parseDouble(rows.get(i).select("td:eq(1)").text()));
                    earthquake.setDepth(Double.parseDouble(rows.get(i).select("td:eq(2)").text()));
                    earthquake.setLatitude(Double.parseDouble(rows.get(i).select("td:eq(3)").text()));
                    earthquake.setLongitude(Double.parseDouble(rows.get(i).select("td:eq(4)").text()));
                    earthquake.setDate(rows.get(i).select("td:eq(5)").text());
                    earthquakes.add(earthquake);
                }
            } catch (IOException e) {
                Toast.makeText(this, "Son depremler alınırken hata ile karşılaşıldı!", Toast.LENGTH_SHORT).show();
            }

            return earthquakes;
        }).thenAcceptAsync(earthquakes -> {
            double north = Double.NEGATIVE_INFINITY;
            double south = Double.POSITIVE_INFINITY;
            double east = Double.NEGATIVE_INFINITY;
            double west = Double.POSITIVE_INFINITY;

            for (Earthquake earthquake : earthquakes) {
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(earthquake.getLatitude(), earthquake.getLongitude()));
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
            Toast.makeText(this, "Son depremler alındı!", Toast.LENGTH_SHORT).show();
        }, ContextCompat.getMainExecutor(this));
    }
}
