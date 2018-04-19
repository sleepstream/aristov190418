package com.sleepstream.checkkeeper.linkedListObjects;

import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;

public class LinkedListData {
    private Integer id;
    private String fk_name;
    private Integer _order;
    private Integer fk_id;
    public InvoiceData invoiceData;
    public AccountingListData accountingListData;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFk_name() {
        return fk_name;
    }

    public void setFk_name(String fk_name) {
        this.fk_name = fk_name;
    }

    public Integer get_order() {
        return _order;
    }

    public void set_order(Integer _order) {
        this._order = _order;
    }

    public Integer getFk_id() {
        return fk_id;
    }

    public void setFk_id(Integer fk_id) {
        this.fk_id = fk_id;
    }
}
