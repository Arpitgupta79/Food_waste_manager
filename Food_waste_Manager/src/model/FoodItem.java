
package model;

import java.sql.Date;

public class FoodItem {
    private String name;
    private String category;
    private String quantity;
    private Date expdate;


    public FoodItem( String name,String category, String quantity,Date expdate ){
        this.name=name;
        this.category=category;
        this.quantity=quantity;
        this.expdate=expdate;
    }

    public String getname(){ return name;}
    public void setname(String name){this.name=name;}

    public String getcategory(){ return category;}
    public void setcategory(String category){this.category=category;}

    public String getquantity(){ return quantity;}
    public void setquantity(String quantity){this.quantity=quantity;}

    public Date getexpdate(){return expdate;}
    public void setData(Date expdate){this.expdate=expdate;}


    @Override
    public String toString(){
        return "Item{"+
                ",name=' " + name + '\'' +
                ", category=' " + category + '\'' +
                ",quantity=' " + quantity  +
                ",expdate=" + expdate +
                '}';
    }
}
