package com.cs6365.keystrokeDynamicsLogin;

import com.cs6365.model.Authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button btnRegPIN;
	private Button btnReg;
	private Button btnLogPIN;
	private Button btnLog;
	private EditText name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnLogPIN = (Button) findViewById(R.id.buttonLogPIN);
		btnRegPIN = (Button) findViewById(R.id.buttonRegPIN);
		btnReg = (Button) findViewById(R.id.buttonReg);
		btnLog = (Button) findViewById(R.id.buttonLog);
		name = (EditText) findViewById(R.id.userName);

		btnLogPIN.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(name.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(),
							"Invalid username", Toast.LENGTH_SHORT)
							.show();
				} else if (!Authentication.userExists(name.getText().toString(),
						getApplicationContext())) {
					Toast.makeText(getApplicationContext(),
							"Username does not exist", Toast.LENGTH_SHORT)
							.show();
				} else {

					Intent intent = new Intent(MainActivity.this,
							LoginActivity.class);
					intent.putExtra("name", name.getText().toString());
					startActivity(intent);
					finish();
				}
			}
		});

		btnRegPIN.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(name.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(),
							"Invalid username", Toast.LENGTH_SHORT)
							.show();
				} else if (Authentication.userExists(name.getText().toString(),
						getApplicationContext())) {
					Toast.makeText(getApplicationContext(),
							"Username already taken", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent intent = new Intent(MainActivity.this,
							LoginActivity.class);
					intent.putExtra("name", name.getText().toString());
					intent.putExtra("new", "register");
					startActivity(intent);
					finish();
				}
			}
		});

		btnLog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(name.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(),
							"Invalid username", Toast.LENGTH_SHORT)
							.show();
				} else if (!Authentication.userExists(name.getText().toString(),
						getApplicationContext())) {
					Toast.makeText(getApplicationContext(),
							"Username does not exist", Toast.LENGTH_SHORT)
							.show();
				} else {

					Intent intent = new Intent(MainActivity.this,
							LoginAlphaActivity.class);
					intent.putExtra("name", name.getText().toString());
					startActivity(intent);
					finish();
				}
			}
		});

		btnReg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(name.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(),
							"Invalid username", Toast.LENGTH_SHORT)
							.show();
				} else if (Authentication.userExists(name.getText().toString(),
						getApplicationContext())) {
					Toast.makeText(getApplicationContext(),
							"Username already taken", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent intent = new Intent(MainActivity.this,
							LoginAlphaActivity.class);
					intent.putExtra("name", name.getText().toString());
					intent.putExtra("new", "register");
					startActivity(intent);
					finish();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
