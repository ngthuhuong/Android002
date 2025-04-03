package com.example.android002;

public class CallHistoryItem {
    private String phoneNumber;
    private String callType;
    private String callDate;
    private String callDuration;
    private String contactName;

    // Constructor
    public CallHistoryItem(String phoneNumber, String callType, String callDate,
                           String callDuration, String contactName) {
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDate = callDate;
        this.callDuration = callDuration;
        this.contactName = contactName;
    }

    // Getters
    public String getPhoneNumber() { return phoneNumber; }
    public String getCallType() { return callType; }
    public String getCallDate() { return callDate; }
    public String getCallDuration() { return callDuration; }
    public String getContactName() { return contactName; }
}