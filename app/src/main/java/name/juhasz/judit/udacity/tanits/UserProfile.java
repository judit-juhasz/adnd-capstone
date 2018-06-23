package name.juhasz.judit.udacity.tanits;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {

    private String name;
    private String email;
    private String childBirthdate;

    public UserProfile() {
        // Default constructor is required for calls to DataSnapshot.getValue(User.class)
    }

    public UserProfile(final String name, final String email, final String childBirthdate) {
        this.name = name;
        this.email = email;
        this.childBirthdate = childBirthdate;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getChildBirthdate() {
        return childBirthdate;
    }

    public void setChildBirthdate(final String childBirthdate) {
        this.childBirthdate = childBirthdate;
    }

}
