package com.cornez.petcontacts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    //DATABASE AND ADAPTER OBJECTS
    DBHelper dbHelper;
    ArrayAdapter<Pet> arrayAdapter;

    //SINGLE RECORD INFORMATION IN A LISTVIEW
    List<Pet> PetArrayList = new ArrayList<Pet>();

    //PET CONTACT DATA ENTRY SCREEN
    Button addContactBTN;
    ImageView inputPhotoId;
    EditText inputPetName;
    EditText inputPetDetails;
    EditText inputPhoneNumber;
    Drawable noPetImage;
    Uri defaultImage = Uri.parse("android.resource://com.cornez.petcontacts/drawable/none.png");

    //VARIABLES TO STORE THE CURRENT CONTACT DATA
    int current_id;
    String currentName;
    String currentDetail;
    String currentPhone;
    Uri currentPhotoURI;

    Boolean newEntry = true;

    //PET LISTING SCREEN
    ListView petListView;
    ImageView listViewPhoto;
    TextView listViewName;
    TextView listViewDetails;
    TextView listViewPhone;

    TabHost tabHost;
    int contactIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TASK 1: SET UP THE DATABASE
        dbHelper = new DBHelper(getApplicationContext());

        // TASK 2: REFERENCE INPUT UI COMPONENTS FROM THE LAYOUT
        addContactBTN = (Button) findViewById(R.id.addBTN);
        inputPetName = (EditText) findViewById(R.id.memberName);
        inputPetDetails = (EditText) findViewById(R.id.memberDetail);
        inputPhoneNumber = (EditText) findViewById(R.id.memberPhoneNumber);
        petListView = (ListView) findViewById(R.id.listView);
        inputPhotoId = (ImageView) findViewById(R.id.memberPhoto);
        noPetImage = inputPhotoId.getDrawable();

        //TASK 3: SET UP TABS
        registerForContextMenu(petListView);
        petListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent,
                                                   View view, int position, long id) {
                        contactIndex = position;
                        return false;
                    }
                });

        // TASK 4: CREATE ACTION TABS: ADD PET INFORMATION
        tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("add pet information");
        tabSpec.setContent(R.id.tabInfo);
        tabSpec.setIndicator("add pet information");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("view all pets");
        tabSpec.setContent(R.id.tabList);
        tabSpec.setIndicator("view all pets");
        tabHost.addTab(tabSpec);

        // TASK 5: A PET CAN BE ADDED ONCE USER HAS ENTERED A NAME
        inputPetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                addContactBTN.setEnabled(String.valueOf(
                        inputPetName.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // TASK 6: LISTENER EVENTS FOR PHOTO SELECTION AND BUTTON
        inputPhotoId.setOnClickListener(getPhotoFromGallery);
        addContactBTN.setOnClickListener(recordPetInformation);


        //TASK 7: POPULATE THE DATABASE
        if (dbHelper.getContactsCount() != 0)
            PetArrayList.addAll(dbHelper.getAllContacts());

        populateList();
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
                                    "Select Contact Image"), 1);
                }
            };

    //*********** ADD PET RECORD TO THE DATABASE *******
    private final View.OnClickListener recordPetInformation =
            new View.OnClickListener() {

                public void onClick(View v) {
                    Pet contact = new Pet(
                            dbHelper.getContactsCount(),
                            String.valueOf(inputPetName.getText().toString()),
                            String.valueOf(inputPetDetails.getText().toString()),
                            String.valueOf(inputPhoneNumber.getText().toString()),
                            defaultImage);

                    if (!contactExists(contact)) {
                        dbHelper.createContact(contact);
                        PetArrayList.add(contact);
                        arrayAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),
                                inputPetName.getText().toString()
                                        + " has been added.",
                                Toast.LENGTH_SHORT).show();
                        newEntry = true;
                        onResume();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),
                            String.valueOf(inputPetName.getText())
                                    + "has already been added. Use another name",
                            Toast.LENGTH_LONG).show();
                }
            };

    //**************CONTEXT MENU : DELETE A PET

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.ic_launcher);
        menu.setHeaderTitle("Contact ...");
        menu.add(Menu.NONE, 1, Menu.NONE, "Delete Contact");

        menu.add(Menu.NONE, 2, Menu.NONE, "Update Contact");
    }

    // CONTEXT MENU METHOD FOR CONTEXT ITEM SELECTED
    public boolean onContextItemSelected(MenuItem item) {

        //CREATE AN INTENT TO SEND TO THE UPDATE ACTIVITY
        Intent i = new Intent(this, updateActivity.class);
        switch(item.getItemId()){
            case 1:
                dbHelper.deleteContact(PetArrayList.get(contactIndex));
                PetArrayList.remove(contactIndex);
                arrayAdapter.notifyDataSetChanged();
                return true;
            case 2:
                //CREATE A BUNDLE TO STORE IN THE INTENT
                Bundle content = new Bundle();
                //GET THE PET OBJECT FOR THE CURRENT CONTACT AND STORE
                Pet currentContact = PetArrayList.get(contactIndex);
                //GET THE CURRENT ID AND STORE
                current_id = currentContact.getId();
                //GET THE CURRENT NAME AND STORE
                currentName = currentContact.getName();
                //GET THE CURRENT DETAILS AND STORE
                currentDetail = currentContact.getDetails();
                //GET THE CURRENT PHONE NUMBER AND STORE
                currentPhone = currentContact.getPhone();
                //GET THE CURRENT PHOTO AND STORE
                currentPhotoURI = currentContact.getPhotoURI();

                //STORE THE DATA IN THE BUNDLE
                content.putInt("ID", current_id);
                content.putString("Name", currentName);
                content.putString("Details", currentDetail);
                content.putString("Phone", currentPhone);
                content.putInt("Position", contactIndex);

                //PUT THE BUNDLE IN THE INTENT
                i.putExtras(content);
                //STORE THE PHOTO IN THE INTENT
                i.putExtra("Photo", currentPhotoURI.toString());
                //START THE ACTIVITY FOR A RESULT (RETURN THE UPDATED DATA AFTER)
                startActivityForResult(i, 2);

        }


        return super.onContextItemSelected(item);
    }

    public boolean contactExists(Pet member) {
        String first = member.getPhone();
        int contactCount = PetArrayList.size();

        for (int i = 0; i < contactCount; i++) {
            if (first.compareToIgnoreCase(
                    PetArrayList.get(i).getPhone()) == 0)
                return true;
        }
        return false;
    }

    // INTENT RETURNS A PHOTO SELECTED FROM THE PHOTO GALLERY
    //INTENT ALSO RETURNS UPDATED CONTACT DATA FROM THE UPDATE ACTIVITY
    public void onActivityResult(int reqCode,
                                 int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 1) {
                newEntry = false;
                defaultImage = data.getData();
                inputPhotoId.setImageURI(data.getData());
            }
        }
        //INTENT RETURNS THE UPDATED CONTACT DATA FROM THE UPDATE ACTIVITY IF THE RESULT CODE = 2
        if (reqCode == 2){
            //GET THE DATA FROM THE INTENT AND STORE INTO VARIABLES
            currentPhotoURI = Uri.parse(data.getStringExtra("Photo"));
            current_id = data.getIntExtra("ID", 0);
            currentName = data.getStringExtra("Name");
            currentDetail = data.getStringExtra("Details");
            currentPhone = data.getStringExtra("Phone");
            int index = data.getIntExtra("Position", 0);


            //CREATE A NEW PET OBJECT WITH THE UPDATED DATA
            Pet updateContact = new Pet(current_id, currentName, currentDetail, currentPhone, currentPhotoURI);

            //UPDATE THE CONTACT IN THE PET ARRAY LIST
            PetArrayList.set(index, updateContact);
            //UPDATE THE DATABASE
            dbHelper.updateContact(updateContact);
            //POPULATE THE PET LIST VIEW AGAIN
            populateList();


        }
    }

    private void populateList() {
        arrayAdapter = new ContactListAdapter();
        petListView.setAdapter(arrayAdapter);
    }

    private class ContactListAdapter extends ArrayAdapter<Pet> {
        public ContactListAdapter() {
            super(getApplicationContext(),
                    R.layout.listview_item, PetArrayList);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Pet currentContact = PetArrayList.get(position);

            listViewName = (TextView)
                    view.findViewById(R.id.textViewName);
            listViewDetails = (TextView)
                    view.findViewById(R.id.textViewDetail);
            listViewPhone = (TextView)
                    view.findViewById(R.id.textViewPhone);
            listViewPhoto = (ImageView)
                    view.findViewById(R.id.memberPhoto);

            listViewName.setText(currentContact.getName());
            listViewDetails.setText(currentContact.getDetails());
            listViewPhone.setText(currentContact.getPhone());
            listViewPhoto.setImageURI(currentContact.getPhotoURI());

            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //CLEAR OUT PET INFORMATION IF IT IS A NEW ENTRY
        if (newEntry) {
            inputPetName.setText("");
            inputPetDetails.setText("");
            inputPhoneNumber.setText("");
            inputPhotoId.setImageDrawable(noPetImage);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
