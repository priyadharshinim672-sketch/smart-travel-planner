package db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReviewDAO {

    public static boolean saveReview(int tripId, int rating, String reviewText) {
        String sql = "INSERT INTO review (trip_id, rating, review_text) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, tripId);
            pst.setInt(2, rating);
            pst.setString(3, reviewText);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}