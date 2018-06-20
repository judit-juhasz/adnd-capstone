package name.juhasz.judit.udacity.tanits;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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

import com.google.firebase.database.DatabaseError;

import org.joda.time.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.DateTimeUtils;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    @BindView(R.id.et_name)
    EditText mNameEditText;
    @BindView(R.id.et_email)
    EditText mEmailEditText;
    @BindView(R.id.et_birthdate_of_child)
    EditText mBirthdateOfChildEditText;
    @BindView(R.id.input_layout_name)
    TextInputLayout mInputLayoutName;
    @BindView(R.id.input_layout_birthdate)
    TextInputLayout mInputLayoutBirthdateOfChild;
    @BindView(R.id.button_save)
    Button mSaveButton;
    @BindView(R.id.cl_fragment_profile)
    CoordinatorLayout mCoordinatorLayout;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, rootView);

        setupNameField();
        setupBirthdateCalendarPopup();
        setupSavePopup();
        queryUserProfileData();
        updateSaveButtonStatus();

        return rootView;
    }

    private void queryUserProfileData() {
        FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
            @Override
            public void onReceive(UserProfile userProfile) {
                if (null != userProfile) {
                    if (null != userProfile.getName()) {
                        mNameEditText.setText(userProfile.getName());
                    }
                    if (null != userProfile.getEmail()) {
                        mEmailEditText.setText(userProfile.getEmail());
                        mEmailEditText.setEnabled(false);
                    }
                    if (null != userProfile.getChildBirthdate()) {
                        mBirthdateOfChildEditText.setText(userProfile.getChildBirthdate());
                    }
                }
                updateSaveButtonStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "User profile data is not available", databaseError.toException());
            }
        });
    }

    private void setupSavePopup() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areAllInputsValid()) {
                    return;
                }
                createSaveConfirmationDialog().show();
            }
        });
    }

    private AlertDialog.Builder createSaveConfirmationDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(),
                    android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(R.string.alert_title)
                .setMessage(R.string.alert_message)
                .setPositiveButton(R.string.alert_positiv,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final UserProfile userProfile =
                                        new UserProfile(mNameEditText.getText().toString(),
                                                mEmailEditText.getText().toString(),
                                                mBirthdateOfChildEditText.getText().toString());
                                FirebaseUtils.saveUserProfile(userProfile);

                                Snackbar snackbar = Snackbar.make(mCoordinatorLayout,
                                        getString(R.string.message_save_successfull),
                                        Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        })
                .setNegativeButton(R.string.alert_negativ, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(0);
        return builder;
    }

    private void setupNameField() {
        mNameEditText.addTextChangedListener(new MyTextWatcher(mNameEditText));
    }

    private void setupBirthdateCalendarPopup() {
        mBirthdateOfChildEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentBirthdate = mBirthdateOfChildEditText.getText().toString();
                final LocalDate calendarStartDate
                        = DateTimeUtils.parseLocalDateOrDefault(currentBirthdate, new LocalDate());
                final int monthFirstIndexCorrection = -1;
                new DatePickerDialog(getActivity(), getDateChangeListener(),
                        calendarStartDate.getYear(),
                        calendarStartDate.getMonthOfYear() + monthFirstIndexCorrection,
                        calendarStartDate.getDayOfMonth()).show();
            }
        });
        mBirthdateOfChildEditText.addTextChangedListener(new MyTextWatcher(mBirthdateOfChildEditText));
    }

    @NonNull
    private DatePickerDialog.OnDateSetListener getDateChangeListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final int monthFirstIndexCorrection = 1;
                final String childBirthdate = new LocalDate(year,
                        monthOfYear + monthFirstIndexCorrection, dayOfMonth).toString();
                mBirthdateOfChildEditText.setText(childBirthdate);
            }
        };
    }

    private void validateName() {
        if (!isAcceptableName()) {
            mInputLayoutName.setError(getString(R.string.error_message_name));
        } else {
            mInputLayoutName.setErrorEnabled(false);
        }
    }

    private void validateBirthdate() {
        if (!isAcceptableBirthdateOfChild()) {
            mInputLayoutBirthdateOfChild.setError(getString(R.string.error_message_birthdate));
            registerForContextMenu(mBirthdateOfChildEditText);
        } else {
            mInputLayoutBirthdateOfChild.setErrorEnabled(false);
        }
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_name:
                    validateName();
                    break;
                case R.id.et_birthdate_of_child:
                    validateBirthdate();
                    break;
            }
            updateSaveButtonStatus();
        }
    }

    public boolean areAllInputsValid() {
        return isAcceptableName() && isAcceptableBirthdateOfChild();
    }

    private boolean isAcceptableBirthdateOfChild() {
        return !mBirthdateOfChildEditText.getText().toString().trim().isEmpty();
    }

    private boolean isAcceptableName() {
        return !mNameEditText.getText().toString().trim().isEmpty();
    }

    private void updateSaveButtonStatus() {
        mSaveButton.setEnabled(areAllInputsValid());
    }
}
