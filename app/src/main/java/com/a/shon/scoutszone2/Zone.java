package com.a.shon.scoutszone2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.a.shon.scoutszone2.enums.ZoneType;
import com.a.shon.scoutszone2.enums.isZonebusy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Zone {
        private String id; // YY
        String branchCode;
        private ZoneType type; //inside, insideRoom , outside
        private isZonebusy isActive;
        private String name;
        private int space; //number of people who can fit in the place.
        private double rating;
        private int raters; // number of people who rated the zone
        private String imageFileURI;

        public Zone(String id, String branchCode, String name, ZoneType type, isZonebusy isActive,  int space)
        {
            this.id = id;
            this.branchCode = branchCode;
            this.name = name;
            this.type = type;
            this.space = space;
            this.isActive = isActive;
            this.rating = 0;
            this.raters = 0;
        }

         public Zone(String name, ZoneType type, isZonebusy isActive, int space)
         {
             this.name = name;
            this.type = type;
            this.space = space;
            this.isActive = isActive;
             this.rating = 0;
             this.raters = 0;
         }

        public Zone(String id,String name,  String branchCode, ZoneType type, int space, isZonebusy isActive, double rating, int raters, String imageFileURI)
        {
            this.id = id;
            this.name = name;
            this.branchCode = branchCode;
            this.type = type;
            this.space = space;
            this.isActive = isActive;
            this.rating = rating;
            this.raters = raters;
            this.imageFileURI = imageFileURI;
        }

        public Zone(String id, double rating, int raters)
        {
            this.id = id;
            this.rating = rating;
            this.raters = raters;
        }

    public Zone(String id, String name, String branchCode, ZoneType type, int space, isZonebusy isActive) {
            this.id = id;
            this.name = name;
            this.branchCode = branchCode;
            this.type = type;
            this.space = space;
            this.isActive = isActive;
            this.isActive = isActive;
            this.rating = 0;
            this.raters = 0;
    }

    public Zone(String name, ZoneType type, isZonebusy isActive, int space, String branchCode, String imageFileURI) {
            this.name = name;
            this.type = type;
            this.isActive = isActive;
            this.space = space;
            this.branchCode = branchCode;
            this.imageFileURI = imageFileURI;
    }

    public Zone(String id, String name, ZoneType type, isZonebusy isActive, int space, String branchCode, String imageFileURI) {
        this.id =id;
        this.name = name;
        this.type = type;
        this.isActive = isActive;
        this.space = space;
        this.branchCode = branchCode;
        this.imageFileURI = imageFileURI;
        }


    public String getId()
        { return id;}

        public String getBranchCode()
        { return branchCode;}

        public ZoneType getType()
        { return type;}

        public String getZonename()
        { return name;}

        public int getSpace() {return space;}

        public double getRating() {return rating;}

        public int getRaters() {return raters;}

        public isZonebusy getAvailability() {return isActive;}

        public void setZonename()
        { this.name = name;}

        public void setType()
        { this.type = type;}

        public void setSpace()
        { this.space = space;}

        public void setRating()
        { this.rating = rating;}

    public String getImageFileURI()
    {
        return imageFileURI;
    }

    public void setImageFileURI(String imageFileURI)
    {
        this.imageFileURI = imageFileURI;
    }

       // public void setIsActive() {this.isActive = isActive;}

    public void setImage(Bitmap bmp)
    {
        // TODO
    }

    @Override
    public String toString() {
        return "Zone{" + "name= " + name + "type= " + type + '\'' + ", space= " + space + '}';
    }

    public String toDetailedString()
    {
        return toString() + "\n<" + id + ">\n" + imageFileURI;
    }




    public void addRating(double newRating)
    {
        this.rating = (this.raters * this.rating + newRating) / (this.raters + 1);
        this.raters++;

    }
}