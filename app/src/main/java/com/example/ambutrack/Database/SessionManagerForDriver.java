package com.example.ambutrack.Database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManagerForDriver {
    SharedPreferences usersSession;
    SharedPreferences.Editor editor;
    Context context;

    public static  final String SESSION_USERSSESSION_DRIVER = "userLoginSessionDriver";
    public static  final String SESSION_REMEMBERME_DRIVER = "rememberMeDriver";

    private static final String IS_REMEMBERME_DRIVER = "IsRememberMeDriver";
    public static final String KEY_SESSIONMAILID_DRIVER = "mailIdDriver";
    public static final String KEY_PASSWORD_DRIVER = "passwordDriver";


    public SessionManagerForDriver(Context _context,String sessionName){
        context = _context;
        usersSession = context.getSharedPreferences(sessionName,Context.MODE_PRIVATE);
        editor = usersSession.edit();
    }

    public void createRememberMeSession(String mailId,String password){
        editor.putBoolean(IS_REMEMBERME_DRIVER,true);
        editor.putString(KEY_SESSIONMAILID_DRIVER,mailId);
        editor.putString(KEY_PASSWORD_DRIVER,password);
        editor.commit();
    }

    public HashMap<String,String> getRememberMeDetailsFromSession(){
        HashMap<String,String> userData = new HashMap<>();
        userData.put(KEY_SESSIONMAILID_DRIVER,usersSession.getString(KEY_SESSIONMAILID_DRIVER,null));
        userData.put(KEY_PASSWORD_DRIVER,usersSession.getString(KEY_PASSWORD_DRIVER,null));

        return userData;
    }

    public boolean checkRememberMe(){
        if(usersSession.getBoolean(IS_REMEMBERME_DRIVER,false)){
            return  true;
        }else {
            return false;
        }
    }
}
