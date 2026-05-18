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

public class HotelService {

    private static final String[] OVERPASS_ENDPOINTS = {
            "https://overpass-api.de/api/interpreter",
            "https://lz4.overpass-api.de/api/interpreter",
            "https://z.overpass-api.de/api/interpreter"
    };

    public static class Hotel {
        public String name;
        public double lat;
        public double lon;
        public String osmLink;
        public String imageUrl;

        public Hotel(String name, double lat, double lon, String osmLink, String imageUrl) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.osmLink = osmLink;
            this.imageUrl = imageUrl;
        }
    }

    public static List<Hotel> getHotels(String city, String country) {
        List<Hotel> hotels = new ArrayList<>();

        try {
            String query = "hotel in " + city + ", " + country;
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&format=jsonv2&limit=8";

            String json = sendGet(url);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

            Set<String> seen = new HashSet<>();

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();

                String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : "";
                if (displayName.isEmpty()) continue;

                String name = displayName.split(",")[0].trim();
                if (name.isEmpty()) continue;

                double lat = obj.get("lat").getAsDouble();
                double lon = obj.get("lon").getAsDouble();

                String key = name.toLowerCase() + "|" + Math.round(lat * 10000) + "|" + Math.round(lon * 10000);
                if (seen.contains(key)) continue;
                seen.add(key);

                String osmLink = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lon
                        + "#map=17/" + lat + "/" + lon;

                String imageUrl = TravelService.getHotelImage(city, name);

                hotels.add(new Hotel(name, lat, lon, osmLink, imageUrl));

                if (hotels.size() >= 6) break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotels;
    }

    private static List<Hotel> fetchHotelsForRadius(double lat, double lon, String city, int radius, Set<String> seen) {
        List<Hotel> hotels = new ArrayList<>();

        String[] queryParts = {
                "node[\"tourism\"=\"hotel\"]",
                "way[\"tourism\"=\"hotel\"]",
                "node[\"tourism\"=\"guest_house\"]",
                "way[\"tourism\"=\"guest_house\"]"
        };

        for (String part : queryParts) {
            try {
                String query = "[out:json][timeout:12];("
                        + part + "(around:" + radius + "," + lat + "," + lon + ");"
                        + ");out center tags 12;";

                String json = sendOverpass(query);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonArray elements = root.getAsJsonArray("elements");
                if (elements == null) {
                    continue;
                }

                for (int i = 0; i < elements.size(); i++) {
                    JsonObject obj = elements.get(i).getAsJsonObject();
                    JsonObject tags = obj.has("tags") ? obj.getAsJsonObject("tags") : null;

                    if (tags == null || !tags.has("name")) {
                        continue;
                    }

                    String name = tags.get("name").getAsString().trim();
                    if (name.isEmpty()) {
                        continue;
                    }

                    double hLat;
                    double hLon;

                    if (obj.has("lat") && obj.has("lon")) {
                        hLat = obj.get("lat").getAsDouble();
                        hLon = obj.get("lon").getAsDouble();
                    } else if (obj.has("center")) {
                        JsonObject center = obj.getAsJsonObject("center");
                        hLat = center.get("lat").getAsDouble();
                        hLon = center.get("lon").getAsDouble();
                    } else {
                        continue;
                    }

                    String key = name.toLowerCase() + "|" + Math.round(hLat * 10000) + "|" + Math.round(hLon * 10000);
                    if (seen.contains(key)) {
                        continue;
                    }
                    seen.add(key);

                    String osmLink = "https://www.openstreetmap.org/?mlat=" + hLat + "&mlon=" + hLon
                            + "#map=17/" + hLat + "/" + hLon;

                    String imageUrl = getBestHotelImage(tags, city, name);

                    hotels.add(new Hotel(name, hLat, hLon, osmLink, imageUrl));

                    if (hotels.size() >= 6) {
                        return hotels;
                    }
                }

            } catch (Exception e) {
                System.out.println("Hotel query part failed: " + part + " -> " + e.getMessage());
            }
        }

        return hotels;
    }

    private static String getBestHotelImage(JsonObject tags, String city, String hotelName) {
        try {
            if (tags != null) {
                if (tags.has("image")) {
                    String img = cleanUrl(tags.get("image").getAsString());
                    if (isUsableImageUrl(img)) {
                        return img;
                    }
                }

                if (tags.has("wikimedia_commons")) {
                    String fileName = tags.get("wikimedia_commons").getAsString();
                    String img = wikimediaFileToThumb(fileName);
                    if (isUsableImageUrl(img)) {
                        return img;
                    }
                }

                if (tags.has("wikidata")) {
                    String qid = tags.get("wikidata").getAsString().trim();
                    String img = getWikidataImage(qid);
                    if (isUsableImageUrl(img)) {
                        return img;
                    }
                }

                if (tags.has("wikipedia")) {
                    String wiki = tags.get("wikipedia").getAsString().trim();
                    String img = getWikipediaPageImage(wiki);
                    if (isUsableImageUrl(img)) {
                        return img;
                    }
                }
            }

            String img = TravelService.getHotelImage(city, hotelName);
            if (isUsableImageUrl(img)) {
                return cleanUrl(img);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String cleanUrl(String url) {
        if (url == null) {
            return null;
        }
        url = url.trim();
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        return url;
    }

    private static boolean isUsableImageUrl(String url) {
        return url != null && !url.isEmpty()
                && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//"));
    }

    private static String wikimediaFileToThumb(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        fileName = fileName.replace("File:", "").trim().replace(" ", "_");
        return "https://commons.wikimedia.org/wiki/Special:FilePath/" + fileName + "?width=800";
    }

    private static String getWikidataImage(String qid) {
        try {
            String api = "https://www.wikidata.org/wiki/Special:EntityData/" + qid + ".json";
            String json = sendGet(api);

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject entities = root.getAsJsonObject("entities");
            if (entities == null || !entities.has(qid)) {
                return null;
            }

            JsonObject entity = entities.getAsJsonObject(qid);
            if (!entity.has("claims")) {
                return null;
            }

            JsonObject claims = entity.getAsJsonObject("claims");
            if (!claims.has("P18")) {
                return null;
            }

            JsonArray p18 = claims.getAsJsonArray("P18");
            if (p18 == null || p18.size() == 0) {
                return null;
            }

            JsonObject first = p18.get(0).getAsJsonObject();
            JsonObject mainsnak = first.getAsJsonObject("mainsnak");
            JsonObject datavalue = mainsnak.getAsJsonObject("datavalue");
            String fileName = datavalue.get("value").getAsString();

            return wikimediaFileToThumb(fileName);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getWikipediaPageImage(String wikipediaTag) {
        try {
            if (wikipediaTag == null || !wikipediaTag.contains(":")) {
                return null;
            }

            String[] parts = wikipediaTag.split(":", 2);
            String lang = parts[0].trim();
            String title = parts[1].trim().replace(" ", "_");

            String api = "https://" + lang
                    + ".wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&pithumbsize=800&titles="
                    + URLEncoder.encode(title, StandardCharsets.UTF_8);

            String json = sendGet(api);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject query = root.getAsJsonObject("query");
            if (query == null) {
                return null;
            }

            JsonObject pages = query.getAsJsonObject("pages");
            if (pages == null) {
                return null;
            }

            for (String key : pages.keySet()) {
                JsonObject page = pages.getAsJsonObject(key);
                if (page.has("thumbnail")) {
                    JsonObject thumb = page.getAsJsonObject("thumbnail");
                    if (thumb.has("source")) {
                        return thumb.get("source").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
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
        con.setRequestProperty("User-Agent", "SmartTravelPlanner/1.0 (student project)");
        con.setRequestProperty("Accept", "application/json,text/plain,*/*");

        int code = con.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("HTTP " + code + " for " + urlStr);
        }

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