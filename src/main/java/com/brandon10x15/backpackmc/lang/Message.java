// src/main/java/com/brandon10x15/backpackmc/lang/Message.java
package com.brandon10x15.backpackmc.lang;

public enum Message {
    NO_PERMISSION("no-permission");
    private final String path;
    Message(String path){ this.path = path; }
    public String path(){ return path; }
}
