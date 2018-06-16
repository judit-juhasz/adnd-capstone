package name.juhasz.judit.udacity.tanits;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    @BindView(R.id.et_name) EditText mNameEditText;
    @BindView(R.id.et_email) EditText mEmailEditText;
    @BindView(R.id.et_birthdate_of_child) EditText mBirthdateOfChildEditText;
    @BindView(R.id.input_layout_name) TextInputLayout mInputLayoutName;
    @BindView(R.id.input_layout_birthdate) TextInputLayout mInputLayoutBirthdateOfChild;
    @BindView(R.id.button_save) Button mSaveButton;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(getActivity(), rootView);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final String childBirthdate =  new LocalDate(year, monthOfYear+1, dayOfMonth).toString();
                mBirthdateOfChildEditText.setText(childBirthdate);
            }
        };

        mBirthdateOfChildEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentBirthdate = mBirthdateOfChildEditText.getText().toString();
                LocalDate childBirthdate;
                try {
                    childBirthdate = new LocalDate(currentBirthdate);
                } catch (Exception e) {
                    childBirthdate = new LocalDate();
                }
                new DatePickerDialog(getContext(), date, childBirthdate.getYear(),
                        childBirthdate.getMonthOfYear()-1, childBirthdate.getDayOfMonth()).show();
            }
        });

        mNameEditText.addTextChangedListener(new MyTextWatcher(mNameEditText));
        mBirthdateOfChildEditText.addTextChangedListener(new MyTextWatcher(mBirthdateOfChildEditText));

        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInvalidConfig()) {
                    return;
                }


                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle(R.string.alert_title)
                        .setMessage(R.string.alert_message)
                        .setPositiveButton(R.string.alert_positiv, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final UserProfile userProfile =
                                        new UserProfile(mNameEditText.getText().toString(),
                                                mEmailEditText.getText().toString(),
                                                mBirthdateOfChildEditText.getText().toString());
                                mFirebaseDatabase.getReference("profiles/" +  currentFirebaseUser.getUid())
                                        .setValue(userProfile);
                            }
                        })
                        .setNegativeButton(R.string.alert_negativ, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(0)
                        .show();
            }
        });

        mFirebaseDatabase.getReference("profiles/" + currentFirebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final UserProfile user = dataSnapshot.getValue(UserProfile.class);
                        if (null != user) {
                            if (null != user.getName()) {
                                mNameEditText.setText(user.getName());
                            }
                            if (null != user.getEmail()) {
                                mEmailEditText.setText(user.getEmail());
                                mEmailEditText.setEnabled(false);
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

    private boolean validateName() {
        if (mNameEditText.getText().toString().trim().isEmpty()) {
            mInputLayoutName.setError(getString(R.string.error_message_name));
            return false;
        } else {
            mInputLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateBirthdate() {
        if (mBirthdateOfChildEditText.getText().toString().trim().isEmpty()) {
            mInputLayoutBirthdateOfChild.setError(getString(R.string.error_message_birthdate));
            registerForContextMenu(mBirthdateOfChildEditText);
            return false;
        } else {
            mInputLayoutBirthdateOfChild.setErrorEnabled(false);
        }
        return true;
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_name:
                    validateName();
                    break;
                case R.id.et_birthdate_of_child:
                    validateBirthdate();
                    break;
            }
        }
    }

    public boolean isInvalidConfig() {
        final boolean isNameCorrect = !mNameEditText.getText().toString().isEmpty();
        final boolean isBirthdateOfChildCorrect = !mBirthdateOfChildEditText.getText().toString().isEmpty();

        if (!isNameCorrect && !isBirthdateOfChildCorrect) {
            Toast.makeText(getContext(), getString(R.string.error_message_missing_name_birthdate),
                    Toast.LENGTH_LONG).show();
            return true;
        } else if (!isNameCorrect) {
            Toast.makeText(getContext(), getString(R.string.error_message_name), Toast.LENGTH_LONG).show();
            return true;
        } else if (!isBirthdateOfChildCorrect) {
            Toast.makeText(getContext(), getString(R.string.error_message_birthdate), Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }
}
