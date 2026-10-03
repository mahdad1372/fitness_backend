package com.example.fitness.dto;

/**
 * Result of a Framingham 10-year cardiovascular risk assessment
 * (see com.example.fitness.health.FraminghamRiskCalculator). Echoes back the
 * inputs actually used (some came live from Google Fit / stored records, not
 * from the request body) so the frontend can show the person exactly what
 * was plugged into the equation, alongside a standing disclaimer that this
 * is a risk-screening estimate, not a diagnosis.
 */
public class CardiovascularAssessmentResponseDto {
    private double rawTenYearRiskPercent;
    private double displayedTenYearRiskPercent;
    private boolean cappedAtDisplayCeiling;
    private String category;

    // Echoed inputs
    private Integer age;
    private String gender;
    private Float totalCholesterolMgDl;
    private Float hdlCholesterolMgDl;
    private Float systolicBpMmHg;
    private Boolean onBloodPressureMedication;
    private Boolean currentSmoker;
    private Boolean diabetic;

    private final String disclaimer =
            "This is a risk-screening estimate based on the Framingham Heart Study equation, " +
                    "not a diagnosis. It is not a substitute for professional medical evaluation - " +
                    "please discuss these results with a doctor.";

    public CardiovascularAssessmentResponseDto(double rawTenYearRiskPercent, double displayedTenYearRiskPercent,
                                               boolean cappedAtDisplayCeiling, String category,
                                               Integer age, String gender, Float totalCholesterolMgDl,
                                               Float hdlCholesterolMgDl, Float systolicBpMmHg,
                                               Boolean onBloodPressureMedication, Boolean currentSmoker,
                                               Boolean diabetic) {
        this.rawTenYearRiskPercent = rawTenYearRiskPercent;
        this.displayedTenYearRiskPercent = displayedTenYearRiskPercent;
        this.cappedAtDisplayCeiling = cappedAtDisplayCeiling;
        this.category = category;
        this.age = age;
        this.gender = gender;
        this.totalCholesterolMgDl = totalCholesterolMgDl;
        this.hdlCholesterolMgDl = hdlCholesterolMgDl;
        this.systolicBpMmHg = systolicBpMmHg;
        this.onBloodPressureMedication = onBloodPressureMedication;
        this.currentSmoker = currentSmoker;
        this.diabetic = diabetic;
    }

    public double getRawTenYearRiskPercent() {
        return rawTenYearRiskPercent;
    }

    public double getDisplayedTenYearRiskPercent() {
        return displayedTenYearRiskPercent;
    }

    public boolean isCappedAtDisplayCeiling() {
        return cappedAtDisplayCeiling;
    }

    public String getCategory() {
        return category;
    }

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public Float getTotalCholesterolMgDl() {
        return totalCholesterolMgDl;
    }

    public Float getHdlCholesterolMgDl() {
        return hdlCholesterolMgDl;
    }

    public Float getSystolicBpMmHg() {
        return systolicBpMmHg;
    }

    public Boolean getOnBloodPressureMedication() {
        return onBloodPressureMedication;
    }

    public Boolean getCurrentSmoker() {
        return currentSmoker;
    }

    public Boolean getDiabetic() {
        return diabetic;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}