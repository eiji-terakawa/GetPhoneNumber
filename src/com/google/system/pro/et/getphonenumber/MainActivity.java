package com.google.system.pro.et.getphonenumber;

import com.google.system.pro.et.getphonenumber.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener ,GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    private final static String TAG = "MainActivity";

	TextView phone_name;
	TextView phone_number;

	TextView[] phone_name_    = new TextView[4];
    TextView[] phone_number_  = new TextView[4];
	TextView[] phone_setting_ = new TextView[4];
	TextView[] phone_call_    = new TextView[4];
		
	String str_phone_name;
	String str_phone_number;

	String[] str_phone_name_ = new String[4];
	String[] str_phone_number_ = new String[4];

	String[] str_phone_name_key_ = new String[4];
	String[] str_phone_number_key_ = new String[4];
	
	String[] str_phone_name_key_def_ = {"この端末","自宅","会社","ぽんき"};
	String[] str_phone_number_key_def_ = {"01234567890","01234567890","01234567890","01234567890"};

	static	SharedPreferences sharedPref;				//プリファレンス

    int[] idList_name;
    int[] idList_number;
    int[] idList_setting;
    int[] idList_call;
	
	//ジェスチャー検出
    private GestureDetector mGestureDetector;	

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCrete");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// res/values/array.xmlから配列データの取得
        Resources res = getResources();
        String[] itemList_name    = res.getStringArray(R.array.name);
        String[] itemList_number  = res.getStringArray(R.array.number);
        String[] itemList_setting = res.getStringArray(R.array.setting);
        String[] itemList_call    = res.getStringArray(R.array.call);

        //配列データからIDの取得
        for (int i = 1; i <= itemList_name.length; i++) {
    		Log.v(TAG, "onCrete3:"+i);

    		phone_name_[i]    = (TextView)findViewById(res.getIdentifier(itemList_name[i-1],    "id", getPackageName()));
    		phone_number_[i]  = (TextView)findViewById(res.getIdentifier(itemList_number[i-1],  "id", getPackageName()));
    		phone_setting_[i] = (TextView)findViewById(res.getIdentifier(itemList_setting[i-1], "id", getPackageName()));
    		phone_call_[i]    = (TextView)findViewById(res.getIdentifier(itemList_call[i-1],    "id", getPackageName()));
//			phone_setting_[i].setOnClickListener(this);
			phone_call_[i].setOnClickListener(this);
			phone_number_[i].setOnClickListener(this);
        }      
		
		phone_name   = (TextView)findViewById(R.id.textView_phone_name);
		phone_number = (TextView)findViewById(R.id.textView_phone_number);
		
        for (int i = 1; i <= itemList_name.length; i++) {
        	str_phone_name_key_[i]   = "phone_name"+(i);
			str_phone_number_key_[i] = "phone_number"+(i);
        }

        
		// プリファレンスから設定値を取得
    	sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

//        Editor editor = sharedPref.edit();
//        // "TIMER_LEFT" というキーで時間を登録
//        editor.putString( "phone_name","この端末" );
//        editor.putString( "phone_name","この端末" );
//        // 書き込みの確定（実際にファイルに書き込む）
//        editor.commit();
    	
    	
    	str_phone_name = sharedPref.getString("phone_name","この端末");
        for (int i = 1; i <= itemList_name.length; i++) {
        	str_phone_name_[i]   = sharedPref.getString(str_phone_name_key_[i],str_phone_name_key_def_[i]);
        	str_phone_number_[i] = sharedPref.getString(str_phone_number_key_[i],str_phone_number_key_def_[i]);
        }

		// この端末の電話番号を取得
		// android.permission.READ_PHONE_STATE パーミッションが必要
		phone_name.setText(str_phone_name);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String line1Number = telephonyManager.getLine1Number();
		String Number = getFormattedPhoneNumber(line1Number);
		phone_number.setText(Number);

		//家の電話番号
        for (int i = 1; i <= itemList_name.length; i++) {
			phone_name_[i].setText(str_phone_name_[i]);
			Number = getFormattedPhoneNumber(str_phone_number_[i]);
			phone_number_[i].setText(Number);
			//プリファレンスの値と違ってたら未登録としてグレーアウト
//			if(str_phone_number_[i].equals(str_phone_number_key_def_[i])){
//				phone_number_[i].setTextColor(Color.GRAY);
//				phone_call_[i].setTextColor(Color.GRAY);
//			}else
			{
				phone_number_[i].setTextColor(Color.WHITE);			
				phone_call_[i].setTextColor(Color.WHITE);
			}
		}		
        TextView textview = phone_number_[1];
        mGestureDetector = new GestureDetector(textview.getContext(), this); 
        textview.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				// GestureDetectorにイベントを委譲する
				boolean result = mGestureDetector.onTouchEvent(event);
				return result;
		    }
		});

    }

