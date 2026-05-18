package service;

import java.util.List;

public class BudgetService {

    public static class BudgetResult {
        public double hotelCostPerNight;
        public double totalHotelCost;

        public double breakfastCost;
        public double lunchCost;
        public double dinnerCost;
        public double totalFoodCost;

        public double localTransportCost;
        public double attractionCost;
        public double miscellaneousCost;

        public double subtotalEuro;
        public double contingencyCost;
        public double totalEuro;
        public double totalINR;

        public String budgetType;
        public double euroToInrRate;
    }

    public static BudgetResult calculateBudget(
            int days,
            List<DayPlanService.DayPlan> plans,
            String hotelName
    ) {
        BudgetResult result = new BudgetResult();

        int nights = Math.max(1, days - 1);
        int visitCount = countActualVisits(plans);
        int movementCount = countMovements(plans);

        result.hotelCostPerNight = estimateHotelCostPerNight(hotelName);
        result.totalHotelCost = result.hotelCostPerNight * nights;

        result.breakfastCost = days * 8.0;
        result.lunchCost = days * 15.0;
        result.dinnerCost = days * 18.0;
        result.totalFoodCost = result.breakfastCost + result.lunchCost + result.dinnerCost;

        result.localTransportCost = estimateTransportCost(movementCount);

        result.attractionCost = estimateAttractionCost(visitCount);

        result.miscellaneousCost = days * 10.0;

        result.subtotalEuro = result.totalHotelCost
                + result.totalFoodCost
                + result.localTransportCost
                + result.attractionCost
                + result.miscellaneousCost;

        result.contingencyCost = result.subtotalEuro * 0.10;
        result.totalEuro = result.subtotalEuro + result.contingencyCost;

        result.euroToInrRate = 90.0;
        result.totalINR = result.totalEuro * result.euroToInrRate;

        result.budgetType = classifyBudget(result.totalEuro, days);

        return result;
    }

    private static int countActualVisits(List<DayPlanService.DayPlan> plans) {
        int count = 0;
        if (plans == null) return 0;

        for (DayPlanService.DayPlan day : plans) {
            for (DayPlanService.PlanStop stop : day.stops) {
                if (!"Return to Stay".equalsIgnoreCase(stop.category)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countMovements(List<DayPlanService.DayPlan> plans) {
        int count = 0;
        if (plans == null) return 0;

        for (DayPlanService.DayPlan day : plans) {
            count += day.stops.size();
        }
        return count;
    }

    private static double estimateHotelCostPerNight(String hotelName) {
        if (hotelName == null || hotelName.trim().isEmpty()) return 85.0;

        String name = hotelName.toLowerCase();

        if (name.contains("ritz") || name.contains("palace") || name.contains("luxury") || name.contains("grand hotel")) {
            return 220.0;
        } else if (name.contains("hilton") || name.contains("marriott") || name.contains("radisson")
                || name.contains("novotel") || name.contains("plaza") || name.contains("regina")) {
            return 160.0;
        } else if (name.contains("inn") || name.contains("budget") || name.contains("guest house")
                || name.contains("guesthouse") || name.contains("hostel")) {
            return 70.0;
        } else {
            return 110.0;
        }
    }

    private static double estimateTransportCost(int movementCount) {
        return movementCount * 6.0;
    }

    private static double estimateAttractionCost(int visitCount) {
        return visitCount * 14.0;
    }

    private static String classifyBudget(double totalEuro, int days) {
        double perDay = totalEuro / Math.max(days, 1);

        if (perDay < 120) return "Budget";
        if (perDay < 220) return "Standard";
        return "Premium";
    }
}