package com.root.role;


public class Role  {

    private  boolean  OWNER;
    private  boolean  ADMIN;
    private  boolean  USER;

    public boolean isOWNER() {
        return OWNER;
    }

    public void setOWNER(boolean OWNER) {
        this.OWNER = OWNER;
    }

    public boolean isADMIN() {
        return ADMIN;
    }

    public void setADMIN(boolean ADMIN) {
        this.ADMIN = ADMIN;
    }

    public boolean isUSER() {
        return USER;
    }

    public void setUSER(boolean USER) {
        this.USER = USER;
    }

    @Override
    public String toString() {
        return "Role{" +
                "OWNER=" + OWNER +
                ", ADMIN=" + ADMIN +
                ", USER=" + USER +
                '}';
    }
}
