package service;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TravelService {

    public static class Place {
        public String name;
        public double lat;
        public double lon;

        public Place(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class CorePlanResult {
        public double distanceKm;
        public double durationHr;
        public String transportMode;
        public String osmProofLink;
        public String sourceLandmark;
        public String destinationLandmark;
        public String sourceImageUrl;
        public String destinationImageUrl;
    }

    private static final String PEXELS_API_KEY = "sh7vjejwjtjqnd36pcgvvigyy2vcvdumh1g5gocyulxmwmwk823xpdo0";

    public static Place geocodePlace(String placeName) throws Exception {
        String encoded = URLEncoder.encode(placeName, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=jsonv2&limit=1";

        String json = sendGet(url);
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

        if (arr.size() == 0) {
            return null;
        }

        JsonObject obj = arr.get(0).getAsJsonObject();
        double lat = obj.get("lat").getAsDouble();
        double lon = obj.get("lon").getAsDouble();

        return new Place(placeName, lat, lon);
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * r * Math.asin(Math.sqrt(a));
    }

    public static String chooseTransport(double distanceKm) {
        if (distanceKm <= 2) return "Walk";
        if (distanceKm <= 20) return "Bike / Cab";
        if (distanceKm <= 350) return "Car / Bus";
        if (distanceKm <= 1500) return "Train / Flight";
        return "Flight";
    }

    public static double estimateDurationHours(double distanceKm) {
        if (distanceKm <= 2) return distanceKm / 5.0;
        if (distanceKm <= 20) return distanceKm / 30.0;
        if (distanceKm <= 350) return distanceKm / 60.0;
        if (distanceKm <= 1500) return distanceKm / 120.0;
        return distanceKm / 800.0 + 3.0;
    }

    public static String osmRouteLink(double lat1, double lon1, double lat2, double lon2) {
        return "https://www.openstreetmap.org/directions?engine=fossgis_osrm_car"
                + "&route=" + lat1 + "%2C" + lon1 + "%3B" + lat2 + "%2C" + lon2;
    }
    public static String getFixedLandmarkImage(String place) {
        if (place == null) return null;

        switch (place.toLowerCase()) {
            case "ariyalur":
            case "gangaikonda cholapuram temple":
                return "https://images.pexels.com/photos/18696159/pexels-photo-18696159.jpeg";

            case "coimbatore":
            case "adiyogi":
                return "https://images.pexels.com/photos/1134166/pexels-photo-1134166.jpeg";

            case "tirunelveli":
            case "nellaiappar temple":
                return "https://images.pexels.com/photos/161154/stained-glass-spiral-circle-pattern-161154.jpeg";

            case "chennai":
            case "marina beach":
                return "https://images.pexels.com/photos/1032650/pexels-photo-1032650.jpeg";

            case "paris":
            case "eiffel tower":
                return "https://images.pexels.com/photos/338515/pexels-photo-338515.jpeg";

            case "vienna":
            case "schonbrunn palace":
                return "https://images.pexels.com/photos/378570/pexels-photo-378570.jpeg";

            case "zurich":
            case "lake zurich":
                return "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg";

            case "berlin":
            case "brandenburg gate":
                return "https://images.pexels.com/photos/19340933/pexels-photo-19340933.jpeg";

            default:
                return null;
        }
    }

    public static String getLandmarkName(String place) {
        if (place == null) return "";

        switch (place.toLowerCase()) {
            case "ariyalur": return "Gangaikonda Cholapuram Temple";
            case "coimbatore": return "Adiyogi";
            case "madurai": return "Meenakshi Temple";
            case "tirunelveli": return "Nellaiappar Temple";
            case "thuthukudi": return "Our Lady of Snows Basilica";
            case "chennai": return "Marina Beach";
            case "paris": return "Eiffel Tower";
            case "vienna": return "Schonbrunn Palace";
            case "zurich": return "Lake Zurich";
            case "rome": return "Colosseum";
            case "berlin": return "Brandenburg Gate";
            case "madrid": return "Plaza Mayor";
            case "amsterdam": return "Amsterdam Canals";
            default: return place;
        }
    }
    

    public static String getPexelsImage(String query) {
        try {
            String apiKey = PEXELS_API_KEY.trim();   // important

            String urlStr = "https://api.pexels.com/v1/search?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&per_page=1";

            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", apiKey);
            con.setRequestProperty("User-Agent", "SmartTravelPlanner/1.0");

            int status = con.getResponseCode();
            System.out.println("Pexels query: " + query);
            System.out.println("Pexels status: " + status);

            BufferedReader br;
            if (status >= 200 && status < 300) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            String jsonText = response.toString();
            System.out.println("Pexels response: " + jsonText);

            if (status < 200 || status >= 300) {
                return null;
            }

            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            JsonArray photos = json.getAsJsonArray("photos");

            if (photos != null && photos.size() > 0) {
                JsonObject photo = photos.get(0).getAsJsonObject();
                JsonObject src = photo.getAsJsonObject("src");

                if (src.has("medium")) return src.get("medium").getAsString();
                if (src.has("small")) return src.get("small").getAsString();
                if (src.has("large")) return src.get("large").getAsString();
                if (src.has("original")) return src.get("original").getAsString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static String getPexelsImageSmart(String mainQuery, String fallback1, String fallback2) {
        String img = getPexelsImage(mainQuery);
        if (img != null && !img.isEmpty()) return img;

        img = getPexelsImage(fallback1);
        if (img != null && !img.isEmpty()) return img;

        img = getPexelsImage(fallback2);
        if (img != null && !img.isEmpty()) return img;

        return null;
    }
    public static CorePlanResult buildCorePlan(String sourceDistrict, String sourceState,
                                               String destinationCity, String destinationCountry) throws Exception {

        String sourceText = sourceDistrict + ", " + sourceState + ", India";
        String destText = destinationCity + ", " + destinationCountry;

        Place src = geocodePlace(sourceText);
        Place dest = geocodePlace(destText);

        if (src == null || dest == null) {
            throw new Exception("Location not found");
        }

        double distance = haversineKm(src.lat, src.lon, dest.lat, dest.lon);
        double time = estimateDurationHours(distance);
        String transport = chooseTransport(distance);
        String routeLink = osmRouteLink(src.lat, src.lon, dest.lat, dest.lon);

        String sourceLandmark = getLandmarkName(sourceDistrict);
        String destinationLandmark = getLandmarkName(destinationCity);

        CorePlanResult result = new CorePlanResult();
        result.distanceKm = Math.round(distance * 100.0) / 100.0;
        result.durationHr = Math.round(time * 10.0) / 10.0;
        result.transportMode = transport;
        result.osmProofLink = routeLink;

        result.sourceLandmark = sourceLandmark;
        result.destinationLandmark = destinationLandmark;
        String sourceFixed = getFixedLandmarkImage(sourceDistrict);
        String destFixed = getFixedLandmarkImage(destinationCity);

        result.sourceImageUrl = (sourceFixed != null) ? sourceFixed
                : getPexelsImageSmart(
                    sourceLandmark + " famous place",
                    sourceDistrict + " tourism",
                    "India tourist place"
                );

        result.destinationImageUrl = (destFixed != null) ? destFixed
                : getPexelsImageSmart(
                    destinationLandmark + " famous place",
                    destinationCity + " tourism",
                    destinationCountry + " travel"
                );
        if (result.sourceImageUrl == null) {
            result.sourceImageUrl = "https://images.pexels.com/photos/346885/pexels-photo-346885.jpeg";
        }

        if (result.destinationImageUrl == null) {
            result.destinationImageUrl = "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg";
        }
        return result;
    }
    
    public static String getHotelImage(String cityName, String hotelName) {
        String img = getPexelsImage(cityName + " hotel");
        if (img != null) return img;

        img = getPexelsImage(hotelName + " hotel");
        if (img != null) return img;

        img = getPexelsImage("hotel room");
        if (img != null) return img;

        return null;
    }

    public static void openInBrowser(String link) {
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "SmartTravelPlanner/1.0");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
        );

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        return sb.toString();
    }
}