package name.juhasz.judit.udacity.tanits;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_DONE = 1;
    public static final int STATUS_REJECTED = 2;

    private String mId;
    private String mSubject;
    private String mDate;
    private int mStatus;
    private String mSummary;

    public Message(final String id, final String subject, final String date, final int status, final String summary) {
        this.mId = id;
        this.mSubject = subject;
        this.mDate = date;
        this.mStatus = status;
        this.mSummary = summary;
    }

    public String getId() {
        return mId;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getDate() {
        return mDate;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setSubject(final String subject) {
        this.mSubject = subject;
    }

    public void setDate(final String date) {
        this.mDate = date;
    }

    public void setStatus(final int status) {
        this.mStatus = status;
    }

    public void setSummary(final String summary) {
        this.mSummary = summary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(mId);
        parcel.writeString(mSubject);
        parcel.writeString(mDate);
        parcel.writeInt(mStatus);
        parcel.writeString(mSummary);
    }

    public Message(final Parcel in) {
        mId = in.readString();
        mSubject = in.readString();
        mDate = in.readString();
        mStatus = in.readInt();
        mSummary = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(final Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(final int size) {
            return new Message[size];
        }
    };
}
