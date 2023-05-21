package com.andreidodu.model;

import com.andreidodu.model.common.ModelCommon;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Table(name = "mj_job_instance")
@EntityListeners(AuditingEntityListener.class)
public class JobInstance extends ModelCommon {

    public JobInstance() {
        super();
    }

    public JobInstance(User userWorker, User userCustomer, Job job, Integer status) {
        this.userWorker = userWorker;
        this.userCustomer = userCustomer;
        this.job = job;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_worker_id", referencedColumnName = "id", nullable = false)
    private User userWorker;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_customer_id", referencedColumnName = "id", nullable = false)
    private User userCustomer;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "status", nullable = false)
    private Integer status;

    @OneToMany(mappedBy = "jobInstance", cascade = CascadeType.REMOVE)
    private List<Rating> ratingList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserWorker() {
        return userWorker;
    }

    public void setUserWorker(User userWorker) {
        this.userWorker = userWorker;
    }

    public User getUserCustomer() {
        return userCustomer;
    }

    public void setUserCustomer(User userCustomer) {
        this.userCustomer = userCustomer;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Rating> getRatingList() {
        return ratingList;
    }

    public void setRatingList(List<Rating> ratingList) {
        this.ratingList = ratingList;
    }

    @Override
    public String toString() {
        return "JobInstance{" +
                "id=" + id +
                ", userWorker=" + userWorker.getId() +
                ", userCustomer=" + userCustomer.getId() +
                ", job=" + job.getId() +
                ", status=" + status +
                ", rating=" + ratingList.toString() +
                '}';
    }
}
