package com.clinton.adrreport.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.clinton.adrreport.R;
import com.clinton.adrreport.activities.MainActivity;
import com.clinton.adrreport.dbflow.PatientReport;
import com.clinton.adrreport.services.MyIntentService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;


public class DetailFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_IMAGE_CAPTURE_1 = 1;
    private static final int REQUEST_IMAGE_CAPTURE_2 = 2;
    private static final int REQUEST_IMAGE_CAPTURE_3 = 3;
    private static final int REQUEST_IMAGE_CAPTURE_4 = 4;
    public static boolean DETAIL_ACTIVITY_ACTIVE = false;

    //UI References
    @BindView(R.id.headerView)
    TextView mHeaderView;
    @BindView(R.id.warningTextView)
    TextView mWarningTextView;

    @BindView(R.id.image1Frame)
    FrameLayout mImage1FrameView;
    @BindView(R.id.image2Frame)
    FrameLayout mImage2FrameView;
    @BindView(R.id.image3Frame)
    FrameLayout mImage3FrameView;
    @BindView(R.id.image4Frame)
    FrameLayout mImage4FrameView;

    @BindView(R.id.image1)
    ImageView mImage1View;
    @BindView(R.id.image2)
    ImageView mImage2View;
    @BindView(R.id.image3)
    ImageView mImage3View;
    @BindView(R.id.image4)
    ImageView mImage4View;
    @BindView(R.id.submitButton)
    Button mSubmitButton;

    //End of UI References

    private OnDetailsFragmentInter mListener;
    private PatientReport mReport;

    public DetailFragment() {}

    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mReport = null;
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mReport = new PatientReport();
            mReport.setPatientName(getArguments().getString(ARG_PARAM1));
            mReport.setPatientId(getArguments().getString(ARG_PARAM2));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mHeaderView.setText(String.format(getActivity().getResources().getString(R.string.details_header), mReport.getPatientName()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetailsFragmentInter) {
            mListener = (OnDetailsFragmentInter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        ActionBar actionBar = ((MainActivity) context).getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @OnClick(R.id.image1Button)
    void takeForImage1() {
        dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_1);
    }

    @OnClick(R.id.image2Button)
    void takeForImage2() {
        dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_2);
    }

    @OnClick(R.id.image3Button)
    void takeForImage3() {
        dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_3);
    }

    @OnClick(R.id.image4Button)
    void takeForImage4() {
        dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_4);
    }

    @OnClick(R.id.submitButton)
    void attemptReportSubmit(){
        MyIntentService.createService(getActivity().getApplicationContext(), mReport);
        DETAIL_ACTIVITY_ACTIVE = false;
        mListener.onFormSubmitted(true);
    }


    private void enableButton(){
        if (!mSubmitButton.isEnabled()){
            mSubmitButton.setEnabled(true);
            mSubmitButton.setBackgroundColor(getActivity().getResources().getColor(R.color.accent));
            mWarningTextView.setVisibility(View.INVISIBLE);
            DETAIL_ACTIVITY_ACTIVE = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE_1:
                    Log.i("DETAIL","activity result");
                    setPic(mImage1View, mReport.getImageFile1());
                    scaleDownImage(mReport.getImageFile1());
                    enableButton();
                    break;
                case REQUEST_IMAGE_CAPTURE_2:
                    setPic(mImage2View, mReport.getImageFile2());
                    scaleDownImage(mReport.getImageFile2());
                    enableButton();
                    break;
                case REQUEST_IMAGE_CAPTURE_3:
                    setPic(mImage3View, mReport.getImageFile3());
                    scaleDownImage(mReport.getImageFile3());
                    enableButton();
                    break;
                case REQUEST_IMAGE_CAPTURE_4:
                    setPic(mImage4View, mReport.getImageFile4());
                    scaleDownImage(mReport.getImageFile4());
                    enableButton();
                    break;
            }


        }
        else {
            Log.i("RESUKTCODE", String.valueOf(resultCode));
        }
    }

    /*@Override
    public void onPause() {
        Log.i("Activity", "Deleting taken pictures.");
        if (DETAIL_ACTIVITY_ACTIVE) {
            if(mReport.getImageFile1() != null)
                deleteOldPicture(mReport.getImageFile1());
            if(mReport.getImageFile2() != null)
                deleteOldPicture(mReport.getImageFile2());
            if(mReport.getImageFile3() != null)
                deleteOldPicture(mReport.getImageFile3());
            if(mReport.getImageFile4() != null)
                deleteOldPicture(mReport.getImageFile4());
        }

        super.onPause();
    }
*/
    private void dispatchTakePictureIntent(int code) {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getActivity().getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile(code);
            }catch (IOException e){
                Log.e("DEBUG_TAG","create file", e);
                return;
            }
            if (imageFile != null){
                Uri photoURI = null;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(imageFile);
                }
                else {
                    photoURI = FileProvider.getUriForFile(getActivity(), "com.clinton.adrreport.fileprovider", imageFile);
                }

                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, code);
            }

        }
    }

    private boolean scaleDownImage(String path) {
        Bitmap bIn = BitmapFactory.decodeFile(path);
        Bitmap bOut = Bitmap.createScaledBitmap(bIn, 1000, 1000, false);
        // File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File resizedFile = new File(path);

        OutputStream fOut=null;
        try {
            fOut = new BufferedOutputStream(new FileOutputStream(resizedFile));
            bOut.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            bIn.recycle();
            bOut.recycle();

        } catch (Exception e) { // TODO
            System.err.println("Scaling image" + e);
            return  false;
        }
        System.out.println("Scaling image success");
        return true;
    }

    private File createImageFile(int index) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "adr_report_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );


        String oldPath = null;

        switch (index) {
            case REQUEST_IMAGE_CAPTURE_1:
                oldPath = mReport.getImageFile1();
                mReport.setImageFile1(image.getAbsolutePath());
                break;
            case REQUEST_IMAGE_CAPTURE_2:
                oldPath = mReport.getImageFile2();
                mReport.setImageFile2(image.getAbsolutePath());
                break;
            case REQUEST_IMAGE_CAPTURE_3:
                oldPath = mReport.getImageFile3();
                mReport.setImageFile3(image.getAbsolutePath());
                break;
            case REQUEST_IMAGE_CAPTURE_4:
                oldPath = mReport.getImageFile4();
                mReport.setImageFile4(image.getAbsolutePath());
                break;
        }

        if (oldPath != null)
            deleteOldPicture(oldPath);

        return image;
    }

    private boolean deleteOldPicture(String path) {
        File file = new File(path);
        return file.exists() && file.delete();
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File(Environment.getExternalStorageDirectory()
                    + "/adrreports/"
                    + "images");

            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("CameraSample", "failed to create directory");
                    return null;
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private void setPic(ImageView imageView, String imagePath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDetailsFragmentInter {
        void onFormSubmitted(boolean val);
    }


}
