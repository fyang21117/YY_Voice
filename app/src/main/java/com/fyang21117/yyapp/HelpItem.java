package com.fyang21117.yyapp;

import org.litepal.crud.DataSupport;

public class HelpItem extends DataSupport {
    private int    id;
    private String name;
    private String info;
    private int    image_photo;

    private CharSequence Appname;
    private CharSequence Actname;
    private String       Packagename;

    public HelpItem() {
        super();
    }

    public HelpItem(int id, String name, String Packagename, CharSequence Appname,
                    CharSequence Actname) {
        super();
        this.id = id;
        this.name = name;
        this.Packagename = Packagename;
        this.Actname = Actname;
        this.Appname = Appname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getImage_photo() {
        return image_photo;
    }

    public void setImage_photo(int image_photo) {
        this.image_photo = image_photo;
    }

    @Override
    public String toString() {
        return "Item[id=" + id + " , name=" + name + "]";
    }


    public CharSequence getAppname() {
        return Appname;
    }

    public void setAppname(CharSequence appname) {
        this.Appname = appname;
    }

    public String getPackagename() {
        return Packagename;
    }

    public void setPackagename(String packagename) {
        this.Packagename = packagename;
    }

    public CharSequence getActname() {
        return Actname;
    }

    public void setActname(CharSequence actname) {
        this.Actname = actname;
    }
}
