package divyaganesh.parking.FirebaseData;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import divyaganesh.parking.helpers.RecursiveMethods;
import divyaganesh.parking.model.Account;
import divyaganesh.parking.model.Login;
import divyaganesh.parking.model.Parking;

public class FirestoreDB {
    //Calling recursive Methods
    RecursiveMethods fun = new RecursiveMethods();
    private final String TAG = this.getClass().getCanonicalName();
    //Initializing firebase connection
    private final FirebaseFirestore db;
    public FirestoreDB() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Firebase database
     */
    /*
    No change to Parking database
     */
    /**
     * PARKING DB
     */
    /**
     * PARKING VARIABLES
     */
    private final String COLLECTION_PARKING = "Parking Details";
    public MutableLiveData<List<Parking>> parkingLiveData = new MutableLiveData<List<Parking>>();
    List<Parking> parkingList = new ArrayList<>();
    boolean created = false;
    /*
      Starting of Parking Details collection firebase functions
      Create, Update, Delete, Get parking Details
     */
    /**
     * READ
     * @getAllParkingDetails - Get all the documents inside the collection
     * For safe side, clear the parkingList prior to add new records
     * For each record, add it to parkingList
     * Once all documents retrieved successfully, post the MutualLiveData with the latest parkingList for it's listeners
     */
    public List<Parking> getAllParkingDetails() {
        try {
            db.collection(COLLECTION_PARKING)
                    .orderBy("Date")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                fun.logCatE(TAG, "Failed in getAllParkingDetails function & error is - " + error.getLocalizedMessage());
                            }

                            if (value.isEmpty()) {
                                fun.logCatE(TAG, "No documents found in Parking Details collection");
                            } else {
                                parkingList.clear();
                                for (DocumentChange documentChange : value.getDocumentChanges()) {
                                    Parking parking = documentChange.getDocument().toObject(Parking.class);
                                    parking.setId(documentChange.getDocument().getId());
                                    switch (documentChange.getType()) {
                                        case ADDED:
                                            parkingList.add(parking);
                                            break;

                                        case MODIFIED:
                                            break;

                                        case REMOVED:
                                            parkingList.remove(parking);
                                            break;
                                    }
                                    fun.logCatD(TAG, "Found record of Parking Details. Record is - " + parking.toString());
                                }
                            }
                            parkingLiveData.postValue(parkingList);
                        }
                    });
            return parkingList;
        } catch (Exception e) {
            fun.logCatE(TAG, "Got an exception inside getAllParkingDetails function & error is - " + e.getLocalizedMessage());
            return null;
        }
    }
    /**
     * DELETE
     * @param parking - obj of class is passed
     * @deleteParkingDetail - Delete the record from Firebase based upon the documentId which was passed inside the obj
     * Once record is deleted successfully from Firebase, removed from parkingList & let MutualLiveData post the latest parkingList
     */
    public void deleteParkingDetail(Parking parking) {
        try {
            db.collection(COLLECTION_PARKING)
                    .document(parking.getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Deleted record successfully from Firebase");
                            for (Parking park : parkingList) {
                                if (park.getCarNo().contentEquals(parking.getCarNo())) {
                                    parkingList.remove(park);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to delete record from Firebase");
                        }
                    });
            parkingLiveData.postValue(parkingList);
        } catch (Exception e) {
            fun.logCatE(TAG, "Got an exception inside deleteParkingDetail function & error is - " + e.getLocalizedMessage());
        }
    }
    public void deleteParkingLinkedToEmail(String email){
        try{
            for(int i =0 ; i<parkingList.size(); i++){
                fun.logCatD(TAG, "size is - "+parkingList.size());
                fun.logCatD(TAG, "list is - "+parkingList.get(i).toString());
                String docID = parkingList.get(i).getId();
                fun.logCatD(TAG, "Doc ID is - "+docID);
                db.collection(COLLECTION_PARKING)
                        .document(docID)
                        .delete();
            }
        }
        catch (Exception e){
            fun.logCatE(TAG, "Caught exception while deleting all parking details linked to email - "+email);
        }
    }
    /**
     * UPDATE
     * @param parking Logic to update in arrayList - find the car No, remove the record first & replace with the passed model class object.
     *                Later let the MutualLiveData post the fresh arrayList of Parking class
     * @updateParkingDetail - update the parking details based upon document Id passed from model class object
     * @author - Ganesh
     */
    public void updateParkingDetail(Parking parking) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Email", parking.getEmail().toLowerCase());
            data.put("Address", parking.getAddress());
            data.put("BuildingNo", parking.getBuildingNo());
            data.put("CarNo", parking.getCarNo());
            data.put("Date", parking.getDate());
            data.put("HostNo", parking.getHostNo());
            data.put("id", parking.getId());
            data.put("Hours", parking.getHours());
            data.put("Lat", parking.getLat());
            data.put("Long", parking.getLong());

            db.collection(COLLECTION_PARKING)
                    .document(parking.getId())
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Updated the record successfully in Firebase");
//                            for(Parking park : parkingList){
//                                if(park.getCarNo().contentEquals(parking.getCarNo())){
//                                    parkingList.remove(park);
//                                    parkingList.add(park);
//                                }
//                            }
                            for (int i = 0; i < parkingList.size(); i++) {
                                if (parkingList.get(i).getCarNo().contentEquals(parking.getCarNo())) {
                                    parkingList.remove(parkingList.get(i));
                                    parkingList.add(parking);
                                    break;
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to update the record in Firebase");
                        }
                    });
            parkingLiveData.postValue(parkingList);
        } catch (Exception e) {
            fun.logCatE(TAG, "Got an exception inside updateParkingDetail function & error is - " + e.getLocalizedMessage());
        }
    }
    /**
     * CREATE
     * @param parking - obj of parking is passed
     * @addParkingDetail - To add a fresh record of Parking details into Firebase
     * Logic - Once the record added successfully, add the parking obj into parkingList
     * Later let the MutualLiveData post the latest parkingList
     * Logic to implement before calling - Check if the carNo already exist w.r.t the email address, only one record w.r.t carNo for an email
     */
    public void addParkingDetail(Parking parking) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Email", parking.getEmail().toLowerCase());
            data.put("Address", parking.getAddress());
            data.put("BuildingNo", parking.getBuildingNo());
            data.put("CarNo", parking.getCarNo());
            data.put("Date", parking.getDate());
            data.put("HostNo", parking.getHostNo());
            data.put("id", parking.getId());
            data.put("Hours", parking.getHours());
            data.put("Lat", parking.getLat());
            data.put("Long", parking.getLong());

            db.collection(COLLECTION_PARKING)
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            fun.logCatD(TAG, "Added new Parking record successfully into Firebase");
                            parkingList.add(parking);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to add new Parking record into Firebase. Exception is - " + e.getLocalizedMessage());
                        }
                    });
            parkingLiveData.postValue(parkingList);
        } catch (Exception e) {
            fun.logCatE(TAG, "Got an exception inside addParkingDetail function & error is - " + e.getLocalizedMessage());
        }
    }

    /**
     * Firebase database version 2.0
     * Trying to re-base the current firebase to accommodate new changes
     * Implementing in two parts
     * Part 1 @author Ganesh
     * Part 2 @author Divya
     * Part 1 will be focused on Account Details part
     * Part 2 will be focused on Parking Details part
     */
    /*
    Part 1 Start's from here
    CRUD of Account Details
    Will use new COLLECTION NAME so that ios app won't crash
     */
    /*
    Re-base collections & their MutableLiveData along with the List of model class (new)
    Account Part
     */
    /**
     * PROFILE DB
     */
    /**
     * PROFILE VARIABLES
     */
    private final String COLLECTION_PROFILE = "Profile";
    public MutableLiveData<List<Account>> profileDBLiveData = new MutableLiveData<List<Account>>();
    private List<Account> profileList = new ArrayList<>();
    /**
     * CREATE
     * @param createProfileObj - object passed to create the same in firebase
     */
    public void createProfile(Account createProfileObj) {
        try {
            Map<String, Object> newProfile = new HashMap<>();
            newProfile.put("Name", createProfileObj.getName());
            newProfile.put("Email", createProfileObj.getEmail().toLowerCase());
            newProfile.put("Password", createProfileObj.getPassword());
            newProfile.put("ContactNo", createProfileObj.getContactNo());
            newProfile.put("CarNo", createProfileObj.getCarNo());
            newProfile.put("Image", createProfileObj.getImage());
            String name = createProfileObj.getEmail().toLowerCase();
            db.collection(COLLECTION_PROFILE)
                    .document(name)
                    .set(newProfile)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Created Profile Successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatD(TAG, "Failed to create profile");
                        }
                    });

        }
        catch (Exception e) {
            fun.logCatE(TAG, "Caught an exception while creatingProfile. Exception is - " + e.getLocalizedMessage());
        }
    }
    /**
     * READ
     */
    public void getAllProfiles() {
        try {
            db.collection(COLLECTION_PROFILE)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                fun.logCatE(TAG, "Failed with some error");
                                return;
                            }
                            if (value.isEmpty()) {
                                fun.logCatE(TAG, "No documents found in the collection");
                            }
                            else {
                                for (DocumentChange docChange : value.getDocumentChanges()) {
                                    Account account = docChange.getDocument().toObject(Account.class);
                                    account.setId(docChange.getDocument().getId());
                                    switch (docChange.getType()) {
                                        case ADDED:
                                            profileList.add(account);
                                            break;

                                        case MODIFIED:
                                            if(account.getEmail().contentEquals(docChange.getDocument().getId())){
                                                for(int i=0; i<profileList.size();i++){
                                                    if(account.getId().contentEquals(profileList.get(i).getId())){
                                                        profileList.set(i, account);
                                                    }
                                                }
                                            }
                                            break;

                                        case REMOVED:
                                            profileList.remove(account);
                                            break;
                                    }
                                    fun.logCatD(TAG, "All profiles details - " + account.toString());
                                }
                            }
                            profileDBLiveData.postValue(profileList);
                        }
                    });
        }
        catch (Exception e) {
            fun.logCatE(TAG, "Unable to fetch data");
        }
    }
    /**
     * UPDATE
     * @param updateProfileObj - update the passed object in firebase
     */
    public void updateProfile(Account updateProfileObj){
        try{
            Map<String, Object> updateProfile = new HashMap<>();
            updateProfile.put("Name", updateProfileObj.getName());
            updateProfile.put("Email", updateProfileObj.getEmail().toLowerCase());
            updateProfile.put("Password", updateProfileObj.getPassword());
            updateProfile.put("ContactNo", updateProfileObj.getContactNo());
            updateProfile.put("CarNo", updateProfileObj.getCarNo());
            updateProfile.put("Image", updateProfileObj.getImage());
            String documentID = updateProfileObj.getEmail();
            db.collection(COLLECTION_PROFILE)
                    .document(documentID)
                    .update(updateProfile)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Updated document related to - "+documentID+" successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to update document related to - "+documentID);
                        }
                    });
        }
        catch (Exception e){
            fun.logCatE(TAG, "Caught an exception in update document of profile. Exception is - "+e.getLocalizedMessage());
        }
    }
    /**
     * DELETE
     * @param deleteProfileObj - the passed object is removed from firebase
     */
    public void deleteProfile(Account deleteProfileObj){
        try{
            Map<String, Object> deleteProfile = new HashMap<>();
            deleteProfile.put("Name", deleteProfileObj.getName());
            deleteProfile.put("Email", deleteProfileObj.getEmail().toLowerCase());
            deleteProfile.put("Password", deleteProfileObj.getPassword());
            deleteProfile.put("ContactNo", deleteProfileObj.getContactNo());
            deleteProfile.put("CarNo", deleteProfileObj.getCarNo());
            deleteProfile.put("Image", deleteProfileObj.getImage());
            String documentID = deleteProfileObj.getEmail(); //bug fix, earlier getName and changed to getEmail
            db.collection(COLLECTION_PROFILE)
                    .document(documentID)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Deleted profile with email - "+documentID+" successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to delete profile with email - "+documentID+" from Firebase");
                        }
                    });
        }
        catch (Exception e){
            fun.logCatE(TAG, "Caught an exception in delete document of profile. Exception is - "+e.getLocalizedMessage());
        }
    }

    /**
     * Firebase database version 1.0
     */
    /*
    Collections & their MutableLiveDate along with the List of model class
     */
    /**
     * LOGIN DB
     */
    /**
     * LOGIN VARIABLES
     */
    private final String COLLECTION_LOGIN = "Login Details";
    public MutableLiveData<List<Login>> loginLiveData = new MutableLiveData<List<Login>>();
    List<Login> loginList = new ArrayList<>();
    /**
     * CRUD for LOGIN
     */
    public void getExistingUsers() {
        try {
            db.collection(COLLECTION_LOGIN)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                fun.logCatE(TAG, "Failed with some error");
                                return;
                            }

                            if (value.isEmpty()) {
                                fun.logCatE(TAG, "No documents found in the collection");
                            } else {
                                for (DocumentChange docChange : value.getDocumentChanges()) {
                                    Login loginDetails = docChange.getDocument().toObject(Login.class);
                                    loginDetails.setId(docChange.getDocument().getId());
                                    fun.logCatD(TAG, "onEvent: LoginDetails - " + loginDetails);
                                    loginList.add(loginDetails);
                                }
                            }
                        }
                    });
            loginLiveData.postValue(loginList);

        } catch (Exception e) {
            fun.logCatE(TAG, "Unable to fetch data");
        }
    }
    public void createLogin(Account ac) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Email", ac.getEmail().toLowerCase());
            data.put("Password", ac.getPassword());

            db.collection(COLLECTION_LOGIN).add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            fun.logCatD(TAG, "Login Created successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, e.getLocalizedMessage());
                        }
                    });
        } catch (Exception e) {
            fun.logCatE(TAG, e.getLocalizedMessage());
        }
    }
    public void updateLogin(Login login) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Password", login.getPassword());

            db.collection(COLLECTION_LOGIN)
                    .document(login.getId())
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Updated the record successfully in Firebase");
                            for (Login log : loginList) {
                                if (log.getEmail().contentEquals(login.getEmail())) {
                                    loginList.remove(log);
                                    loginList.add(log);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to update the record in Firebase");
                        }
                    });
            loginLiveData.postValue(loginList);

        } catch (Exception e) {
            Log.e(TAG, "updateLogin: " + e.getLocalizedMessage());
        }
    }

    /**
     * ACCOUNT DB
     */
    /**
     * ACCOUNT VARIABLES
     */
    private final String COLLECTION_ACCOUNT = "Account Details";
    public MutableLiveData<List<Account>> accountLive = new MutableLiveData<>();
    public MutableLiveData<Account> accFromDB = new MutableLiveData<>();
    List<Account> accountList = new ArrayList<>();
    /**
     * CRUD for ACCOUNT
     */
    public void searchUser(String email) {
        try {
            db.collection(COLLECTION_ACCOUNT).whereEqualTo("Email", email).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getDocuments().size() != 0) {
                                    Account matchedAccount = task.getResult().getDocuments().get(0).toObject(Account.class);
                                    matchedAccount.setId(task.getResult().getDocuments().get(0).getId());
                                    accFromDB.postValue(matchedAccount);

                                    Log.d(TAG, "onComplete: matchedFriend : " + matchedAccount.toString());

                                } else {
                                    Log.e(TAG, "onComplete: No friend retrieved");
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "searchFriend: " + e.getLocalizedMessage());
        }

    }
    public void addUser(Account ac) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Name", ac.getName());
            data.put("Email", ac.getEmail().toLowerCase());
            data.put("Password", ac.getPassword());
            data.put("ContactNo", ac.getContactNo());
            data.put("CarNo", ac.getCarNo());

            db.collection(COLLECTION_ACCOUNT).add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            fun.logCatD(TAG, "Data added successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, e.getLocalizedMessage());
                        }
                    });

        } catch (Exception e) {
            fun.logCatE(TAG, e.getLocalizedMessage());
        }
    }
    public void updateUser(Account account) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("Name", account.getName());
            data.put("Password", account.getPassword());
            data.put("ContactNo", account.getContactNo());

            db.collection(COLLECTION_ACCOUNT)
                    .document(account.getId())
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fun.logCatD(TAG, "Updated the record successfully in Firebase");
                            for (Account acc : accountList) {
                                if (acc.getCarNo().contentEquals(account.getCarNo())) {
                                    accountList.remove(acc);
                                    accountList.add(acc);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            fun.logCatE(TAG, "Failed to update the record in Firebase");
                        }
                    });
            accountLive.postValue(accountList);
        } catch (Exception e) {
            Log.e(TAG, "updateUser: " + e.getLocalizedMessage());
        }
    }
}