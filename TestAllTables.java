package db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestAllTables {

    public static void main(String[] args) {

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                System.out.println("Database connection failed");
                return;
            }

            System.out.println("Database connected successfully\n");

            Statement st = con.createStatement();

            // ✅ Check trip table
            System.out.println("---- TRIP TABLE ----");
            ResultSet rs1 = st.executeQuery("SELECT * FROM trip");
            while (rs1.next()) {
                System.out.println(
                    rs1.getInt("trip_id") + " | " +
                    rs1.getString("source") + " | " +
                    rs1.getString("destination")
                );
            }

            // ✅ Check visited_places table
            System.out.println("\n---- VISITED PLACES TABLE ----");
            ResultSet rs2 = st.executeQuery("SELECT * FROM visited_places");
            while (rs2.next()) {
                System.out.println(
                    rs2.getInt("visit_id") + " | " +
                    rs2.getString("place_name")
                );
            }

            // ✅ Check budget table
            System.out.println("\n---- BUDGET TABLE ----");
            ResultSet rs3 = st.executeQuery("SELECT * FROM budget");
            while (rs3.next()) {
                System.out.println(
                    rs3.getInt("budget_id") + " | " +
                    rs3.getDouble("total_amount")
                );
            }

            // ✅ Check review table
            System.out.println("\n---- REVIEW TABLE ----");
            ResultSet rs4 = st.executeQuery("SELECT * FROM review");
            while (rs4.next()) {
                System.out.println(
                    rs4.getInt("review_id") + " | Rating: " +
                    rs4.getInt("rating")
                );
            }

            con.close();

            System.out.println("\nAll tables accessed successfully ✅");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}