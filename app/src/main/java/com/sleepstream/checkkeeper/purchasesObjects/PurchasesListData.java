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
    private Integer order;

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
    }
    public static class Product
    {
        public Integer id;
        public String correctName;
        public String nameFromBill;
        public Integer barcode;
        public List<Category> categories;
    }
    public static class Category
    {
        public Integer id;
        public String category;
        public String icon_name;
        public Integer count;
        public Integer fk_product_category_data;
        public Integer fk_product_category_products;
        public String category_id;
    }
}
