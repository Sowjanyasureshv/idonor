package com.lokas.idonor;

/**
 * Created by Bala on 12-01-2016.
 */
public class Employee {
    private String name;
    private int id;
    private String department;
    private String type;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDetails() {
        String result = id + ": " + name + "\n" + department + "-" + type;
        return result;
    }
}

