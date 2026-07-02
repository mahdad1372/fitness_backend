package com.example.fitness.entitties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Table(name = "goals")
@Entity
public class Goals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer goal_id;
    @Column(nullable = false)
    private Integer user_id;
    @Column(nullable = false)
    private String goal_type ;
    @Column(nullable = true)
    private Float target_value;
    @Column(nullable = true)
    private Float current_value;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date start_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date end_date;


    @Column(nullable = true)
    private String status;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @JsonIgnore
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "before_goal_image",nullable = true)
    private byte[] beforeGoalImage;
    @JsonIgnore
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "after_goal_image",nullable = true)
    private byte[] afterGoalImage;
    @Column(nullable = true)
    private Integer display_in_main_page;
    @Column(name = "description")
    private String description;
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public String getGoal_type() {
        return goal_type;
    }
    public void setGoal_type(String goal_type) {
        this.goal_type = goal_type;
    }
    public Float getTarget_value() {
        return target_value;
    }
    public void setTarget_value(Float target_value) {
        this.target_value = target_value;
    }
    public Float getCurrent_value() {
        return current_value;
    }
    public void setCurrent_value(Float current_value) {
        this.current_value = current_value;
    }
    public Date getStart_date() {
        return start_date;
    }
    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }
    public Date getEnd_date() {
        return end_date;
    }
    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Integer getGoal_id() {
        return goal_id;
    }
    public void setGoal_id(Integer goal_id) {
        this.goal_id = goal_id;
    }
    public byte[] getBeforeGoalImage() {
        return beforeGoalImage;
    }

    public void setBeforeGoalImage(byte[] beforeGoalImage) {
        this.beforeGoalImage = beforeGoalImage;
    }

    public byte[] getAfterGoalImage() {
        return afterGoalImage;
    }

    public void setAfterGoalImage(byte[] afterGoalImage) {
        this.afterGoalImage = afterGoalImage;
    }

    public Integer getDisplay_in_main_page() {
        return display_in_main_page;
    }

    public void setDisplay_in_main_page(Integer display_in_main_page) {
        this.display_in_main_page = display_in_main_page;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
