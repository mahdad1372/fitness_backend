package com.example.fitness.services;
import com.example.fitness.entitties.*;
import com.example.fitness.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final GeminiService geminiService;
    private final UserRepository userRepository;
    private final WorksoutsRepository workoutRepository;
    private final FoodsRepository foodRepository;
    private final DailyactivityRepository dailyActivityRepository;
    private final Health_metricsRepository healthMetricRepository;
    private final GoogleFitDataService googleFitDataService;
    @Cacheable(value = "recommendations", key = "#userId")
    public String getRecommendationsForUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();

        List<Workouts> workouts = workoutRepository
                .findByUserAndDate(userId, sevenDaysAgo);
        List<Foods> foods = foodRepository
                .findByUserAndDate(userId, sevenDaysAgo);
        List<Daily_activities> activities = dailyActivityRepository
                .findByUserAndDate(userId, sevenDaysAgo);
        List<Health_metrics> metrics = healthMetricRepository
                .findByUserAndDate(userId, sevenDaysAgo);

        // Heart rate / blood pressure now come live from Google Fit instead
        // of the Health_metrics table (same source as the cardiovascular
        // risk calculation). Fall back to 0 if the user hasn't connected a
        // data source yet, or Google Fit has no data for the window.
        double heartRate = 0;
        try {
            heartRate = googleFitDataService.getHeartRateAverage(user);
        } catch (Exception e) {
            System.out.println("Could not get heart rate from Google Fit for user " + userId + ": " + e.getMessage());
        }
        double bloodPressure = 0;
        try {
            bloodPressure = googleFitDataService.getBloodPressureAverage(user);
        } catch (Exception e) {
            System.out.println("Could not get blood pressure from Google Fit for user " + userId + ": " + e.getMessage());
        }

        String prompt = buildPrompt(user, workouts, foods, activities, metrics, heartRate, bloodPressure);
        return geminiService.getRecommendation(prompt);
    }
    @CacheEvict(value = "recommendations", key = "#userId")
    public void clearRecommendationCache(Integer userId) {
        System.out.println("Cache cleared for user: " + userId);
    }
    private String buildPrompt(User user,
                               List<Workouts> workouts,
                               List<Foods> foods,
                               List<Daily_activities> activities,
                               List<Health_metrics> metrics,
                               double heartRate,
                               double bloodPressure) {
        System.out.println("Your workouts is " + workouts);
        System.out.println("Your food is " + foods);
        System.out.println("Your Daily activity is " + activities);
        // Daily activities averages
        OptionalDouble avgSteps = activities.stream()
                .mapToInt(Daily_activities::getSteps).average();
        OptionalDouble avgSleep = activities.stream()
                .mapToDouble(a -> a.getSleep_hours()).average();
        OptionalDouble avgWater = activities.stream()
                .mapToDouble(a -> a.getWater_intake()).average();
        OptionalDouble avgCaloriesBurned = activities.stream()
                .mapToDouble(a -> a.getCalories_burned()).average();
        // Food averages
        OptionalDouble avgProtein = foods.stream()
                .mapToDouble(Foods::getProtein).average();
        OptionalDouble avgCarbs = foods.stream()
                .mapToDouble(Foods::getCarbohydrates).average();
        OptionalDouble avgFats = foods.stream()
                .mapToDouble(Foods::getFats).average();
        OptionalDouble avgCaloriesEaten = foods.stream()
                .mapToDouble(Foods::getCalories).average();
        System.out.println("Your Daily Protein is " + avgProtein);
        System.out.println("Your Daily carbohydrates is " + avgCarbs);
        System.out.println("Your Daily fats is " + avgFats);
        // Health metric averages
        OptionalDouble avgCholesterol = metrics.stream()
                .mapToDouble(Health_metrics::getCholesterol).average();

        String latestMood = activities.isEmpty() ? "unknown" :
                activities.get(activities.size() - 1).getMood();

        return String.format("""
            You are a professional fitness and health coach.
            Analyze this user's data from the last 7 days and give
            specific, actionable recommendations.

            USER PROFILE:
            - Age: %d, Gender: %s, Weight: %.1f kg, Height: %.1f cm
            - Smoker: %s

            ACTIVITY (7-day averages):
            - Daily steps: %.0f
            - Sleep hours: %.1f hrs/night
            - Water intake: %.1f L/day
            - Calories burned: %.0f kcal/day
            - Workout sessions this week: %d
            - Latest mood: %s

            NUTRITION (7-day averages):
            - Calories eaten: %.0f kcal
            - Protein: %.1f g
            - Carbohydrates: %.1f g
            - Fats: %.1f g

            HEALTH METRICS:
            - Heart rate (from Google Fit): %.0f bpm
            - Cholesterol (7-day average): %.1f
            - Blood pressure (from Google Fit): %.1f

            Please give personalized recommendations in these areas:
            1. Workout plan
            2. Nutrition & diet
            3. Sleep & recovery
            4. Hydration
            5. Health warnings (if any)

            Keep it concise, friendly, and practical.
            """,
                user.getAge(), user.getGender(), user.getWeight(), user.getHeight(),
                user.getSmoke() == 1 ? "Yes" : "No",
                avgSteps.orElse(0), avgSleep.orElse(0), avgWater.orElse(0),
                avgCaloriesBurned.orElse(0), workouts.size(), latestMood,
                avgCaloriesEaten.orElse(0), avgProtein.orElse(0),
                avgCarbs.orElse(0), avgFats.orElse(0),
                heartRate, avgCholesterol.orElse(0),
                bloodPressure
        );
    }
}