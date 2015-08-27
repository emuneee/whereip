package com.evanhalley.whereip;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LookupHelperTest {

    @Test
    public void validateIpTest() {
        assertEquals(true, LookupHelper.validateIpAddress("0.0.0.0"));
        assertEquals(true, LookupHelper.validateIpAddress("255.255.255.255"));
        assertEquals(false, LookupHelper.validateIpAddress("0.0.0"));
        assertEquals(false, LookupHelper.validateIpAddress("0.0.0.300"));
        assertEquals(false, LookupHelper.validateIpAddress("Evan Halley"));
        assertEquals(false, LookupHelper.validateIpAddress("255255255255"));
    }

    @Test
    public void toBinaryStringTest() {
        assertEquals("11111111", LookupHelper.toBinaryString(255, 8));
        assertEquals("00000000", LookupHelper.toBinaryString(0, 8));
        assertEquals("00001111", LookupHelper.toBinaryString(15, 8));
        assertEquals("001111", LookupHelper.toBinaryString(15, 6));
        assertEquals("1111", LookupHelper.toBinaryString(15, 4));
    }

    @Test
    public void ipStringToNumberTest() {
        assertEquals(0, LookupHelper.ipStringToNumber("0.0.0.0"));
        assertEquals(255, LookupHelper.ipStringToNumber("0.0.0.255"));
        assertEquals(1824853417, LookupHelper.ipStringToNumber("108.197.13.169"));
        assertEquals(4294967295L, LookupHelper.ipStringToNumber("255.255.255.255"));
    }

    @Test
    public void ipNumberToStringTest() {
        assertEquals("0.0.0.0", LookupHelper.ipNumberToString(0));
        assertEquals("0.0.0.255", LookupHelper.ipNumberToString(255));
        assertEquals("108.197.13.169", LookupHelper.ipNumberToString(1824853417));
        assertEquals("255.255.255.255", LookupHelper.ipNumberToString(4294967295L));
    }

    @Test
    public void getIpAddressesInRangeTest() {
        List<String> addresses = LookupHelper.getIpAddressesInRange("0.0.0.0", "0.0.1.0");
        assertEquals(257, addresses.size());
        addresses = LookupHelper.getIpAddressesInRange("0.0.0.0", "0.1.0.0");
        assertEquals(65537, addresses.size());
        addresses = LookupHelper.getIpAddressesInRange("0.1.0.0", "0.0.0.0");
        assertEquals(65537, addresses.size());
        addresses = LookupHelper.getIpAddressesInRange("192.168.1.0", "192.168.1.255");
        assertEquals(256, addresses.size());
        addresses = LookupHelper.getIpAddressesInRange("192.168.1.0", "192.168.1.0");
        assertEquals(1, addresses.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLocationByIpTestInvalidIp() throws Exception {
        LookupHelper.getLocationByIp(
                null, "3204fdf2d16fc5fd10a5b8a1dae82b1a1fee4f039c8e4fd240ceafd8e9505caa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLocationByIpTestInvalidApiKey() throws Exception {
        LookupHelper.getLocationByIp(
                null, "3204fdf2d16fc5fd10a5b8a1dae82b1a1fee4f039c8e4fd240ceafd8e9505caa");
    }
}