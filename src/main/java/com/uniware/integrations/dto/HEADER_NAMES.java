package com.uniware.integrations.dto;

public enum HEADER_NAMES {
    HOSTNAME, USERNAME, PASSWORD, TENANT_CODE, LOCATION_ID;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
