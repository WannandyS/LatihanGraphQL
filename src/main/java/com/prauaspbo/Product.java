package com.prauaspbo;

public class Product {
    public Long id;
    public String name;
    public Double price;
    public String category;

    public Product(Long id, String name, Double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() { 
        return name; 
    }

    public Double getPrice() { 
        return price; 
    }

    public String getCategory() { 
        return category; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public void setPrice(Double price) { 
        this.price = price; 
    }

    public void setCategory(String category) { 
        this.category = category; 
    }
}
