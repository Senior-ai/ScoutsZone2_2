package com.a.shon.scoutszone2;

import com.google.firebase.firestore.auth.User;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class Assignment {
    //public enum TimeSlot { Morning, Noon, EarlyEvening, LateEvening};
    // option - change to 1/2 hour fixed slots
    String AssignmentID;
    String branchCode;
    String ZoneId;
    String UserId;
    String date;
    String slot;

    public Assignment(String slot, String date, String UserId, String ZoneId, String branchCode, String AssignmentID)
    {
        this.branchCode = branchCode;
        this.ZoneId = ZoneId;
        this.UserId = UserId;
        this.date = date;
        this.slot = slot;
        this.AssignmentID = AssignmentID;
    }

    public Assignment(String slot, String date, String UserId, String ZoneId, String branchCode)
    {
        this.branchCode = branchCode;
        this.ZoneId = ZoneId;
        this.UserId = UserId;
        this.date = date;
        this.slot = slot;
    }
    public String getBranchCode()
    { return branchCode;}

    public String getZoneId()
    { return ZoneId;}

    public String getUserId()
    {return UserId;}

    public String getDate()
    { return date;}

    public String getTimeslot()
    {return slot;}

    @Override
    public String toString() {
        return "Assignment{" +
                "branchCode=" + branchCode +
                ", UserId=" + UserId +
                ", ZoneId=" + ZoneId +
                ", date=" + date +
                ", slot=" + slot +
                '}';
    }
}
