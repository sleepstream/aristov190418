package com.sleepstream.checkkeeper.invoiceObjects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceData implements Cloneable
{

    public String jsonData;
    public String google_id;
    public boolean fromServer;
    public String FP;
    public String FD;
    public String FN;
    public long google_hashcode;
    public Long fk_stores_links;
    public String fk_stores_links_google_id;
    public Long fk_invoice_kktRegId_store_links;
    public String fk_invoice_kktRegId_store_links_google_id;
    private Long dateInvoice;
    private Float fullPrice;
    public Float cashTotalSum;
    public Float ecashTotalSum;
    private Integer id;
    private Integer _order;
    private List<Integer> fk_invoice_accountinglist = new ArrayList<>();
    private Integer fk_invoice_kktRegId;
    private Integer fk_invoice_stores_from_fns;
    private Integer fk_invoice_stores_on_map;
    public String user_google_id;

    public String fk_invoice_accountinglist_google_id;
    public String fk_invoice_kktRegId_google_id;
    public String fk_invoice_stores_from_fns_google_id;
    public String fk_invoice_stores_on_map_google_id;

    private Integer pinId;
    private Long date_add;
    private Integer in_basket;
    public boolean selected = false;
    public Double longitudeAdd;
    public Double latitudeAdd;
    public Integer repeatCount;
    public boolean fromFNS = false;
    private Integer _status;
    public Integer server_status;
    public Store_from_fns store_from_fns;
    public Store_on_map store_on_map;
    public KktRegId kktRegId;
    public Integer quantity;
    public Long date_day;

    public Integer isIn_basket() {
        return in_basket;
    }

    public void setIn_basket(int in_basket) {
        this.in_basket = in_basket;
    }

    public Long getDate_add() {
        return date_add;
    }

    public void setDate_add(Long date_add) {
        this.date_add = date_add;
    }

    public Integer getPinId() {
        return pinId;
    }

    public void setPinId(Integer pinId) {
        this.pinId = pinId;
    }




    public void setfk_invoice_kktRegId(Integer fk_invoice_kktRegId) {
        this.fk_invoice_kktRegId = fk_invoice_kktRegId;
    }

    public Integer getfk_invoice_kktRegId() {
        return fk_invoice_kktRegId;
    }

    public Integer get_status() {
        return _status;
    }

    public void set_status(Integer _status) {
        this._status = _status;
    }



    public List<Integer> getFk_invoice_accountinglist() {
        return fk_invoice_accountinglist;
    }

    public void setFk_invoice_accountinglist(List<Integer> fk_invoice_accountinglist) {
        this.fk_invoice_accountinglist.addAll(fk_invoice_accountinglist);
    }
    public void setFk_invoice_accountinglist(int itemId) {
        this.fk_invoice_accountinglist.add(itemId);
    }


    public Integer get_order() {
        return _order;
    }

    public void set_order(Integer _order) {
        this._order = _order;
    }


    public String getDateInvoice(Integer key) {

        if(key == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            //android.icu.util.Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis(dateInvoice);


            return dateFormat.format(dateInvoice);
        }
        else if(key == 1) {
            if (dateInvoice == null)
                return "0";
            else
                return dateInvoice.toString();
        }
        else if(key == 2)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            //android.icu.util.Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis(dateInvoice);


            return dateFormat.format(dateInvoice);
        }
        return null;
    }

    public void setDateInvoice(Long dateInvoice) {
        this.dateInvoice = dateInvoice;
    }

    public Float getFullPrice() {
        if(fullPrice == null)
            return (float)0;
        else
        return fullPrice;
    }

    public void setFullPrice(float fullPrice) {
        this.fullPrice = fullPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void  setAll(String FP, String FD, String FN, Long dateInvoice, String fullPrice, Integer id, Integer order)
    {
        this.FP = FP;
        this.FD=FD;
        this.FN=FN;
        this.dateInvoice = dateInvoice;
        if (fullPrice != null && fullPrice.length()>1)
            this.fullPrice = Float.parseFloat(fullPrice);
        else
            this.fullPrice = null;
        this.id = id;
        this._order = order;

    }

    public Integer getfk_invoice_stores_from_fns() {
        return fk_invoice_stores_from_fns;
    }

    public void setfk_invoice_stores_from_fns(Integer fk_invoice_stores_from_fns) {
        this.fk_invoice_stores_from_fns = fk_invoice_stores_from_fns;
    }

    public Integer getFk_invoice_stores_on_map() {
        return fk_invoice_stores_on_map;
    }

    public void setFk_invoice_stores_on_map(Integer fk_invoice_stores_on_map) {
        this.fk_invoice_stores_on_map = fk_invoice_stores_on_map;
    }



    public static class Store_from_fns
    {
        public Integer id;
        public String google_id;
        public Long inn;
        public String name_from_fns;
        public String address_from_fns;
        public Integer _status;
        public boolean update;
        public Long date_add;

        public String hashCode;

    }
    public static class Store_on_map
    {
        public Integer id;
        public String google_id;
        public Integer fk_stores_from_fns;
        public String fk_stores_from_fns_google_id;
        public String name;
        public String address;
        public Double latitude;
        public Double longitude;
        public String place_id;
        public String store_type;
        public String iconName;
        public Integer _status;
        public boolean update;
        public String photo_reference;
        public Long date_add;
        public Long distance;
    }
    public static class KktRegId
    {
        public Integer id;
        public String google_id;
        public Long kktRegId;
        public String fk_kktRegId_google_id;
        public Integer _status;
        public Long date_add;
        public Long fk_kktRegId_stores_links;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
