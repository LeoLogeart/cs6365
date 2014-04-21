package com.cs6365.keystrokeDynamicsLogin;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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

public class LoginAlphaActivity extends Activity {

	private EditText pwd;
	private EditText username;
	private Button buttonLogin;
	private Button buttonClear;
	private long lastDown;
	private List<Long> pressTime = new ArrayList<Long>();
	private List<Long> timeBetweenPress = new ArrayList<Long>();
	private boolean register = false;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.activity_login_alpha);

		register = (intent.getStringExtra("new") != null);

		name = intent.getStringExtra("name");

		buttonLogin = (Button) findViewById(R.id.buttonLogin);
		buttonClear = (Button) findViewById(R.id.buttonClear);

		if (register) {
			buttonLogin.setText("register");
		}

		username = (EditText) findViewById(R.id.username);
		username.setEnabled(false);
		username.setText(name);
		pwd = (EditText) findViewById(R.id.password);
		pwd.setEnabled(false);

		Button b0 = (Button) findViewById(R.id.btn0);
		if (b0 == null) {
			Log.d("bouton", "AH NAN");
		}
		setListenersInt(b0, 0);
		setListenersInt((Button) findViewById(R.id.btn1), 1);
		setListenersInt((Button) findViewById(R.id.btn2), 2);
		setListenersInt((Button) findViewById(R.id.btn3), 3);
		setListenersInt((Button) findViewById(R.id.btn4), 4);
		setListenersInt((Button) findViewById(R.id.btn5), 5);
		setListenersInt((Button) findViewById(R.id.btn6), 6);
		setListenersInt((Button) findViewById(R.id.btn7), 7);
		setListenersInt((Button) findViewById(R.id.btn8), 8);
		setListenersInt((Button) findViewById(R.id.btn9), 9);

		setListenersStr((Button) findViewById(R.id.btna), "a");
		setListenersStr((Button) findViewById(R.id.btnb), "b");
		setListenersStr((Button) findViewById(R.id.btnc), "c");
		setListenersStr((Button) findViewById(R.id.btnd), "d");
		setListenersStr((Button) findViewById(R.id.btne), "e");
		setListenersStr((Button) findViewById(R.id.btnf), "f");
		setListenersStr((Button) findViewById(R.id.btng), "g");
		setListenersStr((Button) findViewById(R.id.btnh), "h");
		setListenersStr((Button) findViewById(R.id.btni), "i");
		setListenersStr((Button) findViewById(R.id.btnj), "j");
		setListenersStr((Button) findViewById(R.id.btnk), "k");
		setListenersStr((Button) findViewById(R.id.btnl), "l");
		setListenersStr((Button) findViewById(R.id.btnm), "m");
		setListenersStr((Button) findViewById(R.id.btnn), "n");
		setListenersStr((Button) findViewById(R.id.btno), "o");
		setListenersStr((Button) findViewById(R.id.btnp), "p");
		setListenersStr((Button) findViewById(R.id.btnq), "q");
		setListenersStr((Button) findViewById(R.id.btnr), "r");
		setListenersStr((Button) findViewById(R.id.btns), "s");
		setListenersStr((Button) findViewById(R.id.btnt), "t");
		setListenersStr((Button) findViewById(R.id.btnu), "u");
		setListenersStr((Button) findViewById(R.id.btnv), "v");
		setListenersStr((Button) findViewById(R.id.btnw), "w");
		setListenersStr((Button) findViewById(R.id.btnx), "x");
		setListenersStr((Button) findViewById(R.id.btny), "y");
		setListenersStr((Button) findViewById(R.id.btnz), "z");

		buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pwd.setText("");
				lastDown = 0;
				pressTime = new ArrayList<Long>();
				timeBetweenPress = new ArrayList<Long>();

			}
		});

		buttonLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StringBuilder str = new StringBuilder();
				Vector<Double> featureVector = new Vector<Double>();
				for (int i = 0; i < timeBetweenPress.size(); i++) {
					Log.d("time between each btn", timeBetweenPress.get(i)
							.toString());
					featureVector.add(timeBetweenPress.get(i).doubleValue());
					str.append(timeBetweenPress.get(i) + ";");
				}

				for (int j = 0; j < pressTime.size(); j++) {
					Log.d("pressed time", pressTime.get(j).toString());
					featureVector.add(pressTime.get(j).doubleValue());
					str.append(pressTime.get(j) + ";");
				}

				if (register) {
					// ////////////
					/*
					 * DateFormat dateFormat = new SimpleDateFormat(
					 * "dd;MM;yyyy;HH:mm:ss",java.util.Locale.getDefault());
					 * Calendar cal = Calendar.getInstance();
					 * System.out.println(dateFormat.format(cal.getTime()));
					 * Utils.writeTo( name + dateFormat.format(cal.getTime()) +
					 * ".txt",pwd.getText().toString()+","+ str.toString());
					 * 
					 * pwd.setText(""); lastDown = 0; pressTime = new
					 * ArrayList<Long>(); timeBetweenPress = new
					 * ArrayList<Long>();
					 */
					// ////////////
					Authentication.initialization(name, featureVector.size(),
							pwd.getText().toString(), getApplicationContext());
				} else {
					boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
					if (Authentication.authenticate(featureVector, name, pwd
							.getText().toString(), getApplicationContext(),
							portrait, false)) {
						Toast.makeText(getApplicationContext(), "success!",
								Toast.LENGTH_SHORT).show();

						Intent intent = new Intent(LoginAlphaActivity.this,
								MainActivity.class);
						startActivity(intent);
						finishActivity(0);
					} else {
						Toast.makeText(getApplicationContext(), "failure",
								Toast.LENGTH_SHORT).show();
						pwd.setText("");
						lastDown = 0;
						pressTime = new ArrayList<Long>();
						timeBetweenPress = new ArrayList<Long>();
					}
				}
			}
		});
	}

	private void setListenersInt(Button button, final int i) {
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					long time = System.currentTimeMillis();
					if (lastDown != 0) {
						timeBetweenPress.add(time - lastDown);
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
				pwd.setText(pwd.getText().toString() + i);
			}
		});
	}

	private void setListenersStr(Button button, final String s) {
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					long time = System.currentTimeMillis();
					if (lastDown != 0) {
						timeBetweenPress.add(time - lastDown);
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
				pwd.setText(pwd.getText().toString() + s);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_alpha, menu);
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
