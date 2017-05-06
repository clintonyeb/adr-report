package com.clinton.adrreport.dbflow;


import android.os.Parcel;
import android.os.Parcelable;
import com.raizlabs.android.dbflow.annotation.*;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = ADRDatabase.class)
public class PatientReport extends BaseModel implements Parcelable {

    @PrimaryKey(autoincrement = true)
    @Column
    private long id;

    @Column
    @NotNull
    private String patientName;

    @Column
    private String patientId;

    @Column
    private String imageFile1;

    @Column
    private String imageFile2;

    @Column
    private String imageFile3;

    @Column
    private String imageFile4;

    @Column
    private String serverId;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getImageFile1() {
        return imageFile1;
    }

    public void setImageFile1(String imageFile1) {
        this.imageFile1 = imageFile1;
    }

    public String getImageFile2() {
        return imageFile2;
    }

    public void setImageFile2(String imageFile2) {
        this.imageFile2 = imageFile2;
    }

    public String getImageFile3() {
        return imageFile3;
    }

    public void setImageFile3(String imageFile3) {
        this.imageFile3 = imageFile3;
    }

    public String getImageFile4() {
        return imageFile4;
    }

    public void setImageFile4(String imageFile4) {
        this.imageFile4 = imageFile4;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.patientName);
        dest.writeString(this.patientId);
        dest.writeString(this.imageFile1);
        dest.writeString(this.imageFile2);
        dest.writeString(this.imageFile3);
        dest.writeString(this.imageFile4);
        dest.writeString(this.serverId);
    }

    public PatientReport() {
    }

    protected PatientReport(Parcel in) {
        this.id = in.readLong();
        this.patientName = in.readString();
        this.patientId = in.readString();
        this.imageFile1 = in.readString();
        this.imageFile2 = in.readString();
        this.imageFile3 = in.readString();
        this.imageFile4 = in.readString();
        this.serverId = in.readString();
    }

    public static final Parcelable.Creator<PatientReport> CREATOR = new Parcelable.Creator<PatientReport>() {
        @Override
        public PatientReport createFromParcel(Parcel source) {
            return new PatientReport(source);
        }

        @Override
        public PatientReport[] newArray(int size) {
            return new PatientReport[size];
        }
    };
}
