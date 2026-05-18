package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TripForm extends JFrame implements ActionListener {

    JLabel titleLabel;
    JLabel sourceLabel, destinationLabel, daysLabel, stayLabel;
    JTextField sourceField, destinationField, daysField, stayField;
    JButton saveButton, clearButton, viewLastTripButton;

    public TripForm() {
        setTitle("Smart Travel Planner - Trip Form");
        setSize(550, 420);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        titleLabel = new JLabel("SMART TRAVEL PLANNER - TRIP FORM");
        titleLabel.setBounds(110, 20, 350, 30);
        add(titleLabel);

        sourceLabel = new JLabel("Source:");
        sourceLabel.setBounds(60, 80, 120, 30);
        add(sourceLabel);

        sourceField = new JTextField();
        sourceField.setBounds(200, 80, 220, 30);
        add(sourceField);

        destinationLabel = new JLabel("Destination:");
        destinationLabel.setBounds(60, 130, 120, 30);
        add(destinationLabel);

        destinationField = new JTextField();
        destinationField.setBounds(200, 130, 220, 30);
        add(destinationField);

        daysLabel = new JLabel("No of Days:");
        daysLabel.setBounds(60, 180, 120, 30);
        add(daysLabel);

        daysField = new JTextField();
        daysField.setBounds(200, 180, 220, 30);
        add(daysField);

        stayLabel = new JLabel("Place to Stay:");
        stayLabel.setBounds(60, 230, 120, 30);
        add(stayLabel);

        stayField = new JTextField();
        stayField.setBounds(200, 230, 220, 30);
        add(stayField);

        saveButton = new JButton("Save Trip");
        saveButton.setBounds(60, 300, 120, 35);
        saveButton.addActionListener(this);
        add(saveButton);

        clearButton = new JButton("Clear");
        clearButton.setBounds(210, 300, 120, 35);
        clearButton.addActionListener(this);
        add(clearButton);

        viewLastTripButton = new JButton("View Last Trip");
        viewLastTripButton.setBounds(360, 300, 130, 35);
        viewLastTripButton.addActionListener(this);
        add(viewLastTripButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveTrip();
        } else if (e.getSource() == clearButton) {
            clearFields();
        } else if (e.getSource() == viewLastTripButton) {
            viewLastTrip();
        }
    }

    private void saveTrip() {
        String source = sourceField.getText().trim();
        String destination = destinationField.getText().trim();
        String daysText = daysField.getText().trim();
        String placeToStay = stayField.getText().trim();

        if (source.isEmpty() || destination.isEmpty() || daysText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Source, Destination and No of Days are required.");
            return;
        }

        int noOfDays;
        try {
            noOfDays = Integer.parseInt(daysText);
            if (noOfDays <= 0) {
                JOptionPane.showMessageDialog(this, "No of Days must be greater than 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for No of Days.");
            return;
        }

        String sql = "INSERT INTO trip(source, destination, no_of_days, place_to_stay) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, source);
            pst.setString(2, destination);
            pst.setInt(3, noOfDays);
            pst.setString(4, placeToStay);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Trip saved successfully.");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Trip not saved.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        sourceField.setText("");
        destinationField.setText("");
        daysField.setText("");
        stayField.setText("");
    }

    private void viewLastTrip() {
        String sql = "SELECT * FROM trip ORDER BY trip_id DESC LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String msg =
                        "Trip ID: " + rs.getInt("trip_id") + "\n" +
                        "Source: " + rs.getString("source") + "\n" +
                        "Destination: " + rs.getString("destination") + "\n" +
                        "No of Days: " + rs.getInt("no_of_days") + "\n" +
                        "Place to Stay: " + rs.getString("place_to_stay");

                JOptionPane.showMessageDialog(this, msg, "Last Saved Trip", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No trip records found.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new TripForm();
    }
}