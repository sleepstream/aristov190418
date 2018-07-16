package com.sleepstream.checkkeeper.purchasesObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasesListData {

    public Map<String,Integer> fk =new HashMap<>();
    public Float quantity;
    public Float prise_for_item;
    public Integer id;
    public Float sum;
    public Invoice invoice;
    public Store store;
    public Product product;
    public Integer fk_purchases_accountinglist;
    public Integer fk_purchases_invoice;
    public Integer fk_purchases_stores;
    public Integer fk_purchases_products;
    private Integer order;
    public String google_id;
    public Long date_add;
    public String fk_purchases_products_google_id;
    public String fk_purchases_invoice_google_id;
    public String fk_purchases_stores_google_id;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }



    public static class Invoice
    {
        public Integer id;
        public String date;
        public double fullprice;
    }

    public static class Store
    {
        public Integer id;
        public String name;
        public String adress;
        public String google_id;
    }
    public static class Product
    {
        public Integer id;
        public String google_id;
        public String correctName;
        public String nameFromBill;
        public Integer barcode;
        public Category category;
    }
    public static class Category
    {
        public Integer id;
        public String category;
        public String icon_name;
        public Integer count;
        public Integer fk_product_category_data;
        public Integer fk_product_category_products;
        public Integer category_id;
        public boolean selected;
    }
}
