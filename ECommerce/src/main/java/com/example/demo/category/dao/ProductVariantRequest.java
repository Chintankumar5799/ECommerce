package com.example.demo.category.dao;

import com.fasterxml.jackson.databind.JsonNode;

public class ProductVariantRequest {
    private Long price;
    private Long offerPrice;
    private Float discount;
    private Long quantity;
    private JsonNode variantAttributes;

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Long offerPrice) {
        this.offerPrice = offerPrice;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public JsonNode getVariantAttributes() {
        return variantAttributes;
    }

    public void setVariantAttributes(JsonNode variantAttributes) {
        this.variantAttributes = variantAttributes;
    }
}
