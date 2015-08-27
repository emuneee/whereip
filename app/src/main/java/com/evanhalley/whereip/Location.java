package com.evanhalley.whereip;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a location composed of the IP address, city, country, and GPS coordinates
 * Immutable
 * Created by evan on 8/15/15.
 */
public class Location implements Parcelable {

    private final String mIpAddress;
    private final double mLatitude;
    private final double mLongitude;
    private final String mCountryName;
    private final String mCity;

    public Location(Parcel in) {
        mIpAddress = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mCity = in.readString();
        mCountryName = in.readString();
    }

    private Location(String city, String countryName, String ipAddress, double latitude,
                     double longitude) {
        mCity = city;
        mCountryName = countryName;
        mIpAddress = ipAddress;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIpAddress);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mCity);
        dest.writeString(mCountryName);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public String getCity() {
        return mCity;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return mIpAddress.equals(location.mIpAddress);
    }

    @Override
    public int hashCode() {
        return mIpAddress.hashCode();
    }

    @Override
    public String toString() {
        return "Location{" +
                "mCity='" + mCity + '\'' +
                ", mIpAddress='" + mIpAddress + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                ", mCountryName='" + mCountryName + '\'' +
                '}';
    }

    /**
     * Builds a location object
     */
    public static class Builder {

        private String mIpAddress;
        private double mLatitude;
        private double mLongitude;
        private String mCountryName;
        private String mCity;

        public Builder setCity(String city) {
            mCity = city;
            return this;
        }

        public Builder setCountryName(String countryName) {
            mCountryName = countryName;
            return this;
        }

        public Builder setIpAddress(String ipAddress) {
            mIpAddress = ipAddress;
            return this;
        }

        public Builder setLatitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        public Location build() {

            if (mCity == null) {
                throw new IllegalArgumentException("City value cannot be null");
            }

            if (mCountryName == null) {
                throw new IllegalArgumentException("Country name value cannot be null");
            }

            if (mIpAddress == null) {
                throw new IllegalArgumentException("IP Address cannot be null");
            }

            if (mLatitude < -90 || mLatitude > 90) {
                throw new IllegalArgumentException("Invalid value for latitude");
            }

            if (mLongitude < -180 || mLongitude > 180) {
                throw new IllegalArgumentException("Invalid value for longitude");
            }

            return new Location(mCity, mCountryName, mIpAddress, mLatitude, mLongitude);
        }
    }
}
