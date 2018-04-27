package com.milink.android.airscale;


public class ScaleData {
    public static final int ERROR_LOWPOWER = 0x6;
    public static final int ERROR_OVERLOAD = 0x5;
    public static final int ERROR_UNST = 0x7;
    public static final int STATE_CHANGEUNIT = 0x9;
    public static final int STATE_CONNECTING = 0x1;
    public static final int STATE_FAT_MEASURING = 0x4;
    public static final int STATE_LOCKED = 0x3;
    public static final int STATE_POWOFF = 0x8;
    public static final int STATE_WEIGHING = 0x2;
    public static final int TYPE_BODY_SCALE = 0x0;
    public static final int TYPE_FAT_SCALE = 0x1;
    private int resistance;
    private float weight;
    private int type = 0x0;
    private int state = 0x8;
    
    public ScaleData() {
    }
    
    public ScaleData(int t, int s, float w, int r) {
        type = t;
        state = s;
        weight = w;
        resistance = r;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int t) {
        type = t;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int s) {
        state = s;
    }
    
    public float getWeight() {
        return weight;
    }
    
    public void setWeight(float w) {
        weight = w;
    }
    
    public int getResistance() {
        return resistance;
    }
    
    public void setResistance(int r) {
        resistance = r;
    }
}
