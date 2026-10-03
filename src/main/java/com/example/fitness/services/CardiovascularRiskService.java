package com.example.fitness.services;

import com.example.fitness.dto.CardiovascularAssessmentRequestDto;
import com.example.fitness.dto.CardiovascularAssessmentResponseDto;
import com.example.fitness.entitties.Health_metrics;
import com.example.fitness.entitties.User;
import com.example.fitness.health.FraminghamRiskCalculator;
import com.example.fitness.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Assembles the inputs FraminghamRiskCalculator needs from three different
 * places - the user's profile (age, gender, smoking status), their most
 * recent Health_metrics record (total cholesterol), and a live Google Fit
 * read (systolic blood pressure) - plus the handful of extra risk factors
 * (HDL cholesterol, diabetes status, blood-pressure-medication status) that
 * aren't captured anywhere else yet and so come from the request body.
 * Those extra factors are persisted back onto the user's records so they
 * don't have to be re-entered on the next assessment.
 */
@Service
public class CardiovascularRiskService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final Health_metricsService health_metricsService;
    private final GoogleFitDataService googleFitDataService;

    public CardiovascularRiskService(UserService userService, UserRepository userRepository,
                                     Health_metricsService health_metricsService,
                                     GoogleFitDataService googleFitDataService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.health_metricsService = health_metricsService;
        this.googleFitDataService = googleFitDataService;
    }

    public CardiovascularAssessmentResponseDto assess(Integer userId, CardiovascularAssessmentRequestDto request) {
        List<User> users = userService.finduserbyid(userId);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No user found with id " + userId);
        }
        User user = users.get(0);

        List<Health_metrics> metrics = health_metricsService.finduserbyid(userId);
        if (metrics.isEmpty()) {
            throw new IllegalStateException("This user has no health metrics on record yet - add a cholesterol reading first.");
        }
        // Same "most recent record" assumption the old endpoint made
        // (get(0)); not something this feature is changing.
        Health_metrics latestMetrics = metrics.get(0);

        if (request.getHdlCholesterolMgDl() == null || request.getOnBloodPressureMedication() == null
                || request.getDiabetic() == null) {
            throw new IllegalArgumentException("HDL cholesterol, blood-pressure-medication status, and diabetes status are all required.");
        }

        float systolicBp;
        try {
            systolicBp = (float) googleFitDataService.getBloodPressureAverage(user);
        } catch (Exception e) {
            throw new IllegalStateException("Could not get blood pressure from Google Fit: " + e.getMessage(), e);
        }

        // Persist the inputs that don't already live on a record, so future
        // assessments (and the rest of the app) have them on hand.
        health_metricsService.updateHdlCholesterol(latestMetrics.getId(), request.getHdlCholesterolMgDl());
        userRepository.updateCardiovascularRiskFactors(
                userId,
                request.getDiabetic() ? 1 : 0,
                request.getOnBloodPressureMedication() ? 1 : 0
        );

        boolean isMale = user.getGender() != null && user.getGender().equalsIgnoreCase("male");
        boolean isSmoker = user.getSmoke() != null && user.getSmoke() == 1;

        FraminghamRiskCalculator.RiskResult result = FraminghamRiskCalculator.calculate(new FraminghamRiskCalculator.Inputs(
                user.getAge(),
                isMale,
                latestMetrics.getCholesterol(),
                request.getHdlCholesterolMgDl(),
                systolicBp,
                request.getOnBloodPressureMedication(),
                isSmoker,
                request.getDiabetic()
        ));

        return new CardiovascularAssessmentResponseDto(
                result.rawTenYearRiskPercent(),
                result.displayedTenYearRiskPercent(),
                result.cappedAtDisplayCeiling(),
                result.category(),
                user.getAge(),
                user.getGender(),
                latestMetrics.getCholesterol(),
                request.getHdlCholesterolMgDl(),
                systolicBp,
                request.getOnBloodPressureMedication(),
                isSmoker,
                request.getDiabetic()
        );
    }

    /**
     * Read-only variant for OTHER features (currently: WeeklyPlannerService)
     * that want to factor a user's current cardiovascular risk into a
     * decision without running a brand-new assessment or asking them
     * anything. Unlike assess(), this never persists and never throws for
     * "missing data" - it just returns empty, since a caller like the
     * Weekly Planner should degrade gracefully (no risk-based adjustment)
     * rather than fail outright for a user who's never run an assessment
     * or whose Google Fit blood pressure isn't available right now.
     *
     * Uses only what's already on record: the HDL reading and
     * diabetic/BP-medication flags an earlier assess() call persisted, plus
     * the same live Google Fit blood pressure read assess() uses. A user
     * who's never run an assessment has no HDL on file, so this returns
     * empty for them rather than guessing.
     */
    public Optional<FraminghamRiskCalculator.RiskResult> peekCurrentRisk(Integer userId) {
        List<User> users = userService.finduserbyid(userId);
        if (users.isEmpty()) return Optional.empty();
        User user = users.get(0);

        List<Health_metrics> metrics = health_metricsService.finduserbyid(userId);
        if (metrics.isEmpty()) return Optional.empty();
        Health_metrics latestMetrics = metrics.get(0);
        if (latestMetrics.getHdl_cholesterol() == null || latestMetrics.getCholesterol() == null) {
            return Optional.empty(); // never run a full assessment
        }

        float systolicBp;
        try {
            systolicBp = (float) googleFitDataService.getBloodPressureAverage(user);
        } catch (Exception e) {
            return Optional.empty(); // can't compute right now - not an error, just skip
        }

        boolean isMale = user.getGender() != null && user.getGender().equalsIgnoreCase("male");
        boolean isSmoker = user.getSmoke() != null && user.getSmoke() == 1;
        boolean onBpMeds = user.getOnBpMedication() != null && user.getOnBpMedication() == 1;
        boolean diabetic = user.getIsDiabetic() != null && user.getIsDiabetic() == 1;

        try {
            return Optional.of(FraminghamRiskCalculator.calculate(new FraminghamRiskCalculator.Inputs(
                    user.getAge(), isMale, latestMetrics.getCholesterol(), latestMetrics.getHdl_cholesterol(),
                    systolicBp, onBpMeds, isSmoker, diabetic)));
        } catch (IllegalArgumentException e) {
            return Optional.empty(); // malformed stored data (e.g. age <= 0) - skip rather than fail the caller
        }
    }

    // Conservative, documented simplification - not clinical guidance - for
    // how long a single coaching session should run, based on this user's
    // current Framingham risk category. Only needed if you've also wired
    // the Trainer & Equipment Scheduler risk-capping (remove this block and
    // the two methods below if you haven't).
    private static final int HIGH_RISK_SESSION_CEILING_MINUTES = 30;
    private static final int MODERATE_RISK_SESSION_CEILING_MINUTES = 45;
    private static final int DEFAULT_SESSION_CEILING_MINUTES = 60;

    public Optional<Integer> peekSafeSessionCeilingMinutes(Integer userId) {
        Optional<FraminghamRiskCalculator.RiskResult> risk = peekCurrentRisk(userId);
        if (risk.isEmpty()) return Optional.empty();
        return Optional.of(switch (risk.get().category()) {
            case "High" -> HIGH_RISK_SESSION_CEILING_MINUTES;
            case "Moderate" -> MODERATE_RISK_SESSION_CEILING_MINUTES;
            default -> DEFAULT_SESSION_CEILING_MINUTES;
        });
    }
}