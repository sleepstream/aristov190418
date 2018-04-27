package com.sleepstream.checkkeeper.invoiceObjects;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InvoiceData implements Cloneable
{

    private String FP;
    private String FD;
    private String FN;
    private Long dateInvoice;
    private Float fullPrice;
    private Integer id;
    private Integer order;
    private Integer fk_invoice_accountinglist;
    private Integer fk_invoice_kktRegId;
    private Integer fk_invoice_stores;
    private Integer pinId;
    private Long date_add;
    private Integer in_basket;
    public boolean selected = false;
    public Double longitudeAdd;
    public Double latitudeAdd;
    public Integer repeatCount;

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

    public Store store;
    public KktRegId kktRegId;
    public Integer quantity;
    public Long date_day;


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

    private Integer _status;

    public Integer getFk_invoice_accountinglist() {
        return fk_invoice_accountinglist;
    }

    public void setFk_invoice_accountinglist(Integer fk_invoice_accountinglist) {
        this.fk_invoice_accountinglist = fk_invoice_accountinglist;
    }



    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }



    public String getFP() {
        return FP;
    }

    public void setFP(String FP) {
        this.FP = FP;
    }

    public String getFD() {
        return FD;
    }

    public void setFD(String FD) {
        this.FD = FD;
    }

    public String getFN() {
        return FN;
    }

    public void setFN(String FN) {
        this.FN = FN;
    }

    public String getDateInvoice(Integer key) {

        if(key == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            //android.icu.util.Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis(dateInvoice);


            return dateFormat.format(dateInvoice);
        }
        else
            if(dateInvoice == null)
                return "0";
            else
                return dateInvoice.toString();
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

    public void  setAll(String FP, String FD, String FN, Long dateInvoice, String fullPrice, Integer id, Integer order, Integer fk_invoice_accountinglist, Integer fk_invoice_kktRegId)
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
        this.order = order;
        this.fk_invoice_accountinglist = fk_invoice_accountinglist;
        this.fk_invoice_kktRegId = fk_invoice_kktRegId;

    }

    public Integer getfk_invoice_stores() {
        return fk_invoice_stores;
    }

    public void setfk_invoice_stores(Integer fk_invoice_stores) {
        this.fk_invoice_stores = fk_invoice_stores;
    }

    public static class Store
    {
        public Integer id;
        public String name;
        public String adress;
        public Double latitude;
        public Double longitude;
        public Long inn;
        public String name_from_fns;
        public String address_from_fns;
        public String place_id;
        public String store_type;
        public String iconName;
        public Integer _status;
        public boolean update;
        public String photoreference;
        public Integer date_add;
    }
    public static class KktRegId
    {
        public Integer id;
        public Long kktRegId;
        public Integer fk_kktRegId_stores;
        public Integer _status;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
