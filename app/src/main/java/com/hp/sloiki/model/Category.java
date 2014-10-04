package com.hp.sloiki.model;

public class Category implements PersistedObject {

    private Integer categoryId;
    private Integer parentCategoryId;
    private String name;

    @Override
    public void setId(Integer id) {
        categoryId = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
