package ie.ul.accommodationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BottomNavigationActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    public List<Listing> mData;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mToolbar = findViewById(R.id.main_toolbar);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean nightModeKey = sharedPreferences.getBoolean("nightModeEnabled", false);
        toggleNightMode(nightModeKey);
        setSupportActionBar(mToolbar);
        navController = Navigation.findNavController(this, R.id.bottom_navigation_fragment_container);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        if (mData == null) {
            getAllListings();
        }
    }

    public void toggleNightMode(boolean isNight) {
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public List<Listing> getListings() {
        return mData;
    }

    public void addListing(Listing listing) {
        mData.add(listing);
    }

    public void getAllListings() {
        mData = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Listings");
        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Listing listing = documentSnapshot.toObject(Listing.class);
                                mData.add(listing);
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
