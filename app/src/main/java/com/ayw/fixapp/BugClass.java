package com.ayw.fixapp;

public class BugClass {

    private int number = 2;

    public String getNumber() {
        if (number < 10) {
            throw new RuntimeException("number must > 0");
        }
        return String.valueOf(number);
    }
}
