package com.clinton.adrreport.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.clinton.adrreport.R;
import com.clinton.adrreport.activities.MainActivity;
import com.clinton.adrreport.utils.Validators;


public class MainActivityFragment extends Fragment {

    private onStartButtonClick mStartCallback;

    @BindView(R.id.messageView)
    TextView mMessageView;
    @BindView(R.id.patientNameView)
    TextInputEditText mPatientNameView;
    @BindView(R.id.patientIdView)
    TextInputEditText mPatientIdView;
    @BindView(R.id.startButton)
    Button mStartButton;

    public MainActivityFragment() {
    }

    @OnClick(R.id.startButton)
    void attemptStarting(){
        //System.out.println("here");
        mPatientNameView.setError(null);

        // Store values at the time of the login attempt.
        String patientName = mPatientNameView.getText().toString();
        String patientId = mPatientIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!Validators.isNotEmpty(patientName)) {
            mPatientNameView.setError("Please provide name");
            focusView = mPatientNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            hideSoftKeyboard();
            mStartCallback.onButtonClick(patientName, patientId);
        }
    }

    private void hideSoftKeyboard() {
        if(getActivity().getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mMessageView.getWindowToken(), 0);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStartCallback = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       if (context instanceof onStartButtonClick){
           mStartCallback = (onStartButtonClick) context;
       }else throw new RuntimeException(context.toString()
               + " must implement OnFragmentInteractionListener");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public interface onStartButtonClick {
        public void onButtonClick(String patientName, String patientId);
    }
}
