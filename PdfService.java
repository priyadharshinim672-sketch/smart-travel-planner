package service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.util.List;

public class PdfService {

    public static void generatePDF(
            String city,
            String country,
            int days,
            String hotel,
            List<DayPlanService.DayPlan> plans,
            BudgetService.BudgetResult budget
    ) {
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            float y = 770;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("SMART TRAVEL PLAN");
            content.endText();

            y -= 40;

            y = writeLine(content, "Destination: " + city + ", " + country, y);
            y = writeLine(content, "Days: " + days, y);
            y = writeLine(content, "Stay: " + hotel, y);

            y -= 10;
            y = writeLineBold(content, "DAY WISE PLAN", y);

            for (DayPlanService.DayPlan day : plans) {
                y = writeLineBold(content, "Day " + day.dayNumber, y);

                for (DayPlanService.PlanStop stop : day.stops) {
                    String line = stop.startTime + " - " + stop.endTime + " | "
                            + stop.placeName + " | "
                            + stop.category + " | "
                            + stop.transportMode;

                    y = writeLine(content, line, y);

                    if (y < 80) {
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        content = new PDPageContentStream(doc, page);
                        y = 770;
                    }
                }

                y -= 8;
            }

            y -= 10;
            y = writeLineBold(content, "BUDGET SUMMARY", y);
            y = writeLine(content, "Hotel / Night (€): " + format(budget.hotelCostPerNight), y);
            y = writeLine(content, "Total Hotel (€): " + format(budget.totalHotelCost), y);
            y = writeLine(content, "Breakfast (€): " + format(budget.breakfastCost), y);
            y = writeLine(content, "Lunch (€): " + format(budget.lunchCost), y);
            y = writeLine(content, "Dinner (€): " + format(budget.dinnerCost), y);
            y = writeLine(content, "Food Total (€): " + format(budget.totalFoodCost), y);
            y = writeLine(content, "Transport (€): " + format(budget.localTransportCost), y);
            y = writeLine(content, "Attraction Tickets (€): " + format(budget.attractionCost), y);
            y = writeLine(content, "Miscellaneous (€): " + format(budget.miscellaneousCost), y);
            y = writeLine(content, "Subtotal (€): " + format(budget.subtotalEuro), y);
            y = writeLine(content, "Contingency (€): " + format(budget.contingencyCost), y);
            y = writeLineBold(content, "TOTAL (€): " + format(budget.totalEuro), y);
            y = writeLineBold(content, "TOTAL (INR): " + format(budget.totalINR), y);

            content.close();

            String filePath = System.getProperty("user.home") + "/TravelPlan_" + System.currentTimeMillis() + ".pdf";
            doc.save(filePath);
            doc.close();

            javax.swing.JOptionPane.showMessageDialog(null, "PDF saved successfully:\n" + filePath);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error generating PDF.");
        }
    }
    private static float writeLine(PDPageContentStream content, String text, float y) throws Exception {
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText(text);
        content.endText();
        return y - 18;
    }

    private static float writeLineBold(PDPageContentStream content, String text, float y) throws Exception {
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText(text);
        content.endText();
        return y - 20;
    }

    

    private static String format(double val) {
        return String.format("%.2f", val);
    }
}