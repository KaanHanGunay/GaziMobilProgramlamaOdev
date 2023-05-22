package tr.edu.gazi.earthquake.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tr.edu.gazi.earthquake.models.Earthquake;

public class EarthquakeService {
    public List<Earthquake> getLastEarthquakes() throws IOException {
        ArrayList<Earthquake> earthquakes = new ArrayList<>();
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
        return earthquakes;
    }
}
