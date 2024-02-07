package com.fictiveds.potoksoznaniya.UI;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    // Конструктор по умолчанию необходим для Firebase
    public Product() {
    }

    // Конструктор с параметрами для инициализации объекта Product
    public Product(String id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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