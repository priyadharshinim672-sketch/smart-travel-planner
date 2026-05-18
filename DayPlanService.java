package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DayPlanService {

    private static final String[] OVERPASS_ENDPOINTS = {
            "https://overpass-api.de/api/interpreter",
            "https://lz4.overpass-api.de/api/interpreter",
            "https://z.overpass-api.de/api/interpreter"
    };

    public static class DayPlan {
        public int dayNumber;
        public List<PlanStop> stops = new ArrayList<>();

        public DayPlan(int dayNumber) {
            this.dayNumber = dayNumber;
        }
    }

    public static class PlanStop {
        public String placeName;
        public String category;
        public String startTime;
        public String endTime;
        public String transportMode;
        public double lat;
        public double lon;
        public String routeLink;
        public boolean selected;

        public PlanStop(String placeName, String category, String startTime, String endTime,
                        String transportMode, double lat, double lon, String routeLink) {
            this.placeName = placeName;
            this.category = category;
            this.startTime = startTime;
            this.endTime = endTime;
            this.transportMode = transportMode;
            this.lat = lat;
            this.lon = lon;
            this.routeLink = routeLink;
            this.selected = false;
        }
    }

    public static class Place {
        public String name;
        public String category;
        public double lat;
        public double lon;

        public Place(String name, String category, double lat, double lon) {
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static List<DayPlan> buildDayPlans(String city, String country, int noOfDays,
                                              String stayName, double stayLat, double stayLon) throws Exception {
        List<Place> attractions = fetchRealPlaces(city, country);
        List<DayPlan> plans = new ArrayList<>();

        if (attractions.isEmpty()) {
            return plans;
        }

        int placesPerDay = 3;
        int index = 0;

        for (int d = 1; d <= noOfDays && index < attractions.size(); d++) {
            DayPlan dayPlan = new DayPlan(d);

            double prevLat = stayLat;
            double prevLon = stayLon;

            String[] timeSlots = {"09:00 AM", "11:30 AM", "02:30 PM"};
            String[] endSlots = {"10:30 AM", "01:00 PM", "04:00 PM"};

            for (int i = 0; i < placesPerDay && index < attractions.size(); i++, index++) {
                Place p = attractions.get(index);
                double dist = haversineKm(prevLat, prevLon, p.lat, p.lon);
                String mode = suggestLocalTransport(dist);
                String route = buildRouteLink(prevLat, prevLon, p.lat, p.lon);

                dayPlan.stops.add(new PlanStop(
                        p.name,
                        p.category,
                        timeSlots[i],
                        endSlots[i],
                        mode,
                        p.lat,
                        p.lon,
                        route
                ));

                prevLat = p.lat;
                prevLon = p.lon;
            }

            double backDist = haversineKm(prevLat, prevLon, stayLat, stayLon);
            String backMode = suggestLocalTransport(backDist);
            String backRoute = buildRouteLink(prevLat, prevLon, stayLat, stayLon);

            dayPlan.stops.add(new PlanStop(
                    stayName,
                    "Return to Stay",
                    "07:00 PM",
                    "08:00 PM",
                    backMode,
                    stayLat,
                    stayLon,
                    backRoute
            ));

            plans.add(dayPlan);
        }

        return plans;
    }

    private static List<Place> fetchRealPlaces(String city, String country) throws Exception {
        List<Place> finalPlaces = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        TravelService.Place dest = TravelService.geocodePlace(city + ", " + country);
        if (dest == null) {
            return finalPlaces;
        }

        int[] radii = {1800, 3000, 4500};

        String[] queryParts = {
                "node[\"tourism\"=\"attraction\"]",
                "way[\"tourism\"=\"attraction\"]",
                "node[\"tourism\"=\"museum\"]",
                "way[\"tourism\"=\"museum\"]",
                "node[\"historic\"]",
                "way[\"historic\"]",
                "node[\"leisure\"=\"park\"]",
                "way[\"leisure\"=\"park\"]"
        };

        for (int radius : radii) {
            for (String part : queryParts) {
                String query = "[out:json][timeout:18];(" +
                        part + "(around:" + radius + "," + dest.lat + "," + dest.lon + ");" +
                        ");out center tags 25;";

                try {
                    String json = sendOverpass(query);
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    JsonArray elements = root.getAsJsonArray("elements");
                    if (elements == null) continue;

                    for (int i = 0; i < elements.size(); i++) {
                        JsonObject obj = elements.get(i).getAsJsonObject();
                        JsonObject tags = obj.has("tags") ? obj.getAsJsonObject("tags") : null;
                        if (tags == null || !tags.has("name")) continue;

                        String name = tags.get("name").getAsString().trim();
                        if (name.isEmpty()) continue;

                        double lat;
                        double lon;

                        if (obj.has("lat") && obj.has("lon")) {
                            lat = obj.get("lat").getAsDouble();
                            lon = obj.get("lon").getAsDouble();
                        } else if (obj.has("center")) {
                            JsonObject center = obj.getAsJsonObject("center");
                            lat = center.get("lat").getAsDouble();
                            lon = center.get("lon").getAsDouble();
                        } else {
                            continue;
                        }

                        String key = name.toLowerCase() + "|" + Math.round(lat * 10000) + "|" + Math.round(lon * 10000);
                        if (seen.contains(key)) continue;
                        seen.add(key);

                        String category = "Attraction";
                        if (tags.has("tourism")) category = tags.get("tourism").getAsString();
                        else if (tags.has("historic")) category = "historic";
                        else if (tags.has("leisure")) category = tags.get("leisure").getAsString();

                        finalPlaces.add(new Place(name, category, lat, lon));
                        if (finalPlaces.size() >= noRepeatTargetLimit(noOfDaysHint())) {
                            return finalPlaces;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Day plan partial fetch failed: " + part + " radius " + radius + " -> " + e.getMessage());
                }
            }
            if (!finalPlaces.isEmpty()) break;
        }

        return finalPlaces;
    }

    private static int noRepeatTargetLimit(int days) {
        return Math.max(6, days * 3);
    }

    private static int noOfDaysHint() {
        return 6;
    }

    public static String suggestLocalTransport(double km) {
        if (km <= 1.0) return "Walk";
        if (km <= 3.0) return "Bike/Auto";
        if (km <= 12.0) return "Cab/Car";
        return "Cab/Metro";
    }

    public static String buildRouteLink(double fromLat, double fromLon, double toLat, double toLon) {
        return "https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route="
                + fromLat + "%2C" + fromLon + "%3B" + toLat + "%2C" + toLon;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static String sendOverpass(String query) throws Exception {
        Exception last = null;

        for (String endpoint : OVERPASS_ENDPOINTS) {
            try {
                String url = endpoint + "?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
                return sendGet(url);
            } catch (Exception e) {
                last = e;
                System.out.println("Overpass failed: " + endpoint + " -> " + e.getMessage());
            }
        }

        throw last != null ? last : new RuntimeException("All Overpass endpoints failed");
    }

    private static String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(true);
        con.setRequestMethod("GET");
        con.setConnectTimeout(12000);
        con.setReadTimeout(20000);
        con.setRequestProperty("User-Agent", "SmartTravelPlanner/1.0");
        con.setRequestProperty("Accept", "application/json,text/plain,*/*");

        int code = con.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("HTTP " + code + " for " + urlStr);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}