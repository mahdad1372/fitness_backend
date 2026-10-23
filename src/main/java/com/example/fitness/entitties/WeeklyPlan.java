package com.example.fitness.entitties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@Table(name = "weekly_plans")
@Entity
public class WeeklyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer plan_id;

    @Column(nullable = false)
    private Integer student_id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, name = "week_start_date")
    private Date week_start_date;

    @CreationTimestamp
    @Column(updatable = false, name = "generated_at")
    private Date generated_at;

    @Column(nullable = false)
    private Integer weekly_budget_minutes;

    @Column(nullable = false)
    private Integer total_minutes_used;

    @Column(nullable = false)
    private Integer total_value;

    public Integer getPlan_id() { return plan_id; }
    public void setPlan_id(Integer plan_id) { this.plan_id = plan_id; }
    public Integer getStudent_id() { return student_id; }
    public void setStudent_id(Integer student_id) { this.student_id = student_id; }
    public Date getWeek_start_date() { return week_start_date; }
    public void setWeek_start_date(Date week_start_date) { this.week_start_date = week_start_date; }
    public Date getGenerated_at() { return generated_at; }
    public void setGenerated_at(Date generated_at) { this.generated_at = generated_at; }
    public Integer getWeekly_budget_minutes() { return weekly_budget_minutes; }
    public void setWeekly_budget_minutes(Integer weekly_budget_minutes) { this.weekly_budget_minutes = weekly_budget_minutes; }
    public Integer getTotal_minutes_used() { return total_minutes_used; }
    public void setTotal_minutes_used(Integer total_minutes_used) { this.total_minutes_used = total_minutes_used; }
    public Integer getTotal_value() { return total_value; }
    public void setTotal_value(Integer total_value) { this.total_value = total_value; }
}