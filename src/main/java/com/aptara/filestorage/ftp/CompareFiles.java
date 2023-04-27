package com.aptara.filestorage.ftp;

import java.io.*;
import java.security.MessageDigest;
import sun.net.TelnetInputStream;

public class CompareFiles {

    boolean FileCheck = false;

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public static byte[] createChecksum(TelnetInputStream filename) throws Exception {
        InputStream fis = (InputStream) (filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public static byte[] createChecksum(InputStream filename) throws Exception {
        InputStream fis = filename;

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String getMD5Checksum(TelnetInputStream filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String getMD5Checksum(InputStream filename) throws Exception {
        byte[] b = createChecksum(filename);
        System.out.println("length::::::::" + b.length);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String getMD5Checksum(FileInputStream filename) throws Exception {
        byte[] b = createChecksum((InputStream) filename);
        System.out.println("length::::::::" + b.length);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static int check(String filename1, TelnetInputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        int rc = 0;
        try {
            String chk1 = getMD5Checksum(filename1);
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            System.out.println("Got chk2 ::::" + chk2);

            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc = 0;
            } else {
                System.out.println("Different!");
                rc = 1;
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        }
    }

    public static String[] check(String filename1, String filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        String[] rc = null;
        try {
            rc = new String[3];
            String chk1 = getMD5Checksum(filename1);
            System.out.println("Got chk1 ::::" + chk1);
            rc[0] = chk1;
            String chk2 = getMD5Checksum(filename2);
            System.out.println("Got chk2 ::::" + chk2);
            rc[1] = chk2;
            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc[2] = "0";
            } else {
                System.out.println("Different!");
                rc[2] = "1";
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        }
    }

    public static int check(String filename1, InputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        int rc = 0;
        try {
            String chk1 = getMD5Checksum(filename1);
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            System.out.println("Got chk2 ::::" + chk2);

            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc = 0;
            } else {
                System.out.println("Different!");
                rc = 1;
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        }
    }

    /**************
     *New method added by neeraj on 18 Oct 2011
     *Method name - check
     *Return type - string []
     *********************/
    public static String[] checkNew(String filename1, InputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        String[] rc = null;
        try {
            rc = new String[3];
            String chk1 = getMD5Checksum(filename1);
            rc[0] = chk1;
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            rc[1] = chk2;
            System.out.println("Got chk2 ::::" + chk2);
            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc[2] = "0";
            } else {
                System.out.println("Different!");
                rc[2] = "1";
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        } finally {
            try {
                if (filename2 != null) {
                    filename2.close();
                    filename2 = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**************
     *New method added by neeraj on 18 Oct 2011
     *Method name - check
     *Return type - string []
     *********************/
    public static String[] checkNew2(String filename1, InputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        String[] rc = null;
        try {
            rc = new String[3];
            //String chk1 = getMD5Checksum(filename1);
            String chk1 = filename1;
            rc[0] = chk1;
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            rc[1] = chk2;
            System.out.println("Got chk2 ::::" + chk2);
            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc[2] = "0";
            } else {
                System.out.println("Different!");
                rc[2] = "1";
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        } finally {
            try {
                if (filename2 != null) {
                    filename2.close();
                    filename2 = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ADDED METHOD FOR AUTOUPLOAD MODULE ///
    public static int check(InputStream filename1, InputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        int rc = 0;
        try {
            String chk1 = getMD5Checksum(filename1);
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            System.out.println("Got chk2 ::::" + chk2);
            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc = 0;
            } else {
                System.out.println("Different!");
                rc = 1;
            }
            //is.close();
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        }
    }

    /***********
     *Added by neeraj on 19 oct 2011
     *Method - CheckNew
     *Argument - two inputStream
     *Return type - String []
     ***************/
    public static String[] checkNew(InputStream filename1, InputStream filename2) {
        System.out.println("Inside file MD5 checksum ::::");
        String[] rc = null;
        try {
            rc = new String[3];
            String chk1 = getMD5Checksum(filename1);
            rc[0] = chk1;
            System.out.println("Got chk1 ::::" + chk1);
            String chk2 = getMD5Checksum(filename2);
            rc[1] = chk2;
            System.out.println("Got chk2 ::::" + chk2);
            System.out.println("chk1 = " + chk1 + " and chk2 = " + chk2);
            if (chk2.equals(chk1)) {
                System.out.println("Same!");
                rc[2] = "0";
            } else {
                System.out.println("Different!");
                rc[2] = "1";
            }
            return rc;
        } catch (Exception e) {
            e.printStackTrace();
            return rc;
        }
    }
    /************************************************************************************/
    //public static void main(String[] args)
    //	{
    //	System.out.println("Difference = "+check("TIJ4-code.zip","TIJ4-code.zip"));
    //	}
}
