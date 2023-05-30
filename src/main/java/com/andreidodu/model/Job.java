package com.andreidodu.model;

import com.andreidodu.model.common.ModelCommon;
import com.andreidodu.model.message.Message;
import com.andreidodu.model.message.Participant;
import com.andreidodu.model.message.Room;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "mj_job")
@EntityListeners(AuditingEntityListener.class)
public class Job extends ModelCommon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "description", nullable = false, unique = true)
    private String description;

    @Column(name = "type", nullable = false)
    private Integer type;
    @Column(name = "picture", nullable = true)
    private String picture;

    @ManyToOne
    @JoinColumn(name = "user_publisher_id", nullable = false)
    private User publisher;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "price", nullable = false)
    private Double price;

    @OneToMany(mappedBy = "job")
    private Set<JobInstance> jobInstanceSet;

    @OneToMany(mappedBy = "job", cascade = CascadeType.REMOVE)
    private List<JobPicture> jobPictureList;
    @OneToMany(mappedBy = "job", cascade = CascadeType.REMOVE)
    private List<Participant> participants;
    @OneToMany(mappedBy = "job", cascade = CascadeType.REMOVE)
    private List<Room> rooms;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Set<JobInstance> getJobInstanceSet() {
        return jobInstanceSet;
    }

    public void setJobInstanceSet(Set<JobInstance> jobInstanceSet) {
        this.jobInstanceSet = jobInstanceSet;
    }

    public List<JobPicture> getJobPictureList() {
        return jobPictureList;
    }

    public void setJobPictureList(List<JobPicture> jobPictureList) {
        this.jobPictureList = jobPictureList;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public User getPublisher() {
        return publisher;
    }

    public void setPublisher(User publisher) {
        this.publisher = publisher;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
