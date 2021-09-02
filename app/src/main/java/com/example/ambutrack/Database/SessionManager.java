package com.example.ambutrack.Database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences usersSession;
    SharedPreferences.Editor editor;
    Context context;

    public static  final String SESSION_USERSSESSION = "userLoginSession";
    public static  final String SESSION_REMEMBERME = "rememberMe";

    private static final String IS_REMEMBERME = "IsRememberMe";
    public static final String KEY_SESSIONMAILID = "mailId";
    public static final String KEY_PASSWORD = "password";


    public SessionManager(Context _context,String sessionName){
        context = _context;
        usersSession = context.getSharedPreferences(sessionName,Context.MODE_PRIVATE);
        editor = usersSession.edit();
    }

    public void createRememberMeSession(String mailId,String password){
        editor.putBoolean(IS_REMEMBERME,true);
        editor.putString(KEY_SESSIONMAILID,mailId);
        editor.putString(KEY_PASSWORD,password);
        editor.commit();
    }

    public HashMap<String,String> getRememberMeDetailsFromSession(){
        HashMap<String,String> userData = new HashMap<>();
        userData.put(KEY_SESSIONMAILID,usersSession.getString(KEY_SESSIONMAILID,null));
        userData.put(KEY_PASSWORD,usersSession.getString(KEY_PASSWORD,null));

        return userData;
    }

    public boolean checkRememberMe(){
        if(usersSession.getBoolean(IS_REMEMBERME,false)){
            return  true;
        }else {
            return false;
        }
    }
}
