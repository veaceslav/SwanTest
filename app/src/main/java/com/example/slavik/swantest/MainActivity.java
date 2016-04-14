package com.example.slavik.swantest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import interdroid.swan.SensorInfo;
import interdroid.swan.SwanException;
import interdroid.swan.ExpressionManager;
import interdroid.swan.ValueExpressionListener;
import interdroid.swan.swansong.ExpressionFactory;
import interdroid.swan.swansong.TimestampedValue;
import interdroid.swan.swansong.ValueExpression;
import interdroid.swan.swansong.ExpressionParseException;


public class MainActivity extends AppCompatActivity {

    TextView tv;
    SensorInfo swanSensor;

    int REQUEST_CODE = 1234;
    String SENSOR_NAME = "wear_movement";

    String ACCEL_X = "self@wear_movement:x?accuracy='0'{ANY,0}";
    String ACCEL_Y = "self@wear_movement:y?accuracy='0'{ANY,0}";
    String ACCEL_Z = "self@wear_movement:z?accuracy='0'{ANY,0}";

    Spinner mySpinner;

    //String SENSOR_NAME = "movement";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] array_spinner=new String[8];
        array_spinner[0]="wear_heartrate";
        array_spinner[1]="wear_movement";
        array_spinner[2]="wear_gyroscope";
        array_spinner[3]="wear_gamerotation";
        array_spinner[4]="wear_light";
        array_spinner[5]="wear_linearacceleration";
        array_spinner[6]="wear_step_counter";
        array_spinner[7]="wear_gravity";

        mySpinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, array_spinner);
        mySpinner.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize();
            }
        });
    }

    public void initialize(){
        tv = (TextView)findViewById(R.id.textView1);

        String Text = mySpinner.getSelectedItem().toString();
        try {
            swanSensor = ExpressionManager.getSensor(MainActivity.this, Text);
        } catch (SwanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		/* start activity for configuring sensor */
        startActivityForResult(swanSensor.getConfigurationIntent(), REQUEST_CODE);
    }


    /* Invoked on pressing back key from the sensor configuration activity */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (REQUEST_CODE == requestCode) {
                String myExpression = data.getStringExtra("Expression");
					/*Based on sensor configuration an expression will be created*/
                Log.d("debug", "expression: " + myExpression);

                registerSWANSensor(myExpression);

            }
        }

    }

    /* Register expression to SWAN */
    private void registerSWANSensor(String myExpression){

        try {
            ExpressionManager.registerValueExpression(this, String.valueOf(REQUEST_CODE),
                    (ValueExpression) ExpressionFactory.parse(myExpression),
                    new ValueExpressionListener() {

                        /* Registering a listener to process new values from the registered sensor*/
                        @Override
                        public void onNewValues(String id,
                                                TimestampedValue[] arg1) {
                            if (arg1 != null && arg1.length > 0) {
                                String value = arg1[0].getValue().toString();
                                tv.setText("Value = "+value);

                            } else {
                                tv.setText("Value = null");
                            }

                        }
                    });
        } catch (SwanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /* Unregister expression from SWAN */
    private void unregisterSWANSensor(){

        ExpressionManager.unregisterExpression(this, String.valueOf(REQUEST_CODE));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSWANSensor();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSWANSensor();

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
