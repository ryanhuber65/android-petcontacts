package com.cornez.petcontacts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class updateActivity extends Activity {
    //PET UPDATE DATA ENTRY SCREEN
    ImageView updatePhotoId;
    EditText updatePetName;
    EditText updatePetDetails;
    EditText updatePhoneNumber;
    Button updateContactBTN;

    //VARIABLES TO STORE THE UPDATED DATA
    Uri photo;
    int current_id;
    int position;
    String currentName;
    String currentDetail;
    String currentPhone;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_item);

        //REFERENCE INPUT UI COMPONENTS FROM THE LAYOUT
        updateContactBTN = (Button) findViewById(R.id.updateBTN);
        updatePetName = (EditText) findViewById(R.id.updateName);
        updatePetDetails = (EditText) findViewById(R.id.updateDetail);
        updatePhoneNumber = (EditText)
                findViewById(R.id.updatePhoneNumber);
        updatePhotoId = (ImageView) findViewById(R.id.updatePhoto);

        //ENABLE THE UPDATE BUTTON
        updateContactBTN.setEnabled(true);

        //SET THE ONCLICK LISTENER FOR THE UPDATE PHOTO ID IMAGE VIEW
        updatePhotoId.setOnClickListener(getPhotoFromGallery);

        //CALL THE GET CURRENT CONTACT METHOD
        getCurrentContact();

    }
    //******* ACTIVATE AN INTENT TO CHOOSE A PHOTO FROM THE PHOTO GALLERY
    private final View.OnClickListener getPhotoFromGallery =
            new View.OnClickListener() {

                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(
                            Intent.createChooser(intent,
                                    "Select Contact Image"), 3);
                }
            };

    //GET THE CURRENT CONTACT THAT WAS PASSED OVER IN AN INTENT FROM THE FIRST ACTIVITY
    private  void getCurrentContact(){
        //COLLECT THE INTENT BEING PASSED FROM THE MAIN ACTIVITY
        Intent i = new Intent();
        i = getIntent();

        //EXTRACT THE BUNDLE FROM THE INTENT
        Bundle b = i.getExtras();
        //STORE THE DATA FROM THE BUNDLE INTO VARIABLES
        photo = Uri.parse(i.getStringExtra("Photo"));
        current_id = b.getInt("ID");
        currentName = b.getString("Name");
        currentDetail = b.getString("Details");
        currentPhone = b.getString("Phone");
        position = b.getInt("Position");
        //SET THE INPUTS WITH PRESET DATA FROM THE CONTACT BEING EDITED
        updatePetName.setText(currentName);
        updatePetDetails.setText(currentDetail);
        updatePhoneNumber.setText(currentPhone);
        updatePhotoId.setImageURI(photo);

        //ENABLE THE UPDATE BUTTON
        updateContactBTN.setEnabled(true);


    }
    //CLICK EVENT FOR THE UPDATE BUTTON
    public void onClick(View view) {
        //CREATE AN INTENT TO SEND BACK TO THE MAIN ACTIVITY
        Intent updateContact = new Intent(this, MainActivity.class);

        //STORE THE UPDATED DATA IN THE INTENT
        updateContact.putExtra("ID", current_id);
        updateContact.putExtra("Name", updatePetName.getText().toString());
        updateContact.putExtra("Details", updatePetDetails.getText().toString());
        updateContact.putExtra("Phone", updatePhoneNumber.getText().toString());
        updateContact.putExtra("Photo", photo.toString());
        updateContact.putExtra("Position", position);

        //SET THE RESULT CODE TO 2
        setResult(2, updateContact);

        //FINISH THE UPDATE ACTIVITY
        finish();
    }
    // INTENT RETURNS A PHOTO SELECTED FROM THE PHOTO GALLERY
    public void onActivityResult(int reqCode,
                                 int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 3) {
                photo = data.getData();
                updatePhotoId.setImageURI(data.getData());
            }
        }
    }
}
