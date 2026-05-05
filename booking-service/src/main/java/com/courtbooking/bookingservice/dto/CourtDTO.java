package com.courtbooking.bookingservice.dto;

public class CourtDTO {
    private Long id;
    private String name;
    private String sportType;
    private String location;
    private Boolean available;
    private String description;

    public CourtDTO() {}

    public CourtDTO(Long id, String name, String sportType, String location, Boolean available, String description) {
        this.id = id;
        this.name = name;
        this.sportType = sportType;
        this.location = location;
        this.available = available;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}