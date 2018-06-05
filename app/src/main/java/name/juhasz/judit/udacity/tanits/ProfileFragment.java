package name.juhasz.judit.udacity.tanits;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

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

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                mBirthdateOfChildEditText.setText(sdf.format(myCalendar.getTime()));
            }

        };

        mBirthdateOfChildEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final UserProfile userProfile =
                        new UserProfile(mDisplayNameEditText.getText().toString(),
                                mEmailEditText.getText().toString(),
                                mBirthdateOfChildEditText.getText().toString());
                mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid())
                        .setValue(userProfile);
            }
        });

        mFirebaseDatabase.getReference("profiles/" + currentFirebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final UserProfile user = dataSnapshot.getValue(UserProfile.class);
                        if (null != user) {
                            if (null != user.getName()) {
                                mDisplayNameEditText.setText(user.getName());
                            }
                            if (null != user.getEmail()) {
                                mEmailEditText.setText(user.getEmail());
                            }
                            if (null != user.getChildBirthdate()) {
                                mBirthdateOfChildEditText.setText(user.getChildBirthdate());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "User profile data is not available", databaseError.toException());
                    }
                });

        return rootView;
    }
}
