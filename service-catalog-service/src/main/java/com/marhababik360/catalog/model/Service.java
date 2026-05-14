package com.marhababik360.catalog.model;

import java.util.ArrayList;
import java.util.List;

public class Service {
    public String id;
    public String title;
    public String category;
    public String description;
    public double price;
    public List<String> images = new ArrayList<>();
    public String location;
    public String workerId;
    public String workerName;
    public String createdAt;
    public String updatedAt;
}
