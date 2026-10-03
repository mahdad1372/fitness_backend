package com.example.fitness.dto;

/**
 * Inputs the Framingham calculation needs that aren't already sitting on the
 * User/Health_metrics records: HDL cholesterol isn't collected anywhere else
 * in the app yet, and diabetes/blood-pressure-medication status aren't
 * either. The user supplies these once from the risk assessment form; this
 * service persists them (see CardiovascularRiskService) so they don't have
 * to be re-entered on every future assessment.
 */
public class CardiovascularAssessmentRequestDto {
    private Float hdlCholesterolMgDl;
    private Boolean onBloodPressureMedication;
    private Boolean diabetic;

    public Float getHdlCholesterolMgDl() {
        return hdlCholesterolMgDl;
    }

    public void setHdlCholesterolMgDl(Float hdlCholesterolMgDl) {
        this.hdlCholesterolMgDl = hdlCholesterolMgDl;
    }

    public Boolean getOnBloodPressureMedication() {
        return onBloodPressureMedication;
    }

    public void setOnBloodPressureMedication(Boolean onBloodPressureMedication) {
        this.onBloodPressureMedication = onBloodPressureMedication;
    }

    public Boolean getDiabetic() {
        return diabetic;
    }

    public void setDiabetic(Boolean diabetic) {
        this.diabetic = diabetic;
    }
}