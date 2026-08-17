package com.example.veterinarialemas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static SharedPreferences getSharedPreferences(Context context, String nomPreferencia) {
        return context.getSharedPreferences(nomPreferencia, Context.MODE_PRIVATE);
    }

    public static boolean getValorEsperadoBoolean(Context context, String nomPreferencia, String valueRecuperar) {
        return getSharedPreferences(context,nomPreferencia).getBoolean(valueRecuperar, false);
    }

    public static void setValorBoolean(Context context, String nomPreferencia, boolean newValue, String newStringValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context,nomPreferencia).edit();
        editor.putBoolean(newStringValue , newValue);
        editor.commit();
    }
}
