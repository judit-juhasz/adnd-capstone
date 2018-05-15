package name.juhasz.judit.udacity.tanits;

public class Message {

    private String mSubject;
    private String mDate;

    public Message(String subject, String date) {
        this.mSubject = subject;
        this.mDate = date;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getDate() {
        return mDate;
    }
}
