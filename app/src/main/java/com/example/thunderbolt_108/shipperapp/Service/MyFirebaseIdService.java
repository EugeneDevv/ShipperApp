package com.example.thunderbolt_108.shipperapp.Service;

import com.example.thunderbolt_108.shipperapp.Common.Common;
import com.example.thunderbolt_108.shipperapp.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken=FirebaseInstanceId.getInstance().getToken();
        if (Common.currentShipper !=null)
            updateToServer(refreshedToken);

    }

    private void updateToServer(String refreshedToken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(refreshedToken,true);
        tokens.child(Common.currentShipper.getPhone()).setValue(token);
    }
}
