package com.cs6365.keystrokeDynamicsLogin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs6365.model.Authentication;
import com.cs6365.model.Utils;

public class LoginActivity extends Activity {

	private EditText pwd;
	private EditText username;
	private Button buttonLogin;
	private Button buttonClear;
	private Button button0;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button button6;
	private Button button7;
	private Button button8;
	private Button button9;
	private long lastDown;
	private List<Long> pressTime = new ArrayList<Long>();
	private List<Long> timeBetweenPress = new ArrayList<Long>();
	private boolean register=false;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		register=(intent.getStringExtra("new")!=null);
		
		

		name = intent.getStringExtra("name");
		setContentView(R.layout.activity_login);

		buttonLogin = (Button) findViewById(R.id.buttonLogin);
		button0 = (Button) findViewById(R.id.button0);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button6 = (Button) findViewById(R.id.button6);
		button7 = (Button) findViewById(R.id.button7);
		button8 = (Button) findViewById(R.id.button8);
		button9 = (Button) findViewById(R.id.button9);
		buttonClear = (Button) findViewById(R.id.buttonClear);
		
		if(register){
			buttonLogin.setText("register");
		}

		username = (EditText) findViewById(R.id.username);
		username.setEnabled(false);
		username.setText(name);
		pwd = (EditText) findViewById(R.id.password);
		pwd.setEnabled(false);
		
		setListeners(button0,0);
		setListeners(button1,1);
		setListeners(button2,2);
		setListeners(button3,3);
		setListeners(button4,4);
		setListeners(button5,5);
		setListeners(button6,6);
		setListeners(button7,7);
		setListeners(button8,8);
		setListeners(button9,9);
		
		buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pwd.setText("");
				lastDown=0;
				pressTime = new ArrayList<Long>();
				timeBetweenPress = new ArrayList<Long>();

			}
		});
		
		buttonLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StringBuilder str = new StringBuilder();
				Vector<Double> featureVector = new Vector<Double>();
				for (int i =0 ; i<timeBetweenPress.size();i++){
					Log.d("time between each btn", timeBetweenPress.get(i).toString());
					featureVector.add(timeBetweenPress.get(i).doubleValue());
					str.append(timeBetweenPress.get(i)+";");
				}
				
				for (int j =0 ; j<pressTime.size();j++){
					Log.d("pressed time", pressTime.get(j).toString());
					featureVector.add(pressTime.get(j).doubleValue());
					str.append(pressTime.get(j)+";");
				}
				
				if(register){	
					//////////////
					DateFormat dateFormat = new SimpleDateFormat("dd;MM;yyyy;HH:mm:ss",java.util.Locale.getDefault());
					Calendar cal = Calendar.getInstance();
					System.out.println(dateFormat.format(cal.getTime()));
					Utils.writeTo("PIN"+name+dateFormat.format(cal.getTime())+".txt",pwd.getText().toString()+","+str.toString());

					pwd.setText("");
					lastDown=0;
					pressTime = new ArrayList<Long>();
					timeBetweenPress = new ArrayList<Long>();
					//////////////
					//Authentication.initialization(name, featureVector.size(),pwd.getText().toString(), getApplicationContext());
				} else {
					if(Authentication.authenticate(featureVector, name, pwd.getText().toString(), getApplicationContext())){
						Toast.makeText(getApplicationContext(),
								"success!", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(getApplicationContext(),
								"failure", Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
		});
	}

	private void setListeners(Button button, final int i) {
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					long time = System.currentTimeMillis();
					if(lastDown!=0){
						timeBetweenPress.add(time-lastDown);
					}
					lastDown = time;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					pressTime.add(System.currentTimeMillis() - lastDown);
				}
				return false;
			}
		});
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pwd.setText(pwd.getText().toString()+i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
