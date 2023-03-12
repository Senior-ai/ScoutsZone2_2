package com.a.shon.scoutszone2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Branch {
    String branchCode;
    String name;

    static private final String textFileName = "branch.txt";
    static  boolean mExternalStorageAvailable = false;
    static  boolean mExternalStorageWriteable = false;

    public Branch(String id, String branchCode, String name) {
        this.branchCode = branchCode;
        this.name = name;
    }

    public Branch(String branchCode, String name)
    {
        this.branchCode = branchCode;
        this.name = name;
    }

    public Branch(String line)
    {
        String[] data = line.split("\t");
        this.branchCode = data[0].trim();
        this.name = data[1].trim();
    }

    public String getBranchCode()
    {
        return branchCode;
    }

    public String getBranchName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "Branch{" +
                "branchCode=" + branchCode
                + ", branchName=" + name +
                '}';
    }

    static 	public void readFileFromResources(ArrayList<Branch> personArrList, Context context)
    {
        // Used for resources
        InputStream is = null;
        InputStreamReader isr = null;

        try
        {
            is = context.getResources().openRawResource(R.raw.branch);
            isr = new InputStreamReader(is, "UTF8");

            BufferedReader reader = new BufferedReader(isr);
            personArrList.clear(); // clear the list we are going to read
            String strLine = reader.readLine(); // ignore first line containing headers
            strLine = reader.readLine();
            while (strLine != null)
            {
                Branch p = new Branch(strLine);
                personArrList.add(p);
                Log.d("Read Branch", p.toString());
                strLine = reader.readLine();
            }
            reader.close();
            isr.close();
            is.close();

        } catch (Exception e)
        {
            Log.e("ReadFromFile", "Error reading from file: persons.txt");
            e.printStackTrace();
        } // catch
        // TODO - add finally and move the close statements there, with the respective if's
    }

    // Reads a file from external memory
    // TODO - get the file name as a parameter rather than a hard-coded constant
    static 	public void readFile(ArrayList<Branch> personArrList, Context context)
    {

        InputStreamReader isr = null;
        FileInputStream fis = null;
        try
        {
            checkExternalStorageState(); // results are stored in mExternalStorageAvailable, mExternalStorageWritable
            if (mExternalStorageAvailable)
            {
                File txtFile = new File(context.getExternalFilesDir(null),
                        textFileName);
                fis = new FileInputStream(txtFile);
                isr = new InputStreamReader(fis, "UTF8");


                BufferedReader reader = new BufferedReader(isr);
                personArrList.clear(); // clear the list we are going to read
                String strLine = reader.readLine(); // ignore first line containing headers
                strLine = reader.readLine();
                while (strLine != null)
                {
                    Branch p = new Branch(strLine);
                    personArrList.add(p);
                    Log.d("Read Person", p.toString());
                    strLine = reader.readLine();
                }
                reader.close();
                isr.close();
                fis.close();
            }
            // TODO - add else with appropriate message
        } catch (Exception e)
        {
            Log.e("ReadFromFile", "Error reading from file: persons.txt");
            e.printStackTrace();
        } // catch
        // TODO - add finally and move the close statements there, with the respective if's
    }

    static public void checkExternalStorageState()
    {
        // Verify that the external storage is available for writing
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        { // We can read and write the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = true;
        } else
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {    // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else { // Something else is wrong. we can neither read nor write
            mExternalStorageAvailable = false;
            mExternalStorageWriteable = false;
        }
    }
}


