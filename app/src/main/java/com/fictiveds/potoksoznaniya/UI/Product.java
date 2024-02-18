package com.fictiveds.potoksoznaniya.UI;

import com.google.firebase.database.Exclude;

public class Product {
   private String key;
    private String name;
    private String description;
    private double price;

    // Конструктор по умолчанию необходим для Firebase
    public Product() {
    }

    // Конструктор с параметрами для инициализации объекта Product
    public Product(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Геттеры и сеттеры

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


}