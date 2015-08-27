package com.evanhalley.whereip;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides convenience functions for retrieving IP addresses and their locations
 * Created by evan on 8/15/15.
 */
public class LookupHelper {

    private static final String IP_INFO_DB_URL =
            "http://api.ipinfodb.com/v3/ip-city/?key=|KEY|&ip=|IP|&format=json";

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Validates the validity of an IP address, returns true if IP address is valid
     * @param ipAddress IP address to validate
     * @return true if the IP address is valid
     */
    public static boolean validateIpAddress(String ipAddress) {
        return IP_PATTERN.matcher(ipAddress).matches();
    }

    /**
     * Returns a list of IP addresses between the start and end IP address
     * @param ipAddressStart starting IP address
     * @param ipAddressEnd ending IP address
     * @return list of IP addresses in the range
     */
    public static List<String> getIpAddressesInRange(String ipAddressStart, String ipAddressEnd) {
        List<String> ipAddresses = new LinkedList<>();

        long ipNumberStart = ipStringToNumber(ipAddressStart);
        long ipNumberEnd = ipStringToNumber(ipAddressEnd);

        // if the end of the range is smaller than the beginning, reverse the numbers
        if (ipNumberEnd < ipNumberStart) {
            long temp = ipNumberEnd;
            ipNumberEnd = ipNumberStart;
            ipNumberStart = temp;
        }

        for (long i = ipNumberStart; i <= ipNumberEnd; i++) {
            ipAddresses.add(ipNumberToString(i));
        }
        return ipAddresses;
    }

    /**
     * Converts an ip address to it's integer equivalent
     * @param ipAddress IP address
     * @return number representation of the IP address
     */
    public static long ipStringToNumber(String ipAddress) {

        if (ipAddress == null) {
            throw new IllegalArgumentException("IP address is null");
        }

        String[] ipAddressArr = ipAddress.split("\\.");

        if (ipAddressArr.length != 4) {
            throw new IllegalArgumentException("IP address is malformed");
        }

        String binaryStr = toBinaryString(Integer.parseInt(ipAddressArr[0]), 8) +
                toBinaryString(Integer.parseInt(ipAddressArr[1]), 8) +
                toBinaryString(Integer.parseInt(ipAddressArr[2]), 8) +
                toBinaryString(Integer.parseInt(ipAddressArr[3]), 8);
        return Long.parseLong(binaryStr, 2);
    }

    /**
     * Converts a number to a legitimate IP address
     * @param ipNumber number representation of an IP address
     * @return IP address
     */
    public static String ipNumberToString(long ipNumber) {
        String binaryStr = toBinaryString(ipNumber, 32);
        return String.valueOf(Integer.parseInt(binaryStr.substring(0, 8), 2)) + "." +
                String.valueOf(Integer.parseInt(binaryStr.substring(8, 16), 2)) + "." +
                String.valueOf(Integer.parseInt(binaryStr.substring(16, 24), 2)) + "." +
                String.valueOf(Integer.parseInt(binaryStr.substring(24, 32), 2));
    }

    /**
     * Converts a number to a 0 padded binary string
     * @param value number
     * @param length padding
     * @return binary string
     */
    public static String toBinaryString(long value, int length) {
        String binaryStr = Long.toBinaryString(value);
        int paddingLength = length - binaryStr.length();
        char[] padding = new char[paddingLength];

        for (int i = 0; i < paddingLength; i++) {
            padding[i] = '0';
        }
        return String.valueOf(padding) + binaryStr;
    }

    /**
     * Returns the location of the IP address
     * @param ipAddress IP address to find a location for
     * @param apiKey API key to IP Info DB
     * @return location
     * @throws Exception
     */
    public static Location getLocationByIp(String ipAddress, String apiKey) throws Exception {
        Location location = null;

        if (ipAddress == null || !validateIpAddress(ipAddress)) {
            throw new IllegalArgumentException("Missing valid IP address");
        }

        if (apiKey == null ||apiKey.trim().length() == 0) {
            throw new IllegalArgumentException("Missing valid IP Info DB API key");
        }
        String url = IP_INFO_DB_URL.replace("|IP|", ipAddress).replace("|KEY|", apiKey);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String locationJson = response.body().string();

        if (locationJson != null) {
            JSONObject jsonObject = new JSONObject(locationJson);

            if ((jsonObject.getString("countryCode").contentEquals("-") &&
                    jsonObject.getString("countryName").contentEquals("-") &&
                    jsonObject.getString("timeZone").contentEquals("-") &&
                    jsonObject.getString("latitude").contentEquals("0"))) {
                throw new RuntimeException(String.format(
                        "Invalidate location encountered for IP address %s", ipAddress));
            }

            if (jsonObject.optString("statusCode", "BAD").contentEquals("OK")) {
                location = jsonToLocation(jsonObject);
            }
        }
        return location;
    }

    /**
     * Converts a JSON Object to a location object
     * @param json
     * @return
     * @throws JSONException
     */
    public static Location jsonToLocation(JSONObject json) throws JSONException {

        return new Location.Builder()
                .setCity(json.getString("cityName"))
                .setCountryName(json.getString("countryName"))
                .setIpAddress(json.getString("ipAddress"))
                .setLatitude(Double.parseDouble(json.getString("latitude")))
                .setLongitude(Double.parseDouble(json.getString("longitude")))
                .build();
    }
}