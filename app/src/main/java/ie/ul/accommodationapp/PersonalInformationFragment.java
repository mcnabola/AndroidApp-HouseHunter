package ie.ul.accommodationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class PersonalInformationFragment extends Fragment {
    private Toolbar mToolbar;
    private String username;
    private FirebaseAuth mAuth;
    private EditText usernameField;
    private TextView emailField, dateJoinedField, lastLoginField;
    private Button saveButton;
    private FirebaseFirestore db;
    private SearchView searchView;
    private ProfileViewModel profileViewModel;
    private SharedPreferences sharedPreferences;
    private DatabaseReference userRef;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_personal_information, container, false);
        updateUI();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usernameField = view.findViewById(R.id.username_field);
        username = mAuth.getCurrentUser().getDisplayName();
        usernameField.setText(username);
        emailField = view.findViewById(R.id.email_field);
        dateJoinedField = view.findViewById(R.id.date_joined_text);
        lastLoginField = view.findViewById(R.id.last_login_text);
        saveButton = view.findViewById(R.id.save_button);
        emailField.setText(mAuth.getCurrentUser().getEmail());
        dateJoinedField.setText(profileViewModel.getJoined());
        lastLoginField.setText(profileViewModel.getLastLogin());

        //initialise the Firebase RTDB connection
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameField.getText().toString().equals(username)) {
                    Toast.makeText(getContext(), "No changes made.", Toast.LENGTH_SHORT).show();
                } else if (usernameField.getText().toString() == null || usernameField.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a username.", Toast.LENGTH_SHORT).show();
                } else {
                    String usernameText = usernameField.getText().toString();
                    updateUsernameRTDB(mAuth.getCurrentUser().getUid(), usernameText);
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(usernameText)
                            .build();
                    mAuth.getCurrentUser().updateProfile(profileChangeRequest);
                    User user = new User(mAuth.getCurrentUser().getUid(), usernameText);
                    db.collection("Users").document(mAuth.getCurrentUser().getUid()).set(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Updated User Details.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                    } else {
                                        Toast.makeText(getContext(), "Error updating details.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        return view;
    }

    //when a user changes their username we have to update this on the RT DB as well as FireStore
    public void updateUsernameRTDB(String uid, String name){

        userRef.child(uid).child("name").setValue(name)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(getActivity(), "User Name updated RTDB", Toast.LENGTH_SHORT).show();

                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(getActivity(), "Error updating user in RTDB: " + error, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void updateUI() {
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("nightModeEnabled", false);
        mToolbar = getActivity().findViewById(R.id.main_toolbar);
        mToolbar.setTitle("Personal Information");
        searchView = getActivity().findViewById(R.id.search_view);
        searchView.setVisibility(View.GONE);
        if (isNightMode) {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }


}
