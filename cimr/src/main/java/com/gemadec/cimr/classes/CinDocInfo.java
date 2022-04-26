package com.gemadec.cimr.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

public class CinDocInfo implements Parcelable {

    private String passnumber;
    private String firstName;
    private String lastName;
    private String identifier;
    private String nationality;
    private String sexe;
    private String address;
    private Bitmap photo;
    private String birthdate;
    private String expirydate;

    public CinDocInfo() {

    }


    protected CinDocInfo(Parcel in) {
        passnumber = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        identifier = in.readString();
        nationality = in.readString();
        sexe = in.readString();
        address = in.readString();
        photo = in.readParcelable(Bitmap.class.getClassLoader());
        birthdate = in.readString();
        expirydate = in.readString();
    }


    public static final Creator<CinDocInfo> CREATOR = new Creator<CinDocInfo>() {
        @Override
        public CinDocInfo createFromParcel(Parcel in) {
            return new CinDocInfo(in);
        }

        @Override
        public CinDocInfo[] newArray(int size) {
            return new CinDocInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(passnumber);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(identifier);
        dest.writeString(nationality);
        dest.writeString(sexe);
        dest.writeString(address);
        dest.writeParcelable(photo, flags);
        dest.writeString(birthdate);
        dest.writeString(expirydate);
    }

    @Override
    public int describeContents() {
        return 0;
    }
;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(String expirydate) {
        this.expirydate = expirydate;
    }

    public String getPassnumber() {
        return passnumber;
    }

    public void setPassnumber(String passnumber) {
        this.passnumber = passnumber;
    }
    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationality() {
        return nationality;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getSexe() {
        return sexe;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName + " ";
    }

}
