package com.ntu.staizen.EasyTracker.ui.jobList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ntu.staizen.EasyTracker.MainActivity;
import com.ntu.staizen.EasyTracker.R;
import com.ntu.staizen.EasyTracker.SharedPreferenceHelper;
import com.ntu.staizen.EasyTracker.Utilities;
import com.ntu.staizen.EasyTracker.firebase.Authentication;
import com.ntu.staizen.EasyTracker.firebase.FireStore;
import com.ntu.staizen.EasyTracker.manager.EasyTrackerManager;
import com.ntu.staizen.EasyTracker.model.ContractorInfo;
import com.ntu.staizen.EasyTracker.model.JobData;
import com.ntu.staizen.EasyTracker.model.LocationData;
import com.ntu.staizen.EasyTracker.ui.newJobDetails.JobDetailState;
import com.ntu.staizen.EasyTracker.ui.newJobDetails.JobDetailsViewModel;
import com.ntu.staizen.EasyTracker.ui.settings.SettingsFragment;

import java.util.ArrayList;

/**
 * Created by Malcom
 */
public class JobListFragment extends Fragment {
    private static String TAG = JobListFragment.class.getSimpleName();

    private JobListAdapter jobListAdapter;
    private RecyclerView jobListRecyclerView;
    private ArrayList<JobData> jobDataArrayList = new ArrayList<>();

    protected JobListViewModel jobListViewModel;
    protected JobDetailsViewModel jobDetailsViewModel;

    private boolean locationPermissionGranted = false;  //ToDo: Optional Add flow for if user denies location request; maybe dont allow user to start job
    protected boolean isJobRunning = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    protected boolean debug = false;

    public JobListFragment(){
    }

    public JobListFragment(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void onStart() {
        super.onStart();
        jobListViewModel.updatePastJobHistory();
        if (!debug) {
            getLocationPermission();
        }
        EasyTrackerManager locationManager = EasyTrackerManager.getInstance(getActivity());
        JobData jobData = locationManager.checkAndResumeTrackingJob();
        jobDetailsViewModel.setJobDetails(jobData);

        if (!Utilities.isLocationEnabled(getContext())) {
            Utilities.showGPSPrompt(getContext());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        jobListViewModel.updatePastJobHistory();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity().getClass() == MainActivity.class) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.hide();
        }

        jobListViewModel = new ViewModelProvider(this).get(JobListViewModel.class);
        jobDetailsViewModel = new ViewModelProvider(getActivity()).get(JobDetailsViewModel.class);
        NavController navController = Navigation.findNavController(getView());
        Authentication authentication = Authentication.getInstance(getContext());
        FireStore fireStore = FireStore.getInstance();

        //If user isnt authenticated, direct to login fragment
        if (authentication.getmAuth().getCurrentUser() == null) {
            navController.navigate(R.id.loginFragment);
        } else {    //Get Contractor Info
            ContractorInfo contractorInfo = new ContractorInfo();
            contractorInfo.setPhoneNo(SharedPreferenceHelper.getPreference(SharedPreferenceHelper.KEY_PHONE_NUMBER, getActivity()));
            contractorInfo.setName(SharedPreferenceHelper.getPreference(SharedPreferenceHelper.KEY_USERNAME, getActivity()));
            fireStore.sendNewContractorToFireStore(authentication.getUID(), contractorInfo, false);

            TextView tvUID = view.findViewById(R.id.tv_uid);
            tvUID.setText("UID: " + authentication.getmAuth().getCurrentUser().getUid());
        }

        jobListRecyclerView = view.findViewById(R.id.rv_job_list);
        jobListAdapter = new JobListAdapter(getContext(), jobDataArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        jobListRecyclerView.setLayoutManager(linearLayoutManager);
        jobListRecyclerView.setAdapter(jobListAdapter);

        Button start_new_job = (Button) view.findViewById(R.id.btn_start_new_job);
        TextView tv_no_jobs = view.findViewById(R.id.tv_no_jobs);
        TextView tv_helloUser = view.findViewById(R.id.tv_title_hello);
        ImageButton ib_settings = view.findViewById(R.id.ib_settings);

        tv_helloUser.setText("Hello " + SharedPreferenceHelper.getPreference(SharedPreferenceHelper.KEY_USERNAME, getContext()) + "!");

        jobListViewModel.getJobDataState().observe(getViewLifecycleOwner(), new Observer<ArrayList<JobData>>() {
            @Override
            public void onChanged(ArrayList<JobData> vmJobDataArrayList) {
                if (vmJobDataArrayList == null | vmJobDataArrayList.isEmpty()) {
                    tv_no_jobs.setVisibility(View.VISIBLE);
                } else {
                    tv_no_jobs.setVisibility(View.GONE);
                    jobDataArrayList = new ArrayList<>(vmJobDataArrayList);
                    jobListAdapter.setNewJobListData(jobDataArrayList);
                    if (!jobDataArrayList.isEmpty()) {
                        jobListRecyclerView.smoothScrollToPosition(jobDataArrayList.size() - 1);
                    }
                }
            }
        });

        jobDetailsViewModel.getJobDetailStateMutableLiveData().observe(getViewLifecycleOwner(), new Observer<JobDetailState>() {
            @Override
            public void onChanged(JobDetailState jobDetailState) {
                if (jobDetailState != null && jobDetailState.getUID() != null) {
                    if (jobDetailState.getEnd() == 0) {
                        start_new_job.setText(getString(R.string.resume_job));
                        isJobRunning = true;
                    }
                } else {
                    start_new_job.setText(getString(R.string.start_new_job));
                    isJobRunning = false;
                }
            }
        });

        start_new_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationPermissionGranted){
                    getLocationPermission();
                    return;
                }
                if (isJobRunning) {
                    // Navigate to JobDetail fragment to show current tracking job
                    navController.navigate(JobListFragmentDirections.actionJobListFragmentToJobDetails(jobDetailsViewModel.getUID()));
                } else {
                    // Navigate to newJobDetail fragment to start new job
                    navController.navigate(JobListFragmentDirections.actionJobListFragmentToNewJobDetails());
                }
            }
        });

        // Settings Button
        ib_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(JobListFragmentDirections.actionJobListFragmentToSettingsFragment());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_list, container, false);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;

        } else {
            showLocationServicesDialog(getContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * This will be called when user denies location services
     * Meant to show dialog stressing how the user must enable location services for app to work
     *
     * @param context
     */
    private void showLocationServicesDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Easy Tracker requires location updates to perform core functionality! Please allow us access to your location services!")
                .setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}