public void onClick(View v) {
	Log.v(TAG, "onClick");
		
//    for (int i = 1; i < phone_setting_.length; i++) {
//		if(v == phone_setting_[i]) {
//			Log.v(TAG, "phone_setting");
//			phoneSetting(i);
//		}
//	}
    for (int i = 1; i < phone_call_.length; i++) {
		if(v == phone_call_[i]) {
			Log.v(TAG, "phone_setting");
			phoneSetting(i);
		}
    }
	for (int i = 1; i < phone_number_.length; i++) {
		if(v == phone_number_[i]) {
			Log.v(TAG, "phone_number"+i);
			//電話をかける
			phoneCall(str_phone_number_[i]);
		}
	}
}

@Override
//画面を押した時
public boolean onDown(MotionEvent e) {
    Log.v(TAG,"画面押し検出！");
	return true;
}
@Override
//1タップで画面から指が離れた
public boolean onSingleTapUp(MotionEvent e) {
    Log.v(TAG,"シングルタップアップ検出！");
	return false;
}
@Override
//2連続でタップした
public boolean onDoubleTap(MotionEvent e) {
    Log.v(TAG,"ダブルタップ検出！");
	return false;
}
@Override
//シングルタップと認識された時
public boolean onSingleTapConfirmed(MotionEvent e) {
    Log.v(TAG,"シングルタップ検出！");
	return false;
}
@Override
//ダブルタップ時の処理
public boolean onDoubleTapEvent(MotionEvent e) {
    Log.v(TAG,"ダブルタップ検出！");
	return false;
}
@Override
//画面を長押しした
public void onShowPress(MotionEvent e) {
    Log.v(TAG,"短押し検出！");
}
@Override
//同じ箇所を長押しした時の処理
public void onLongPress(MotionEvent e) {
    Log.v(TAG,"長押し検出！");
}
@Override
//画面を押したまま指を動かした
public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    Log.v(TAG,"スクロール検出！");
	return false;
}
@Override
//フリックした
public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
    Log.v(TAG,"フリック検出！");
	return true;
}


/**
 * 電話番号を設定する
 * @param num
 * @return none
 */
private void phoneSetting(final int num){
	Log.v(TAG, "phoneSetting");
    //テキスト入力を受け付けるビューを作成します。
//    final EditText editView = new EditText(MainActivity.this);
//    new AlertDialog.Builder(MainActivity.this)
//        .setIcon(android.R.drawable.ic_dialog_info)
//        .setTitle(str_phone_name_[num]+"の電話番号を入力してください。")
//        //setViewにてビューを設定します。
//        .setView(editView)
//        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                //入力した文字をトースト出力する
//                Toast.makeText(MainActivity.this, 
//                        editView.getText().toString(), 
//                        Toast.LENGTH_LONG).show();
//                		str_phone_number_[num]=editView.getText().toString();
//                		phone_number_[num].setText(str_phone_number_[num]);
//    					phone_number_[num].setTextColor(Color.WHITE);			
//    					phone_call_[num].setTextColor(Color.WHITE);
//                        Editor editor = sharedPref.edit();
//                        // "TIMER_LEFT" というキーで時間を登録
//                        editor.putString( str_phone_number_key_[num], str_phone_number_[num] );
//                        // 書き込みの確定（実際にファイルに書き込む）
//                        editor.commit();
//            }
//        })
//        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//            }
//        })
//        .show();

    	Intent pickIntent = new Intent(Intent.ACTION_PICK,
    		ContactsContract.Contacts.CONTENT_URI);
    		startActivityForResult(pickIntent, 0);


}

protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.v(TAG,"onActivityResult１");
    if(data != null){
        Log.v(TAG,"onActivityResult２");
        Uri uri = data.getData();
        if(uri != null){
            Log.v(TAG,"onActivityResult３"+uri);
            String[] projection = new String[]{Phone.DISPLAY_NAME,Phone.NUMBER};
            
            Log.v(TAG,"onActivityResult４");
            Cursor cur = getContentResolver().query(uri,projection,null,null,null);
            Log.v(TAG,"onActivityResult５");
            if(cur != null){
                Log.v(TAG,"onActivityResult６");
                cur.moveToFirst();
                String displayName = cur.getString(0);
                String number = cur.getString(1);
                Log.v(TAG,"displayName="+displayName);
                Log.v(TAG,"number="+number);
            }
        }
    }
}
	
/**
 * 指定された電話番号に電話をかける
 * @param number
 * @return none
 */
	private void phoneCall(String number){
		if(!number.equals(str_phone_number_key_def_[1])){
			Intent intent = new Intent(
					Intent.ACTION_CALL,
					Uri.parse("tel:"+number));	
			startActivity(intent);
		}	
	}
	
/**
 * 数字が羅列された電話番号をハイフン付きの電話番号に変換する
 * @param incomingNumber
 * @return ハイフン付き電話番号
 */
	private String getFormattedPhoneNumber(String incomingNumber) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			PhoneNumber pn = phoneUtil.parse(incomingNumber, "JP");
			incomingNumber = phoneUtil.format(pn, PhoneNumberFormat.NATIONAL);
			Log.v(TAG,"Complete");
		} catch (NumberParseException e) {
			Log.v(TAG,"Fail");
		}
		return incomingNumber;
	}
}