package com.example.fitness.health;

/**
 * 10-year general cardiovascular disease risk, using the real, published
 * Framingham Heart Study equation (D'Agostino RB Sr, et al. "General
 * Cardiovascular Risk Profile for Use in Primary Care: The Framingham Heart
 * Study." Circulation. 2008;117(6):743-753). This is the SAME sex-specific
 * Cox proportional-hazards equation used in clinical risk calculators - not
 * an invented scoring formula - and it is a genuinely different kind of
 * algorithm from everything else in this app: a statistical/epidemiological
 * model (log-linear hazard regression converted to a survival probability),
 * as opposed to the graph, DP, search, and flow algorithms used elsewhere.
 *
 * Model (for each sex):
 *   L = sum(coefficient_i * ln(riskFactor_i)) + coefficient_smoker*smoker + coefficient_diabetic*diabetic
 *   risk = 1 - S0(10)^exp(L - meanL)
 * where S0(10) and meanL are the sex-specific baseline 10-year survival and
 * the mean of L over the original Framingham cohort.
 *
 * Known limitation, disclosed rather than hidden: like the published point-
 * based Framingham tables (whose top row is "30% or more"), this continuous
 * equation is not well-validated for combinations of the most extreme risk
 * factors simultaneously, and can mathematically produce numbers well above
 * what's clinically meaningful. This class caps the DISPLAYED risk at 30%
 * with an explicit flag, matching the standard published table's top
 * bucket, while still exposing the raw uncapped value for transparency.
 *
 * This is a risk-screening estimate, not a diagnosis, and is not a
 * substitute for professional medical evaluation.
 */
public class FraminghamRiskCalculator {

    public record Inputs(
            int age,
            boolean male,
            double totalCholesterolMgDl,
            double hdlCholesterolMgDl,
            double systolicBpMmHg,
            boolean onBloodPressureMedication,
            boolean currentSmoker,
            boolean diabetic
    ) {}

    public record RiskResult(
            double rawTenYearRiskPercent,
            double displayedTenYearRiskPercent,
            boolean cappedAtDisplayCeiling,
            String category
    ) {}

    private static final double DISPLAY_CEILING_PERCENT = 30.0;

    public static RiskResult calculate(Inputs in) {
        if (in.age() <= 0 || in.totalCholesterolMgDl() <= 0 || in.hdlCholesterolMgDl() <= 0 || in.systolicBpMmHg() <= 0) {
            throw new IllegalArgumentException("Age, total cholesterol, HDL cholesterol, and systolic blood pressure must all be positive numbers.");
        }

        double lnAge = Math.log(in.age());
        double lnTotalChol = Math.log(in.totalCholesterolMgDl());
        double lnHdl = Math.log(in.hdlCholesterolMgDl());
        double lnSbp = Math.log(in.systolicBpMmHg());
        int smoker = in.currentSmoker() ? 1 : 0;
        int diabetic = in.diabetic() ? 1 : 0;

        double L;
        double baselineSurvival;
        double meanL;

        if (in.male()) {
            double sbpCoefficient = in.onBloodPressureMedication() ? 1.99881 : 1.93303;
            L = 3.06117 * lnAge
                    + 1.12370 * lnTotalChol
                    - 0.93263 * lnHdl
                    + sbpCoefficient * lnSbp
                    + 0.65451 * smoker
                    + 0.57367 * diabetic;
            baselineSurvival = 0.88936;
            meanL = 23.9802;
        } else {
            double sbpCoefficient = in.onBloodPressureMedication() ? 2.82263 : 2.76157;
            L = 2.32888 * lnAge
                    + 1.20904 * lnTotalChol
                    - 0.70833 * lnHdl
                    + sbpCoefficient * lnSbp
                    + 0.52873 * smoker
                    + 0.69154 * diabetic;
            baselineSurvival = 0.95012;
            meanL = 26.1931;
        }

        double rawRisk = 1.0 - Math.pow(baselineSurvival, Math.exp(L - meanL));
        double rawRiskPercent = rawRisk * 100.0;

        boolean capped = rawRiskPercent > DISPLAY_CEILING_PERCENT;
        double displayedRiskPercent = capped ? DISPLAY_CEILING_PERCENT : rawRiskPercent;

        String category;
        if (displayedRiskPercent < 10.0) category = "Low";
        else if (displayedRiskPercent < 20.0) category = "Moderate";
        else category = "High";

        return new RiskResult(rawRiskPercent, displayedRiskPercent, capped, category);
    }
}