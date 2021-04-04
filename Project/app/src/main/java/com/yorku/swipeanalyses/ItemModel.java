package com.yorku.swipeanalyses;

public class ItemModel {
    private int image;
    private String name;
    private int age;
    private String city;


    public ItemModel(int image, String name, int age, String city) {
        this.image = image;
        this.name = name;
        this.age = age;
        this.city = city;
    }

    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return String.format("%s days old", age);
    }

    public String getCity() {
        return city;
    }

}
