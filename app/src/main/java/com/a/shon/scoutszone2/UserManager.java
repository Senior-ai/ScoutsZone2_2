package com.a.shon.scoutszone2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.a.shon.scoutszone2.enums.GradeType;
import com.a.shon.scoutszone2.enums.RoleType;

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

import static com.a.shon.scoutszone2.Firebases.UserFirebase.UserID;

public class UserManager {
    private String userID;
    private static String password;
    private String firstName;
    private String lastName;
    private String email;
    private RoleType role; //role in the shevet
    private GradeType grade; //grade
    private String branchCode;  // the number of the place/tribe to identify.
    private boolean isApproved;

    public UserManager(String password, String firstName, String lastName, String email, RoleType role, GradeType grade, String branchCode, boolean isApproved) {
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.grade = grade;
        this.branchCode = branchCode;
        this.isApproved = isApproved;
    }

    public UserManager(String password, String email) {
        this.password = password;
        this.email = email;
    }

    public UserManager(String firstName, String lastName, RoleType role, String email,  boolean isApproved) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.isApproved = isApproved;
    }

    public UserManager(String userID, String firstName, String lastName, String email, String branchCode, RoleType role, GradeType grade, boolean isApproved) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.grade = grade;
        this.branchCode = branchCode;
        this.isApproved = isApproved;
    }

    public UserManager(String firstname, String lastname, String email, RoleType roleType, GradeType gradeType, boolean isApproved, String branchCode)
    {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.role = roleType;
        this.grade = gradeType;
        this.isApproved = isApproved;
        this.branchCode = branchCode;
    }

    public UserManager(String firstname, String lastname, String email, RoleType roleType, GradeType gradeType, boolean isApproved) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.role = roleType;
        this.grade = gradeType;
        this.isApproved = isApproved;
    }

    public UserManager(String userID, String firstName, String lastName, RoleType role, GradeType grade, String email, boolean isApproved) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.grade = grade;
        this.email = email;
        this.isApproved = isApproved;

    }

    public String getUserID() {return userID;}

    public static String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return firstName;
    }

    public void setName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public GradeType getGrade() {
        return grade;
    }

    public void setGrade(GradeType grade) {
        this.grade = grade;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
