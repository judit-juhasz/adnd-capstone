package name.juhasz.judit.udacity.tanits;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable{

    private String mSubject;
    private String mDate;

    public Message(final String subject, final String date) {
        this.mSubject = subject;
        this.mDate = date;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getDate() {
        return mDate;
    }

    public void setSubject(final String subject) {
        this.mSubject = subject;
    }

    public void setDate(final String date) {
        this.mDate = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(mSubject);
        parcel.writeString(mDate);
    }

    public Message(final Parcel in) {
        mSubject = in.readString();
        mDate = in.readString();
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
