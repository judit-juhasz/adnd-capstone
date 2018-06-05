package name.juhasz.judit.udacity.tanits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase mFirebaseDatabase;
    private EditText mDisplayNameEditText;
    private EditText mEmailEditText;
    private EditText mBirthdateOfChildEditText;
    private Button mSaveButton;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mSaveButton = rootView.findViewById(R.id.button_save);
        mDisplayNameEditText = rootView.findViewById(R.id.et_display_name);
        mEmailEditText = rootView.findViewById(R.id.et_email);
        mBirthdateOfChildEditText = rootView.findViewById(R.id.et_birthdate_of_child);
        mBirthdateOfChildEditText.addTextChangedListener(tw);

        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/displayname")
                        .setValue(mDisplayNameEditText.getText().toString());
                mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/email")
                        .setValue(mEmailEditText.getText().toString());
                mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/birthday")
                        .setValue(mBirthdateOfChildEditText.getText().toString());
            }
        });

        mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/displayname")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String value = dataSnapshot.getValue(String.class);
                        if (null != value) {
                            mDisplayNameEditText.setText(value);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String value = dataSnapshot.getValue(String.class);
                        if (null != value) {
                            mEmailEditText.setText(value);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid() + "/birthday")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String value = dataSnapshot.getValue(String.class);
                            if (null != value) {
                                mBirthdateOfChildEditText.setText(value);
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        return rootView;
    }

    TextWatcher tw = new TextWatcher() {
        private String current = "";
        private String ddmmyyyy = "DDMMYYYY";
        private Calendar cal = Calendar.getInstance();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals(current)) {
                String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                int cl = clean.length();
                int sel = cl;
                for (int i = 2; i <= cl && i < 6; i += 2) {
                    sel++;
                }

                if (clean.equals(cleanC)) sel--;

                if (clean.length() < 8){
                    clean = clean + ddmmyyyy.substring(clean.length());
                } else {

                    int day  = Integer.parseInt(clean.substring(0,2));
                    int mon  = Integer.parseInt(clean.substring(2,4));
                    int year = Integer.parseInt(clean.substring(4,8));

                    mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                    cal.set(Calendar.MONTH, mon-1);
                    year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
                    cal.set(Calendar.YEAR, year);

                    day = (day > cal.getActualMaximum(Calendar.DATE))
                            ? cal.getActualMaximum(Calendar.DATE) : day;
                    clean = String.format("%02d%02d%02d",day, mon, year);
                }

                clean = String.format("%s/%s/%s", clean.substring(0, 2), clean.substring(2, 4),
                        clean.substring(4, 8));

                sel = sel < 0 ? 0 : sel;
                current = clean;
                mBirthdateOfChildEditText.setText(current);
                mBirthdateOfChildEditText.setSelection(sel < current.length() ? sel : current.length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };
}
