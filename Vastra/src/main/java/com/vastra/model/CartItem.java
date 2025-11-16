package com.vastra.model;

public class CartItem {
    private Product product;
    private int quantity;
    private int discountCents;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.discountCents = 0;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getDiscountCents() { return discountCents; }
    public void setDiscountCents(int discountCents) { this.discountCents = discountCents; }

    public void incrementQuantity() { quantity++; }
    public void decrementQuantity() { if (quantity > 1) quantity--; }

    public double getLineTotal() {
        return ((product.getSellPriceCents() * quantity) - discountCents) / 100.0;
    }

    public double getTaxAmount() {
        return product.calculateTaxAmount(quantity);
    }

    public double getDiscount() {
        return discountCents / 100.0;
    }

    public double getSubtotal() {
        return (product.getSellPriceCents() * quantity) / 100.0;
    }
}