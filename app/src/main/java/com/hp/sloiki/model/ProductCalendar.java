package com.hp.sloiki.model;


public class ProductCalendar extends FridgeElement {
    public ProductCalendar() {

    }

	public ProductCalendar(FridgeElement product, Long volume) {
        this.setProduct(new Product());
	    this.getProduct().setProductId(product.getProduct().getProductId());
	    this.getProduct().setName(product.getProduct().getName());
	    this.setExpiryDate(product.getExpiryDate());
	    this.getProduct().setEnergyValue(product.getProduct().getEnergyValue());
	    this.getProduct().setCategory(product.getProduct().getCategory());
	    
	    this.setVolume(volume);				// overwrite the value of volume
	}
	
	private String consumptionDate;
	
	private String weekday;

	public String getConsumptionDate() {
		return consumptionDate;
	}

	public void setConsumptionDate(String consumptionDate) {
		this.consumptionDate = consumptionDate;
	}

	public String getWeekday() {
		return weekday;
	}

	public void setWeekday(String weekday) {
		this.weekday = weekday;
	}

}
