package name.juhasz.judit.udacity.tanits;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {

    public String name;
    public String email;
    public String childBirthdate;

    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserProfile(final String name, final String email, final String childBirthdate) {
        this.name = name;
        this.email = email;
        this.childBirthdate = childBirthdate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getChildBirthdate() {
        return childBirthdate;
    }
}
