package com.example.enctiptstring;

/**
 * Created by Thor Odynson on 30.03.2021.
 */
public class Decrypt {

    public String stringOriginal;
    public String password;
    public String encrypted;

    public Decrypt() {
    }

    public Decrypt(String stringOriginal, String password, String encrypted) {
        this.stringOriginal = stringOriginal;
        this.password = password;
        this.encrypted = encrypted;
    }
}
