package ui;

import service.TravelService;
import service.TravelService.CorePlanResult;
import service.HotelService;
import service.HotelService.Hotel;
import service.DayPlanService;
import service.AttractionService;
import service.BudgetService;
import service.PdfService;
import java.util.ArrayList;
import service.ReviewService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.io.File;

public class MainDashboard extends JFrame implements ActionListener {

    int currentTripId = -1;
    String selectedPlaceToStay = null;
    String currentProofLink = null;
    List<Hotel> currentHotels;
    List<DayPlanService.DayPlan> currentDayPlans;
    int selectedRating = 0;
    String submittedReview = "";
    JButton[] starButtons = new JButton[5];
    JLabel ratingValueLabel;
    JTextArea submittedReviewArea;

    double selectedHotelLat = 0.0;
    double selectedHotelLon = 0.0;

    JLabel planeLabel;
    Timer planeTimer;
    int planeX = 240;

    JLabel titleLabel, subtitleLabel, homeLabel, statusLabel;
    JLabel sourceStateLabel, sourceDistrictLabel, destCountryLabel, destCityLabel, daysLabel;

    JComboBox<String> sourceStateBox, sourceDistrictBox, destCountryBox, destCityBox;
    JTextField daysField;

    JButton generateButton, resetButton;
    JButton placeStayButton, dayPlanButton, budgetButton, pdfButton, reviewButton;

    JPanel mainPanel, headerPanel, formPanel, buttonPanel, outputPanel;
    JScrollPane mainScrollPane;

    String[] tamilNaduDistricts = {
            "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
            "Tirunelveli", "Thuthukudi", "Dharmapuri", "Dindigul", "Erode",
            "Kallakurichi", "Kancheepuram", "Kanniyakumari", "Karur", "Krishnagiri",
            "Madurai", "Mayiladuthurai", "Nagapattinam", "Namakkal", "Perambalur",
            "Pudukkottai", "Ramanathapuram", "Ranipet", "Salem", "Sivaganga",
            "Tenkasi", "Thanjavur"
    };

    String[] keralaDistricts = {
            "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod",
            "Kollam", "Kottayam", "Kozhikode", "Malappuram", "Palakkad",
            "Pathanamthitta", "Thiruvananthapuram", "Thrissur", "Wayanad"
    };

    String[] delhiDistricts = {
            "Central", "East", "New Delhi", "North", "North East",
            "North West", "Shahdara", "South", "South East", "South West", "West"
    };

    String[] franceCities = {"Paris", "Lyon", "Nice"};
    String[] germanyCities = {"Berlin", "Munich", "Hamburg"};
    String[] italyCities = {"Rome", "Milan", "Venice"};
    String[] spainCities = {"Madrid", "Barcelona", "Seville"};
    String[] switzerlandCities = {"Zurich", "Geneva"};
    String[] netherlandsCities = {"Amsterdam", "Rotterdam"};
    String[] austriaCities = {"Vienna", "Salzburg"};
    String[] czechCities = {"Prague", "Brno"};
    String[] portugalCities = {"Lisbon", "Porto"};
    String[] greeceCities = {"Athens", "Santorini"};

    public MainDashboard() {
        setTitle("SMART TRAVEL PLANNER");
        setSize(1380, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(1360, 1500));
        mainPanel.setBackground(new Color(252, 242, 248));

        mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(mainScrollPane, BorderLayout.CENTER);

        buildHeader();
        buildForm();
        buildTopButtons();
        buildOutputArea();

        setVisible(true);
    }

    private void buildHeader() {
        headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 1360, 110);
        headerPanel.setBackground(new Color(236, 72, 153));

