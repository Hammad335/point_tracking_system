package com.codewithhamad.muetbustracker.helper;

import com.codewithhamad.muetbustracker.models.User;

public interface FirebaseDatabaseCallBack {
    void currentUserCallBack(User currentUser);
}
