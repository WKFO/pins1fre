// Coded by KeyLo99 | TurkHackTeam.org
package com.keylo.pins1fre;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PinS1fre extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "locker_db";
    private static final String TABLE_NAME = "locker_table";
    private static String LOCKHASH = "lockhash";
    private static String LOCKID = "id";
    private Dialog currentDialog;
    private static String passChar = "•";
    private static String IV = "38435241";
    private static byte[] encKey = "OAPQPNKWQKXA99FGDWTMOWFQMYROT20".getBytes();
    private String currentPassCode = "", firstPass = "", secPass = "";
    private Boolean setLock = false, firstPassDone  = false, delLock = false, changeLock = false;
    private TextView txvEnter;
    private EditText passField;
    private Context mContext;
    private Activity mActivity;
    private TableLayout mTableLayout;

    public interface PasscodeEvent {
        void onCorrectPass();
        void onWrongPass();
    }
    private PasscodeEvent delegate = null;

    public PinS1fre(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + LOCKID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LOCKHASH + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public void addHash(String hash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOCKHASH, hash);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    public String getHash(int pos){
        String hash = "";
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            hash = cursor.getString(pos);
        }
        db.close();
        return hash;
    }
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        return rowCount;
    }
    public Boolean resetTables(){
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, null, null);
            db.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean haveLock(){
        try {
            if (getRowCount() == 0) {
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private Boolean savePassLock(String passLock){
        try{
            addHash(protect(passLock));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private Boolean checkPassLock(String passLock){
        try{
            if(getHash(1).equals(protect(passLock))){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private Boolean delPassLock(){
        try{
            if(resetTables()){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void setLock(Activity activity){
        delPassLock();
        setLock = true;
        showLock(activity, null);
    }
    public void showLock(Activity activity, PasscodeEvent delegate){
        if(delegate!=null)
            this.delegate = delegate;
        mActivity = activity;
        mContext = activity.getApplicationContext();
        LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Dialog lockDialog = new Dialog(activity, android.R.style.Theme_NoTitleBar_Fullscreen);
        currentDialog = lockDialog;
        lockDialog.getWindow().requestFeature(1);
        lockDialog.setContentView(inflater.inflate(R.layout.layout_setlock, null));
        lockDialog.setCancelable(false);
        lockDialog.setCanceledOnTouchOutside(false);
        (lockDialog.findViewById(R.id.imgIco)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, mActivity.getResources().getString(R.string.pins1fre_msg), Toast.LENGTH_LONG).show();
            }
        });
        TableLayout numericTab = lockDialog.findViewById(R.id.numericTab);
        mTableLayout = numericTab;
        passField = lockDialog.findViewById(R.id.edtxPassCode);
        txvEnter = lockDialog.findViewById(R.id.txvEnter);
        if(setLock) {
            passField.addTextChangedListener(new edtxPassCode_TextChanged());
            setLock = false;
        }else{
            passField.addTextChangedListener(new edtxPassCodeLOGIN_TextChanged());
        }
        AddKeys(activity, numericTab);
        lockDialog.show();
    }
    public void delLock(Activity activity){
        showLock(activity, null);
        delLock = true;
    }
    public void changeLock(Activity activity){
        showLock(activity, null);
        changeLock = true;
    }
    private class btnKey_Clicked implements Button.OnClickListener{
        @Override
        public void onClick(View view) {
            KeyOut(view.getTag().toString());
        }
    }
    private void KeyOut(String key){
        try {
            int tlength = passField.getText().toString().replace(" ", "").length();
            if (!key.contains("DEL")) {
                currentPassCode += key;
                passField.setText("");
                for (int i = 0; i < tlength; i++) {
                    passField.setText(passField.getText().toString() + passChar + " ");
                }
                passField.setText(passField.getText().toString() + key);
            } else {
                passField.setText("");
                for (int i = 0; i < tlength - 1; i++) {
                    passField.setText(passField.getText().toString() + passChar + " ");
                }
                currentPassCode = currentPassCode.replace(currentPassCode.substring(currentPassCode.length() - 1, currentPassCode.length()), "");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private class edtxPassCode_TextChanged implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().replace(" ", "").length() == 4 && !firstPassDone){
                mTableLayout.setEnabled(false);
                mTableLayout.setClickable(false);
                firstPass = currentPassCode;
                firstPassDone = true;
                txvEnter.setText(mActivity.getResources().getString(R.string.pass_enter_again_msg));
                currentPassCode = "";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        passField.setText("");
                        mTableLayout.setClickable(true);
                        mTableLayout.setEnabled(true);
                    }
                }, 70);
            }else if(editable.toString().replace(" ", "").length() == 4 && firstPassDone){
                mTableLayout.setEnabled(false);
                mTableLayout.setClickable(false);
                secPass = currentPassCode;
                firstPassDone = false;
                if(firstPass.equals(secPass) && savePassLock(firstPass)){
                    txvEnter.setText(mActivity.getResources().getString(R.string.pass_changed_msg));
                    Toast.makeText(mContext, mActivity.getResources().getString(R.string.pass_changed_msg), Toast.LENGTH_LONG).show();
                    currentDialog.dismiss();
                }else{
                    txvEnter.setText(mActivity.getResources().getString(R.string.pass_enter_msg));
                    Toast.makeText(mContext, mActivity.getResources().getString(R.string.pass_false_msg), Toast.LENGTH_SHORT).show();
                }
                currentPassCode = "";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        passField.setText("");
                        mTableLayout.setClickable(true);
                        mTableLayout.setEnabled(true);
                    }
                }, 70);
            }
        }
    }
    private class edtxPassCodeLOGIN_TextChanged implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.toString().replace(" ", "").length() == 4) {
                mTableLayout.setEnabled(false);
                mTableLayout.setClickable(false);
                if(checkPassLock(currentPassCode)){
                    if(delLock){
                        delPassLock();
                        Toast.makeText(mContext, mActivity.getResources().getString(R.string.pass_delete_msg), Toast.LENGTH_SHORT).show();
                        currentDialog.dismiss();
                        delLock = false;
                    }else if(changeLock){
                        delPassLock();
                        currentDialog.cancel();
                        changeLock = false;
                        setLock(mActivity);
                    }else{
                        currentDialog.dismiss();
                        if(delegate!=null)
                            delegate.onCorrectPass();
                    }
                }else{
                    Animation shake = AnimationUtils.loadAnimation(mActivity, R.anim.shake);
                    txvEnter.startAnimation(shake);
                    vibrate();
                    if(delegate!=null)
                        delegate.onWrongPass();
                }
                currentPassCode = "";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        passField.setText("");
                        mTableLayout.setEnabled(true);
                        mTableLayout.setClickable(true);
                    }
                }, 70);
            }
        }
    }
    private ArrayList<String> createKeys(){
        return new ArrayList<String>()
        {{
            add("1~ ");
            add("2~ABC");
            add("3~DEF");
            add("4~GHI");
            add("5~JKL");
            add("6~MNO");
            add("7~TUV");
            add("8~PQRS");
            add("9~WXYZ");
            add(" ~ ");
            add("0~+");
            add("DEL~DEL");
        }};
    }
    private void AddKeys(Activity activity, TableLayout tableLayout){
        ArrayList<String> keys = createKeys();
        int keyval = 0;
        for(int x = 0; x<4; x++){
            TableRow tableRow = new TableRow(activity);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            tableRow.setLayoutParams(lp);
            for(int y = 0; y<3; y++){
                Button button = new Button(activity);
                TableRow.LayoutParams btnParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                button.setText(Html.fromHtml("<b><strong><big>"+keys.get(keyval).split("~")[0]+"</big></strong></b><br><small><font color=\"gray\">"+
                        keys.get(keyval).split("~")[1]+"</font></small>"));
                button.setTag(String.valueOf(keys.get(keyval).split("~")[0]));
                button.setTextSize(24);
                button.setAllCaps(false);
                button.setTextColor(Color.parseColor("#FFFFFF"));
                btnParams.setMargins(10, 10, 10, 10);
                button.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.gradient, null));
                button.setLayoutParams(btnParams);
                button.setOnClickListener(new btnKey_Clicked());
                if(keys.get(keyval).split("~")[0].contains("DEL")){
                    ImageView imageView = new ImageView(activity);
                    TableRow.LayoutParams imgParams = new TableRow.LayoutParams(48, 48);
                    imgParams.setMargins(30, 30, 30, 30);
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.delete, null));
                    tableRow.addView(imageView);
                    imageView.setLayoutParams(imgParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setTag(button.getTag());
                    imageView.setOnClickListener(new btnKey_Clicked());
                }else {
                    tableRow.addView(button);
                }
                keyval++;
            }
            tableLayout.addView(tableRow, x);
        }
    }
    private void vibrate(){
        try {
            if (mActivity.getApplicationContext().checkCallingOrSelfPermission("android.permission.VIBRATE")
                    == PackageManager.PERMISSION_GRANTED) {
                Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(150);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String protect(String str){
        String finStr = "";
        try {
            /*BLOWFISH*/
            SecretKeySpec keySpec = new SecretKeySpec(encKey, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(IV.getBytes()));
            byte[] blowed = cipher.doFinal(str.getBytes());
            /*RC4*/
            cipher = Cipher.getInstance("RC4/ECB/NoPadding");
            Key sk = new SecretKeySpec(encKey, "RC4");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            byte[] rc4ed = cipher.doFinal(blowed);
            /*MD5*/
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(rc4ed);
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            finStr = hexString.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return finStr;
    }
}