        titleLabel = new JLabel("SMART TRAVEL PLANNER");
        titleLabel.setBounds(430, 10, 520, 40);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        subtitleLabel = new JLabel("Welcome! Select your source, destination and travel days to generate a smart itinerary.");
        subtitleLabel.setBounds(180, 58, 900, 25);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel);
    }

    private void buildForm() {
        formPanel = new JPanel(null);
        formPanel.setBounds(20, 130, 1300, 155);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

        homeLabel = new JLabel("HOME DASHBOARD");
        homeLabel.setBounds(500, 15, 320, 30);
        homeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        homeLabel.setForeground(new Color(219, 39, 119));
        formPanel.add(homeLabel);

        sourceStateLabel = new JLabel("Source State");
        sourceStateLabel.setBounds(25, 55, 120, 20);
        sourceStateLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(sourceStateLabel);

        sourceDistrictLabel = new JLabel("Source District");
        sourceDistrictLabel.setBounds(210, 55, 120, 20);
        sourceDistrictLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(sourceDistrictLabel);

        destCountryLabel = new JLabel("Destination Country");
        destCountryLabel.setBounds(395, 55, 140, 20);
        destCountryLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(destCountryLabel);

        destCityLabel = new JLabel("Destination City");
        destCityLabel.setBounds(610, 55, 120, 20);
        destCityLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(destCityLabel);

        daysLabel = new JLabel("No of Days");
        daysLabel.setBounds(825, 55, 100, 20);
        daysLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(daysLabel);

        sourceStateBox = new JComboBox<>(new String[]{"Tamil Nadu", "Kerala", "Delhi"});
        sourceStateBox.setBounds(25, 82, 165, 34);
        sourceStateBox.addActionListener(this);
        formPanel.add(sourceStateBox);

        sourceDistrictBox = new JComboBox<>(tamilNaduDistricts);
        sourceDistrictBox.setBounds(210, 82, 165, 34);
        formPanel.add(sourceDistrictBox);

        destCountryBox = new JComboBox<>(new String[]{
                "France", "Germany", "Italy", "Spain", "Switzerland",
                "Netherlands", "Austria", "Czech Republic", "Portugal", "Greece"
        });
        destCountryBox.setBounds(395, 82, 185, 34);
        destCountryBox.addActionListener(this);
        formPanel.add(destCountryBox);

        destCityBox = new JComboBox<>(franceCities);
        destCityBox.setBounds(610, 82, 185, 34);
        formPanel.add(destCityBox);

        daysField = new JTextField();
        daysField.setBounds(825, 82, 100, 34);
        formPanel.add(daysField);

        generateButton = new JButton("Generate Core Plan");
        generateButton.setBounds(950, 78, 180, 38);
        generateButton.setBackground(new Color(96, 165, 250));
        generateButton.setForeground(Color.BLACK);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(this);
        formPanel.add(generateButton);

        resetButton = new JButton("Reset");
        resetButton.setBounds(950, 120, 180, 28);
        resetButton.setBackground(new Color(100, 116, 139));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(this);
        formPanel.add(resetButton);

        mainPanel.add(formPanel);
    }

    private void buildTopButtons() {
        buttonPanel = new JPanel(new GridLayout(1, 5, 12, 10));
        buttonPanel.setBounds(20, 305, 1300, 55);
        buttonPanel.setBackground(new Color(252, 242, 248));

        placeStayButton = new JButton("1. Place to Stay");
        dayPlanButton = new JButton("2. Day Wise Plan");
        budgetButton = new JButton("3. Estimated Budget");
        pdfButton = new JButton("4. Save All as PDF");
        reviewButton = new JButton("5. Rating and Reviews");

        JButton[] buttons = {placeStayButton, dayPlanButton, budgetButton, pdfButton, reviewButton};

        for (JButton btn : buttons) {
            btn.setEnabled(false);
            btn.setFocusPainted(false);
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }

        placeStayButton.setBackground(new Color(236, 72, 153));
        dayPlanButton.setBackground(new Color(96, 165, 250));
        budgetButton.setBackground(new Color(236, 72, 153));
        pdfButton.setBackground(new Color(96, 165, 250));
        reviewButton.setBackground(new Color(236, 72, 153));
        

        mainPanel.add(buttonPanel);
    }

    private void buildOutputArea() {
        outputPanel = new JPanel(null);
        outputPanel.setBounds(20, 380, 1300, 980);
        outputPanel.setBackground(Color.WHITE);
        outputPanel.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

        JLabel outputTitle = new JLabel("Project Output");
        outputTitle.setBounds(20, 10, 220, 25);
        outputTitle.setFont(new Font("Arial", Font.BOLD, 18));
        outputTitle.setForeground(new Color(219, 39, 119));
        outputPanel.add(outputTitle);

        statusLabel = new JLabel("Ready");
        statusLabel.setBounds(1170, 10, 100, 25);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(37, 99, 235));
        outputPanel.add(statusLabel);

        JTextArea introArea = new JTextArea();
        introArea.setEditable(false);
        introArea.setFont(new Font("Arial", Font.PLAIN, 16));
        introArea.setLineWrap(true);
        introArea.setWrapStyleWord(true);
        introArea.setText(
                "WELCOME TO SMART TRAVEL PLANNER\n\n" +
                "Choose your source state and district from India, then choose the destination country and city.\n" +
                "Enter the number of travel days and click Generate Core Plan.\n\n" +
                "After that, use the five action buttons to explore:\n" +
                "1. Place to Stay\n" +
                "2. Day Wise Plan\n" +
                "3. Estimated Budget\n" +
                "4. Save All as PDF\n" +
                "5. Rating and Reviews"
        );

        JScrollPane introScroll = new JScrollPane(introArea);
        introScroll.setBounds(20, 50, 1240, 200);
        outputPanel.add(introScroll);

        mainPanel.add(outputPanel);
    }

    private void loadSourceDistricts(String state) {
        sourceDistrictBox.removeAllItems();

        String[] districts;
        if (state.equals("Tamil Nadu")) {
            districts = tamilNaduDistricts;
        } else if (state.equals("Kerala")) {
            districts = keralaDistricts;
        } else {
            districts = delhiDistricts;
        }

        for (String district : districts) {
            sourceDistrictBox.addItem(district);
        }
    }

    private void loadDestinationCities(String country) {
        destCityBox.removeAllItems();

        String[] cities;
        if (country.equals("France")) {
            cities = franceCities;
        } else if (country.equals("Germany")) {
            cities = germanyCities;
        } else if (country.equals("Italy")) {
            cities = italyCities;
        } else if (country.equals("Spain")) {
            cities = spainCities;
        } else if (country.equals("Switzerland")) {
            cities = switzerlandCities;
        } else if (country.equals("Netherlands")) {
            cities = netherlandsCities;
        } else if (country.equals("Austria")) {
            cities = austriaCities;
        } else if (country.equals("Czech Republic")) {
            cities = czechCities;
        } else if (country.equals("Portugal")) {
            cities = portugalCities;
        } else {
            cities = greeceCities;
        }

        for (String city : cities) {
            destCityBox.addItem(city);
        }
    }
    private void showDayPlanUI() {
        if (selectedPlaceToStay == null || selectedPlaceToStay.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select your place to stay first.");
            return;
        }

        String destCountry = (String) destCountryBox.getSelectedItem();
        String destCity = (String) destCityBox.getSelectedItem();
        String daysText = daysField.getText().trim();

        int noOfDays;
        try {
            noOfDays = Integer.parseInt(daysText);
            if (noOfDays <= 0) {
                JOptionPane.showMessageDialog(this, "Enter valid number of days.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid number of days.");
            return;
        }

        statusLabel.setText("Loading day plan...");

        SwingWorker<List<DayPlanService.DayPlan>, Void> worker = new SwingWorker<List<DayPlanService.DayPlan>, Void>() {
            @Override
            protected List<DayPlanService.DayPlan> doInBackground() throws Exception {
                return DayPlanService.buildDayPlans(
                        destCity,
                        destCountry,
                        noOfDays,
                        selectedPlaceToStay,
                        selectedHotelLat,
                        selectedHotelLon
                );
            }

            @Override
            protected void done() {
                try {
                    currentDayPlans = get();
                    statusLabel.setText("Day plan ready");

                    outputPanel.removeAll();
                    outputPanel.setLayout(null);

                    JLabel outputTitle = new JLabel("2. DAY WISE PLAN");
                    outputTitle.setBounds(20, 10, 280, 30);
                    outputTitle.setFont(new Font("Arial", Font.BOLD, 22));
                    outputTitle.setForeground(new Color(219, 39, 119));
                    outputPanel.add(outputTitle);

                    JLabel stayLabel = new JLabel("Stay: " + selectedPlaceToStay);
                    stayLabel.setBounds(900, 12, 340, 25);
                    stayLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    stayLabel.setForeground(new Color(37, 99, 235));
                    outputPanel.add(stayLabel);

                    if (currentDayPlans == null || currentDayPlans.isEmpty()) {
                        JLabel noData = new JLabel("No day plan available.");
                        noData.setBounds(20, 60, 300, 25);
                        noData.setFont(new Font("Arial", Font.BOLD, 18));
                        noData.setForeground(Color.RED);
                        outputPanel.add(noData);
                        outputPanel.revalidate();
                        outputPanel.repaint();
                        return;
                    }

                    int y = 55;

                    for (DayPlanService.DayPlan day : currentDayPlans) {
                        JPanel dayCard = new JPanel(null);
                        dayCard.setBounds(20, y, 1240, 240);
                        dayCard.setBackground(Color.WHITE);
                        dayCard.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

                        JLabel dayTitle = new JLabel("Day " + day.dayNumber);
                        dayTitle.setBounds(20, 10, 100, 25);
                        dayTitle.setFont(new Font("Arial", Font.BOLD, 20));
                        dayTitle.setForeground(new Color(219, 39, 119));
                        dayCard.add(dayTitle);

                        JLabel startLabel = new JLabel("Start: " + selectedPlaceToStay);
                        startLabel.setBounds(160, 12, 350, 22);
                        startLabel.setFont(new Font("Arial", Font.BOLD, 13));
                        dayCard.add(startLabel);

                        JLabel endLabel = new JLabel("End: " + selectedPlaceToStay);
                        endLabel.setBounds(860, 12, 320, 22);
                        endLabel.setFont(new Font("Arial", Font.BOLD, 13));
                        dayCard.add(endLabel);

                        JLabel h1 = new JLabel("Time");
                        h1.setBounds(20, 45, 120, 25);
                        h1.setFont(new Font("Arial", Font.BOLD, 14));
                        dayCard.add(h1);

                        JLabel h2 = new JLabel("Place");
                        h2.setBounds(150, 45, 260, 25);
                        h2.setFont(new Font("Arial", Font.BOLD, 14));
                        dayCard.add(h2);

                        JLabel h3 = new JLabel("Category");
                        h3.setBounds(420, 45, 120, 25);
                        h3.setFont(new Font("Arial", Font.BOLD, 14));
                        dayCard.add(h3);

                        JLabel h4 = new JLabel("Transport");
                        h4.setBounds(560, 45, 120, 25);
                        h4.setFont(new Font("Arial", Font.BOLD, 14));
                        dayCard.add(h4);

                        JLabel h5 = new JLabel("Route");
                        h5.setBounds(860, 45, 160, 25);
                        h5.setFont(new Font("Arial", Font.BOLD, 14));
                        dayCard.add(h5);

                        int rowY = 80;

                        for (DayPlanService.PlanStop stop : day.stops) {
                            JLabel timeLabel = new JLabel(stop.startTime + " - " + stop.endTime);
                            timeLabel.setBounds(20, rowY, 120, 25);
                            timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                            dayCard.add(timeLabel);

                            JLabel placeLabel = new JLabel("<html><body style='width:240px'>" + stop.placeName + "</body></html>");
                            placeLabel.setBounds(150, rowY - 4, 250, 35);
                            placeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                            dayCard.add(placeLabel);

                            JLabel catLabel = new JLabel(stop.category);
                            catLabel.setBounds(420, rowY, 120, 25);
                            catLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                            dayCard.add(catLabel);

                            JLabel transportLabel = new JLabel(stop.transportMode);
                            transportLabel.setBounds(560, rowY, 120, 25);
                            transportLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                            dayCard.add(transportLabel);

                            

                            JButton routeBtn = new JButton("Open Route");
                            routeBtn.setBounds(980, rowY - 2, 130, 28);
                            routeBtn.setBackground(new Color(96, 165, 250));
                            routeBtn.setForeground(Color.WHITE);
                            routeBtn.setFocusPainted(false);
                            routeBtn.addActionListener(ev -> TravelService.openInBrowser(stop.routeLink));
                            dayCard.add(routeBtn);

                            rowY += 35;
                        }

                        outputPanel.add(dayCard);
                        y += 260;
                    }

                    outputPanel.setBounds(20, 380, 1300, y + 20);
                    outputPanel.revalidate();
                    outputPanel.repaint();

                    mainPanel.setPreferredSize(new Dimension(1360, 380 + y + 100));
                    mainPanel.revalidate();
                    mainPanel.repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Error");
                    JOptionPane.showMessageDialog(MainDashboard.this, "Failed to load day wise plan.");
                }
            }
        };

        worker.execute();
    }

    private JPanel createInfoCard(String title, String value, int x, int y, int w, int h) {
        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, w, h);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));

        JLabel t = new JLabel(title);
        t.setBounds(20, 10, 220, 20);
        t.setFont(new Font("Arial", Font.BOLD, 14));
        t.setForeground(new Color(219, 39, 119));
        panel.add(t);

        JLabel v = new JLabel(value);
        v.setBounds(20, 40, 300, 30);
        v.setFont(new Font("Arial", Font.BOLD, 22));
        v.setForeground(new Color(30, 41, 59));
        panel.add(v);

        return panel;
    }

    private JLabel createImageLabel(String imageUrl, int x, int y, int w, int h, String fallbackText) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setBounds(x, y, w, h);
        label.setOpaque(true);
        label.setBackground(new Color(253, 242, 248));
        label.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new Exception("Empty image URL");
            }

            imageUrl = imageUrl.trim();
            if (imageUrl.startsWith("//")) {
                imageUrl = "https:" + imageUrl;
            }

            URL url = new URL(imageUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
            con.setRequestProperty("Referer", "https://www.google.com/");

            BufferedImage bufferedImage;
            try (var in = con.getInputStream()) {
                bufferedImage = ImageIO.read(in);
            }

            if (bufferedImage != null) {
                int imgWidth = bufferedImage.getWidth();
                int imgHeight = bufferedImage.getHeight();

                double scaleX = (double) w / imgWidth;
                double scaleY = (double) h / imgHeight;
                double scale = Math.min(scaleX, scaleY);

                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);

                Image scaled = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
                return label;
            }

        } catch (Exception e) {
            System.out.println("Image load failed: " + imageUrl + " -> " + e.getMessage());
        }

        label.setText("<html><center>" + fallbackText + "</center></html>");
        label.setForeground(new Color(100, 116, 139));
        label.setFont(new Font("Arial", Font.BOLD, 18));
        return label;
    }
    private void generateCorePlanPreview() {
        String sourceState = (String) sourceStateBox.getSelectedItem();
        String sourceDistrict = (String) sourceDistrictBox.getSelectedItem();
        String destCountry = (String) destCountryBox.getSelectedItem();
        String destCity = (String) destCityBox.getSelectedItem();
        String days = daysField.getText().trim();

        if (days.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter number of days.");
            return;
        }

        int noOfDays;
        try {
            noOfDays = Integer.parseInt(days);
            if (noOfDays <= 0) {
                JOptionPane.showMessageDialog(this, "Number of days must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter valid number of days.");
            return;
        }

        statusLabel.setText("Loading...");

        SwingWorker<CorePlanResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CorePlanResult doInBackground() throws Exception {
                return TravelService.buildCorePlan(sourceDistrict, sourceState, destCity, destCountry);
            }

            @Override
            protected void done() {
                try {
                    CorePlanResult result = get();
                    currentProofLink = result.osmProofLink;
                    statusLabel.setText("Ready");
                    showCorePlanUI(result, sourceDistrict, destCity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Error");
                    JOptionPane.showMessageDialog(MainDashboard.this, "Failed to generate live core plan.");
                }
            }
        };

        worker.execute();
    }

    private void showCorePlanUI(CorePlanResult result, String sourceLandmark, String destinationLandmark) {
        outputPanel.removeAll();
        outputPanel.setLayout(null);

        JLabel outputTitle = new JLabel("Project Output");
        outputTitle.setBounds(20, 10, 220, 25);
        outputTitle.setFont(new Font("Arial", Font.BOLD, 18));
        outputTitle.setForeground(new Color(219, 39, 119));
        outputPanel.add(outputTitle);

        statusLabel = new JLabel("Ready");
        statusLabel.setBounds(1170, 10, 100, 25);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(37, 99, 235));
        outputPanel.add(statusLabel);

        JPanel distCard = createInfoCard("Distance", result.distanceKm + " km", 20, 50, 380, 90);
        JPanel timeCard = createInfoCard("Travel Time", result.durationHr + " hr", 450, 50, 380, 90);
        JPanel modeCard = createInfoCard("Mode of Transport", result.transportMode, 880, 50, 380, 90);

        outputPanel.add(distCard);
        outputPanel.add(timeCard);
        outputPanel.add(modeCard);

        JButton proofButton = new JButton("Open Exact Distance Proof");
        proofButton.setBounds(20, 155, 240, 34);
        proofButton.setBackground(new Color(96, 165, 250));
        proofButton.setForeground(Color.WHITE);
        proofButton.setFocusPainted(false);
        proofButton.addActionListener(e -> TravelService.openInBrowser(result.osmProofLink));
        outputPanel.add(proofButton);

        JPanel routePanel = new JPanel(null);
        routePanel.setBounds(20, 210, 1240, 120);
        routePanel.setBackground(new Color(253, 242, 248));
        routePanel.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));

        JLabel srcLabel = new JLabel(sourceLandmark, SwingConstants.CENTER);
        srcLabel.setBounds(20, 10, 220, 25);
        srcLabel.setFont(new Font("Arial", Font.BOLD, 18));
        srcLabel.setForeground(new Color(219, 39, 119));
        routePanel.add(srcLabel);

        JLabel destLabel = new JLabel(destinationLandmark, SwingConstants.CENTER);
        destLabel.setBounds(1000, 10, 200, 25);
        destLabel.setFont(new Font("Arial", Font.BOLD, 18));
        destLabel.setForeground(new Color(37, 99, 235));
        routePanel.add(destLabel);

        JPanel sourceBox = new JPanel();
        sourceBox.setBounds(20, 55, 140, 45);
        sourceBox.setBackground(new Color(244, 114, 182));
        sourceBox.add(new JLabel("SOURCE"));
        routePanel.add(sourceBox);

        JPanel destBox = new JPanel();
        destBox.setBounds(1080, 55, 140, 45);
        destBox.setBackground(new Color(147, 197, 253));
        destBox.add(new JLabel("DESTINATION"));
        routePanel.add(destBox);

        RouteLinePanel routeLine = new RouteLinePanel();
        routeLine.setBounds(180, 58, 880, 25);
        routePanel.add(routeLine);

        planeLabel = new JLabel("✈");
        planeLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        planeLabel.setForeground(new Color(37, 99, 235));
        planeLabel.setBounds(240, 48, 40, 30);
        routePanel.add(planeLabel);

        if (planeTimer != null) {
            planeTimer.stop();
        }

        planeX = 240;
        planeTimer = new Timer(25, e -> {
            planeX += 4;
            if (planeX > 1010) {
                planeX = 240;
            }
            planeLabel.setBounds(planeX, 48, 40, 30);
        });
        planeTimer.start();

        outputPanel.add(routePanel);

        JPanel imgPanel = new JPanel(null);
        imgPanel.setBounds(20, 350, 1240, 260);
        imgPanel.setBackground(Color.WHITE);

        JLabel sourceText = new JLabel("Source Landmark: " + sourceLandmark);
        sourceText.setBounds(20, 10, 400, 25);
        sourceText.setFont(new Font("Arial", Font.BOLD, 16));
        sourceText.setForeground(new Color(219, 39, 119));
        imgPanel.add(sourceText);

        JLabel destText = new JLabel("Destination Landmark: " + destinationLandmark);
        destText.setBounds(700, 10, 420, 25);
        destText.setFont(new Font("Arial", Font.BOLD, 16));
        destText.setForeground(new Color(37, 99, 235));
        imgPanel.add(destText);

        JLabel srcImage = createImageLabel(result.sourceImageUrl, 20, 45, 520, 190, "No source image");
        JLabel destImage = createImageLabel(result.destinationImageUrl, 680, 45, 520, 190, "No destination image");

        imgPanel.add(srcImage);
        imgPanel.add(destImage);

        outputPanel.add(imgPanel);

        placeStayButton.setEnabled(true);
        dayPlanButton.setEnabled(true);
        budgetButton.setEnabled(true);
        pdfButton.setEnabled(true);
        reviewButton.setEnabled(true);

        mainPanel.setPreferredSize(new Dimension(1360, 1500));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void resetForm() {
        sourceStateBox.setSelectedIndex(0);
        loadSourceDistricts("Tamil Nadu");

        destCountryBox.setSelectedIndex(0);
        loadDestinationCities("France");

        daysField.setText("");
        statusLabel.setText("Ready");

        currentTripId = -1;
        selectedPlaceToStay = null;

        placeStayButton.setEnabled(false);
        dayPlanButton.setEnabled(false);
        budgetButton.setEnabled(false);
        pdfButton.setEnabled(false);
        reviewButton.setEnabled(false);

        outputPanel.removeAll();
        buildOutputArea();
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    private void showPlaceToStayUI() {
        String destCountry = (String) destCountryBox.getSelectedItem();
        String destCity = (String) destCityBox.getSelectedItem();

        statusLabel.setText("Loading hotels...");

        SwingWorker<List<Hotel>, Void> worker = new SwingWorker<List<Hotel>, Void>() {
            @Override
            protected List<Hotel> doInBackground() {
                try {
                    return HotelService.getHotels(destCity, destCountry);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    currentHotels = get();
                    statusLabel.setText("Hotels ready");

                    outputPanel.removeAll();
                    outputPanel.setLayout(null);

                    JLabel outputTitle = new JLabel("1. PLACE TO STAY");
                    outputTitle.setBounds(20, 10, 260, 30);
                    outputTitle.setFont(new Font("Arial", Font.BOLD, 22));
                    outputTitle.setForeground(new Color(219, 39, 119));
                    outputPanel.add(outputTitle);

                    JLabel status = new JLabel("Destination: " + destCity + ", " + destCountry);
                    status.setBounds(960, 12, 300, 25);
                    status.setFont(new Font("Arial", Font.BOLD, 14));
                    status.setForeground(new Color(37, 99, 235));
                    outputPanel.add(status);

                    JLabel info = new JLabel("Select Your Place to Stay");
                    info.setBounds(20, 50, 320, 25);
                    info.setFont(new Font("Arial", Font.BOLD, 18));
                    outputPanel.add(info);

                    if (currentHotels == null || currentHotels.isEmpty()) {
                        JLabel noData = new JLabel("No live hotels available right now. Please try again.");
                        noData.setBounds(20, 95, 520, 30);
                        noData.setFont(new Font("Arial", Font.BOLD, 18));
                        noData.setForeground(Color.RED);
                        outputPanel.add(noData);

                        outputPanel.setBounds(20, 380, 1300, 180);
                        outputPanel.revalidate();
                        outputPanel.repaint();

                        mainPanel.setPreferredSize(new Dimension(1360, 700));
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        return;
                    }

                    int x1 = 20;
                    int x2 = 660;
                    int y = 90;
                    int cardW = 600;
                    int cardH = 250;

                    for (int i = 0; i < currentHotels.size(); i++) {
                        Hotel hotel = currentHotels.get(i);

                        int x = (i % 2 == 0) ? x1 : x2;
                        if (i > 0 && i % 2 == 0) {
                            y += 270;
                        }

                        JPanel card = new JPanel(null);
                        card.setBounds(x, y, cardW, cardH);
                        card.setBackground(Color.WHITE);
                        card.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

                        JLabel nameLabel = new JLabel(hotel.name);
                        nameLabel.setBounds(15, 10, 560, 30);
                        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        nameLabel.setForeground(new Color(219, 39, 119));
                        card.add(nameLabel);

                        JLabel locationLabel = new JLabel("Location: " + hotel.lat + ", " + hotel.lon);
                        locationLabel.setBounds(15, 45, 320, 20);
                        locationLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                        card.add(locationLabel);

                        JLabel imageLabel = createImageLabel(hotel.imageUrl, 15, 75, 220, 140, "No hotel image");
                        card.add(imageLabel);

                        JButton selectBtn = new JButton("Select");
                        selectBtn.setBounds(270, 85, 120, 38);
                        selectBtn.setBackground(new Color(236, 72, 153));
                        selectBtn.setForeground(Color.WHITE);
                        selectBtn.setFocusPainted(false);
                        selectBtn.addActionListener(ev -> {
                            selectedPlaceToStay = hotel.name;
                            selectedHotelLat = hotel.lat;
                            selectedHotelLon = hotel.lon;
                            JOptionPane.showMessageDialog(MainDashboard.this, "Selected stay: " + hotel.name);
                        });
                        card.add(selectBtn);

                        JButton mapBtn = new JButton("Open in Map");
                        mapBtn.setBounds(410, 85, 150, 38);
                        mapBtn.setBackground(new Color(96, 165, 250));
                        mapBtn.setForeground(Color.WHITE);
                        mapBtn.setFocusPainted(false);
                        mapBtn.addActionListener(ev -> TravelService.openInBrowser(hotel.osmLink));
                        card.add(mapBtn);

                        JLabel note = new JLabel("<html><body style='width:290px'>This link opens the exact hotel location in OpenStreetMap, showing that the place really exists.</body></html>");
                        note.setBounds(270, 140, 290, 60);
                        note.setFont(new Font("Arial", Font.PLAIN, 12));
                        note.setForeground(new Color(80, 80, 80));
                        card.add(note);

                        outputPanel.add(card);
                    }

                    int cardRows = (currentHotels.size() + 1) / 2;
                    int requiredOutputHeight = 120 + (cardRows * 270) + 40;

                    outputPanel.setBounds(20, 380, 1300, requiredOutputHeight);
                    outputPanel.revalidate();
                    outputPanel.repaint();

                    mainPanel.setPreferredSize(new Dimension(1360, 380 + requiredOutputHeight + 80));
                    mainPanel.revalidate();
                    mainPanel.repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Error");
                    JOptionPane.showMessageDialog(MainDashboard.this, "Failed to load hotels.");
                }
            }
        };

        worker.execute();
    }
    private void showBudgetUI() {

        if (selectedPlaceToStay == null || selectedPlaceToStay.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select place to stay first.");
            return;
        }

        String daysText = daysField.getText().trim();

        int noOfDays;
        try {
            noOfDays = Integer.parseInt(daysText);
            if (noOfDays <= 0) {
                JOptionPane.showMessageDialog(this, "Enter valid number of days.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid number of days.");
            return;
        }

        BudgetService.BudgetResult budget =
                BudgetService.calculateBudget(
                        noOfDays,
                        currentDayPlans,
                        selectedPlaceToStay
                );

        outputPanel.removeAll();
        outputPanel.setLayout(null);

        JLabel title = new JLabel("3. ESTIMATED BUDGET");
        title.setBounds(20, 10, 320, 30);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(219, 39, 119));
        outputPanel.add(title);

        JLabel stayLabel = new JLabel("Stay: " + selectedPlaceToStay);
        stayLabel.setBounds(820, 12, 430, 25);
        stayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stayLabel.setForeground(new Color(37, 99, 235));
        outputPanel.add(stayLabel);

        JPanel summaryCard = new JPanel(null);
        summaryCard.setBounds(20, 55, 1240, 100);
        summaryCard.setBackground(new Color(253, 242, 248));
        summaryCard.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

        JLabel tripDays = new JLabel("Days: " + noOfDays);
        tripDays.setBounds(30, 18, 180, 25);
        tripDays.setFont(new Font("Arial", Font.BOLD, 18));
        summaryCard.add(tripDays);

        JLabel budgetType = new JLabel("Budget Type: " + budget.budgetType);
        budgetType.setBounds(250, 18, 250, 25);
        budgetType.setFont(new Font("Arial", Font.BOLD, 18));
        budgetType.setForeground(new Color(22, 163, 74));
        summaryCard.add(budgetType);

        JLabel euroRate = new JLabel("EUR to INR: ₹ " + String.format("%.2f", budget.euroToInrRate));
        euroRate.setBounds(560, 18, 260, 25);
        euroRate.setFont(new Font("Arial", Font.BOLD, 18));
        euroRate.setForeground(new Color(37, 99, 235));
        summaryCard.add(euroRate);

        JLabel note = new JLabel("This estimate includes stay, food, local transport, attraction tickets, and emergency buffer.");
        note.setBounds(30, 55, 900, 22);
        note.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryCard.add(note);

        outputPanel.add(summaryCard);

        int y = 180;

        outputPanel.add(createBudgetCard("Hotel Cost / Night (€)", String.format("%.2f", budget.hotelCostPerNight), 20, y, 380, 80));
        outputPanel.add(createBudgetCard("Total Hotel Cost (€)", String.format("%.2f", budget.totalHotelCost), 440, y, 380, 80));
        outputPanel.add(createBudgetCard("Breakfast Total (€)", String.format("%.2f", budget.breakfastCost), 860, y, 380, 80));
        y += 100;

        outputPanel.add(createBudgetCard("Lunch Total (€)", String.format("%.2f", budget.lunchCost), 20, y, 380, 80));
        outputPanel.add(createBudgetCard("Dinner Total (€)", String.format("%.2f", budget.dinnerCost), 440, y, 380, 80));
        outputPanel.add(createBudgetCard("Food Total (€)", String.format("%.2f", budget.totalFoodCost), 860, y, 380, 80));
        y += 100;

        outputPanel.add(createBudgetCard("Local Transport (€)", String.format("%.2f", budget.localTransportCost), 20, y, 380, 80));
        outputPanel.add(createBudgetCard("Attraction Tickets (€)", String.format("%.2f", budget.attractionCost), 440, y, 380, 80));
        outputPanel.add(createBudgetCard("Miscellaneous (€)", String.format("%.2f", budget.miscellaneousCost), 860, y, 380, 80));
        y += 100;

        outputPanel.add(createBudgetCard("Subtotal (€)", String.format("%.2f", budget.subtotalEuro), 20, y, 380, 80));
        outputPanel.add(createBudgetCard("Contingency 10% (€)", String.format("%.2f", budget.contingencyCost), 440, y, 380, 80));
        y += 110;

        JPanel totalPanel = new JPanel(null);
        totalPanel.setBounds(20, y, 1240, 120);
        totalPanel.setBackground(new Color(239, 246, 255));
        totalPanel.setBorder(new LineBorder(new Color(96, 165, 250), 2, true));

        JLabel totalEuro = new JLabel("TOTAL BUDGET: € " + String.format("%.2f", budget.totalEuro));
        totalEuro.setBounds(30, 20, 450, 35);
        totalEuro.setFont(new Font("Arial", Font.BOLD, 28));
        totalEuro.setForeground(new Color(37, 99, 235));
        totalPanel.add(totalEuro);

        JLabel totalInr = new JLabel("TOTAL BUDGET: ₹ " + String.format("%.2f", budget.totalINR));
        totalInr.setBounds(30, 65, 500, 35);
        totalInr.setFont(new Font("Arial", Font.BOLD, 28));
        totalInr.setForeground(new Color(22, 163, 74));
        totalPanel.add(totalInr);

        outputPanel.add(totalPanel);

        outputPanel.setBounds(20, 380, 1300, y + 150);
        outputPanel.revalidate();
        outputPanel.repaint();

        mainPanel.setPreferredSize(new Dimension(1360, 380 + y + 230));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    private JPanel createBudgetCard(String title, String value, int x, int y, int w, int h) {
        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, w, h);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

        JLabel t = new JLabel(title);
        t.setBounds(15, 12, 300, 20);
        t.setFont(new Font("Arial", Font.BOLD, 14));
        t.setForeground(new Color(219, 39, 119));
        panel.add(t);

        JLabel v = new JLabel(value);
        v.setBounds(15, 40, 250, 25);
        v.setFont(new Font("Arial", Font.BOLD, 22));
        v.setForeground(new Color(30, 41, 59));
        panel.add(v);

        return panel;
    }
    private void generatePDFReport() {
        if (selectedPlaceToStay == null || selectedPlaceToStay.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select place to stay first.");
            return;
        }

        if (currentDayPlans == null || currentDayPlans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate day wise plan first.");
            return;
        }

        String destCountry = (String) destCountryBox.getSelectedItem();
        String destCity = (String) destCityBox.getSelectedItem();
        String daysText = daysField.getText().trim();

        int noOfDays;
        try {
            noOfDays = Integer.parseInt(daysText);
            if (noOfDays <= 0) {
                JOptionPane.showMessageDialog(this, "Enter valid number of days.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid number of days.");
            return;
        }

        BudgetService.BudgetResult budget =
                BudgetService.calculateBudget(
                        noOfDays,
                        currentDayPlans,
                        selectedPlaceToStay
                );

        PdfService.generatePDF(
                destCity,
                destCountry,
                noOfDays,
                selectedPlaceToStay,
                currentDayPlans,
                budget
        );
    }
    
    private void updateStarDisplay() {
        for (int i = 0; i < 5; i++) {
            if (i < selectedRating) {
                starButtons[i].setText("\u2605");
            } else {
                starButtons[i].setText("\u2606");
            }
        }

        if (ratingValueLabel != null) {
            ratingValueLabel.setText("Selected Rating: " + selectedRating + " / 5");
        }
    }
    private void openReviewDialog() {
        JTextArea reviewInput = new JTextArea(8, 30);
        reviewInput.setLineWrap(true);
        reviewInput.setWrapStyleWord(true);
        reviewInput.setFont(new Font("Arial", Font.PLAIN, 14));

        if (submittedReview != null && !submittedReview.isEmpty()) {
            reviewInput.setText(submittedReview);
        }

        JScrollPane scrollPane = new JScrollPane(reviewInput);

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Enter Your Review",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            submittedReview = reviewInput.getText().trim();
            if (submittedReviewArea != null) {
                submittedReviewArea.setText(submittedReview);
            }
        }
    }
    private void showReviewUI() {
        if (selectedPlaceToStay == null || selectedPlaceToStay.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select place to stay first.");
            return;
        }

        outputPanel.removeAll();
        outputPanel.setLayout(null);

        JLabel title = new JLabel("5. RATING AND REVIEWS");
        title.setBounds(20, 10, 340, 30);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(219, 39, 119));
        outputPanel.add(title);

        JLabel stayLabel = new JLabel("Stay: " + selectedPlaceToStay);
        stayLabel.setBounds(920, 12, 320, 25);
        stayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stayLabel.setForeground(new Color(37, 99, 235));
        outputPanel.add(stayLabel);

        JPanel card = new JPanel(null);
        card.setBounds(20, 70, 1240, 420);
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(229, 231, 235), 2, true));

        JLabel hotelLabel = new JLabel(selectedPlaceToStay);
        hotelLabel.setBounds(25, 20, 500, 30);
        hotelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        hotelLabel.setForeground(new Color(219, 39, 119));
        card.add(hotelLabel);

        JLabel instructionLabel = new JLabel("Rate your stay:");
        instructionLabel.setBounds(25, 70, 180, 25);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        card.add(instructionLabel);

        int starX = 25;
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            starButtons[i] = new JButton("\u2606");
            starButtons[i].setBounds(starX, 110, 70, 60);
            starButtons[i].setFont(new Font("Segoe UI Symbol", Font.PLAIN, 38));
            starButtons[i].setFocusPainted(false);
            starButtons[i].setBorderPainted(false);
            starButtons[i].setContentAreaFilled(false);
            starButtons[i].setOpaque(false);
            starButtons[i].setForeground(new Color(234, 179, 8));
            starButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            starButtons[i].addActionListener(e -> {
                selectedRating = rating;
                updateStarDisplay();
            });
            card.add(starButtons[i]);
            starX += 75;
        }

        ratingValueLabel = new JLabel("Selected Rating: 0 / 5");
        ratingValueLabel.setBounds(25, 175, 220, 25);
        ratingValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ratingValueLabel.setForeground(new Color(22, 163, 74));
        card.add(ratingValueLabel);

        JButton reviewButton = new JButton("Write Review");
        reviewButton.setBounds(25, 220, 160, 38);
        reviewButton.setBackground(new Color(236, 72, 153));
        reviewButton.setForeground(Color.WHITE);
        reviewButton.setFocusPainted(false);
        reviewButton.addActionListener(e -> openReviewDialog());
        card.add(reviewButton);

        JButton submitButton = new JButton("Submit Review");
        submitButton.setBounds(205, 220, 170, 38);
        submitButton.setBackground(new Color(96, 165, 250));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> {
            if (selectedRating == 0) {
                JOptionPane.showMessageDialog(this, "Please select a star rating first.");
                return;
            }
            if (submittedReview == null || submittedReview.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please write your review.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Review submitted successfully!");
            submittedReviewArea.setText(submittedReview);
        });
        card.add(submitButton);

        JLabel yourReviewLabel = new JLabel("Your Review:");
        yourReviewLabel.setBounds(25, 285, 180, 25);
        yourReviewLabel.setFont(new Font("Arial", Font.BOLD, 18));
        card.add(yourReviewLabel);

        submittedReviewArea = new JTextArea();
        submittedReviewArea.setEditable(false);
        submittedReviewArea.setLineWrap(true);
        submittedReviewArea.setWrapStyleWord(true);
        submittedReviewArea.setFont(new Font("Arial", Font.PLAIN, 16));
        submittedReviewArea.setText(submittedReview == null ? "" : submittedReview);

        JScrollPane reviewScroll = new JScrollPane(submittedReviewArea);
        reviewScroll.setBounds(25, 320, 1180, 70);
        card.add(reviewScroll);

        outputPanel.add(card);

        outputPanel.setBounds(20, 380, 1300, 520);
        outputPanel.revalidate();
        outputPanel.repaint();

        mainPanel.setPreferredSize(new Dimension(1360, 980));
        mainPanel.revalidate();
        mainPanel.repaint();

        updateStarDisplay();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sourceStateBox) {
            String selectedState = (String) sourceStateBox.getSelectedItem();
            loadSourceDistricts(selectedState);
        } else if (e.getSource() == destCountryBox) {
            String selectedCountry = (String) destCountryBox.getSelectedItem();
            loadDestinationCities(selectedCountry);
        } else if (e.getSource() == generateButton) {
            generateCorePlanPreview();
        } else if (e.getSource() == resetButton) {
            resetForm();
        }  else if (e.getSource() == placeStayButton) {
            showPlaceToStayUI();
        }  else if (e.getSource() == dayPlanButton) {
            showDayPlanUI();
        } else if (e.getSource() == budgetButton) {
            showBudgetUI();
        
            
        } else if (e.getSource() == pdfButton) {
            generatePDFReport();
        } else if (e.getSource() == reviewButton) {
            showReviewUI();
        }
    }

    class RouteLinePanel extends JPanel {
        public RouteLinePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(100, 116, 139));

            int y = getHeight() / 2;
            for (int x = 0; x < getWidth(); x += 14) {
                g2.drawLine(x, y, x + 7, y);
            }
        }
    }

    public static void main(String[] args) {
        new MainDashboard();
    }
}