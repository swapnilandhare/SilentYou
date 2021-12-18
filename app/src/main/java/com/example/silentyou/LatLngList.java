package com.example.silentyou;

import android.content.Context;
import android.content.ContextWrapper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LatLngList extends ContextWrapper {
    public LatLngList(Context base) {
        super(base);
    }
    public static ArrayList<LatLng> arrayList;
    void setArrayList(ArrayList<LatLng> arrayList)
    {
        this.arrayList=arrayList;
    }
    ArrayList<LatLng> getArrayList()
    {
        return arrayList;
    }
}
