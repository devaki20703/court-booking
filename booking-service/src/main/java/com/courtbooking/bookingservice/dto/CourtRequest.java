package com.courtbooking.bookingservice.dto;

public class CourtRequest {
    private String name;
    private String sportType;
    private String location;
    private String description;

    public CourtRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}