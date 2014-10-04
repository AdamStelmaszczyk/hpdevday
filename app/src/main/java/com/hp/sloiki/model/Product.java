package com.hp.sloiki.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product implements PersistedObject {

    private Integer productId;
    private String name;
    private Long energyValue;
    private Category category;

    @Override
    public void setId(Integer key) {
        productId = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Long getEnergyValue() {
        return energyValue;
    }

    public void setEnergyValue(Long energyValue) {
        this.energyValue = energyValue;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

}
