package com.andreidodu.model;

import com.andreidodu.model.common.ModelCommon;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "mj_rating", uniqueConstraints =
@UniqueConstraint(columnNames = {"user_voter_id", "user_target_id", "job_instance_id"}))
@EntityListeners(AuditingEntityListener.class)
public class Rating extends ModelCommon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_voter_id", nullable = false)
    private User userVoter;


    @ManyToOne
    @JoinColumn(name = "user_target_id", nullable = false)
    private User userTarget;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "job_instance_id", nullable = false)
    private JobInstance jobInstance;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserVoter() {
        return userVoter;
    }

    public void setUserVoter(User userVoter) {
        this.userVoter = userVoter;
    }

    public User getUserTarget() {
        return userTarget;
    }

    public void setUserTarget(User userTarget) {
        this.userTarget = userTarget;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public JobInstance getJobInstance() {
        return jobInstance;
    }

    public void setJobInstance(JobInstance jobInstance) {
        this.jobInstance = jobInstance;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userVoter=" + userVoter.getId() +
                ", userTarget=" + userTarget.getId() +
                ", rating=" + rating +
                ", jobInstance=" + jobInstance.getId() +
                '}';
    }
}
