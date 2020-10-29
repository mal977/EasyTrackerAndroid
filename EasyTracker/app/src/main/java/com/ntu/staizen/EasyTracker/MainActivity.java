package com.ntu.staizen.EasyTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.myapp.MyEventBusIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.ntu.staizen.EasyTracker.events.FirebaseAuthenticatedEvent;
import com.ntu.staizen.EasyTracker.events.LocationChangedEvent;
import com.ntu.staizen.EasyTracker.firebase.Authentication;
import com.ntu.staizen.EasyTracker.firebase.FireStore;
import com.ntu.staizen.EasyTracker.model.ContractorInfo;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus eventBus = EventBus.builder().addIndex(new MyEventBusIndex()).build();
//        Authentication authentication = Authentication.getInstance(this);
//        FirebaseAuth auth = authentication.getmAuth();
//
//
//        ContractorInfo contractorInfo = new ContractorInfo();
//        contractorInfo.setName("Malcom");
//        contractorInfo.setPhoneNo("222222222");
//        FireStore.getInstance(this).sendNewContractorToFireStore(auth.getCurrentUser().getUid(), contractorInfo, false);

        NavHostFragment navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
    }

}