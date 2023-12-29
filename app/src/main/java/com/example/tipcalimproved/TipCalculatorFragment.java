package com.example.tipcalimproved;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.prefs.Preferences;

public class TipCalculatorFragment extends Fragment
        implements TextView.OnEditorActionListener, View.OnKeyListener, SeekBar.OnSeekBarChangeListener,
        RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    //1. Create objects for views (wigits) that trigger
    //events or are changed when events are triggered:
    //EditText, Text Views RadioGroup, RadioButton, Spinner, SeekBar Views
    private EditText billAmountEditText;
    private TextView percentTextView;
    private SeekBar percentSeekBar;
    private TextView tipTextView;
    private TextView totalTextView;
    private RadioGroup roundingRadioGroup;
    private RadioButton roundNoneRadioButton;
    private RadioButton roundTipRadioButton;
    private RadioButton roundTotalRadioButton;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private TextView perPersonTextView;
    //define a SharedPreference object to save our values
    private SharedPreferences savedValues;
    //define rounding constants
    private final int ROUND_NONE  = 0; // this will be used in chapter 8 as a preference variable
    private final int ROUND_TIP   = 1;
    private final int ROUND_TOTAL = 2;
    //define instance variables we will need
    private String billAmountString = "";
    private float tipPercent = .15f;
    private int rounding = ROUND_NONE;
    private int split = 1;
    //chapter 8 Step 1 define preference variables
    private SharedPreferences prefs;
    private boolean rememberTipPercent = true;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_main,container,false);

        billAmountEditText = (EditText) view.findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) view.findViewById(R.id.percentTextView);
        percentSeekBar = (SeekBar) view.findViewById(R.id.percentSeekBar);
        tipTextView = (TextView) view.findViewById(R.id.tipTextView);
        totalTextView = (TextView) view.findViewById(R.id.totalTextView);
        roundingRadioGroup = (RadioGroup) view.findViewById(R.id.roundingRadioGroup);
        roundNoneRadioButton = (RadioButton) view.findViewById(R.id.roundNoneRadioButton);
        roundTipRadioButton = (RadioButton) view.findViewById(R.id.roundTipRadioButton);
        roundTotalRadioButton = (RadioButton) view.findViewById(R.id.roundTotalRadioButton);
        splitSpinner = (Spinner) view.findViewById(R.id.splitSpinner);
        perPersonLabel = (TextView) view.findViewById(R.id.perPersonLabel); //we are going to hide this
        perPersonTextView = (TextView) view.findViewById(R.id.perPersonTextView);//we are going to hide this

        //2 set array adapter for the Spinner object
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        splitSpinner.setAdapter(adapter);

        //3 Set the Listeners
        billAmountEditText.setOnEditorActionListener(this);//Edit Text Handler
        billAmountEditText.setOnKeyListener(this);//create stub for setOnKeyListener used by several view
        percentSeekBar.setOnSeekBarChangeListener(this);//create stub for setOnSeekBarChangeListener
        percentSeekBar.setOnKeyListener(this);//create stub for setOnKeyListener uses onKeyListener
        roundingRadioGroup.setOnCheckedChangeListener(this);//used to set stub for radioGroup
        roundingRadioGroup.setOnKeyListener(this);//set stub for radio setOnKeyListener uses onKeyListener
        splitSpinner.setOnItemSelectedListener(this);

        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
    }//End onCreate

    public void calculateAndDisplay() {
        //get bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if (billAmountString.equals("")) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        //get tip percent
        int progress = percentSeekBar.getProgress();
        tipPercent = (float) progress / 100;

        //calculate tip and total
        float tipAmount = 0;
        float totalAmount = 0;
        float tipPercentToDisplay=0;
        if (rounding == ROUND_NONE) {
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
            tipPercentToDisplay = tipPercent;
        } else if (rounding == ROUND_TIP) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
            tipPercentToDisplay = tipAmount / billAmount;
        } else if (rounding == ROUND_TOTAL) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
            tipPercentToDisplay = tipAmount/billAmount;
        }

        //calculate split amount and show/hide split amount and show widgets
        float splitAmount = 0;

        if (split == 1) { // no split hide widgets

            perPersonLabel.setVisibility(View.GONE);
            perPersonTextView.setVisibility(View.GONE);
        } else { //calculate amount and show widgets
            splitAmount = totalAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonTextView.setVisibility(View.VISIBLE);
        }

        //Display results with formatting

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        perPersonTextView.setText(currency.format(splitAmount));

        NumberFormat percent = NumberFormat.getPercentInstance();
        //percentTextView.setText(percent.format(tipPercent));
        percentTextView.setText(percent.format(tipPercentToDisplay));
    }//end calculateAndDisplay

    @Override
    public void onPause() { //need to import shared preferences editor
        //save the instance variables
        super.onPause();//calls the superclass to complete the creation of the activity
        SharedPreferences.Editor editor = prefs.edit();//needed unchanged check listener book used Editor editor = savedValuses
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.putInt("rounding", rounding);
        editor.putInt("split", split);
        editor.commit();
    }//end onPause
    @Override
    public void onResume() {
        super.onResume();

        /*****************************************
         Chapter 8 Step 4 get preference code added we are going  to use
         *****************************************/
        //get preferences uses preference activities and prefs instead of the savedValues and Activity Main
        rememberTipPercent = prefs.getBoolean("pref_remember_percent",true);
        rounding = Integer.parseInt(prefs.getString("pref_rounding", "0"));
        billAmountString = prefs.getString("billAmountString", "");
        if (rememberTipPercent){
            tipPercent = prefs.getFloat("tipPercent", 0.15f);
        } else{
            tipPercent = 0.15f;
        }
        billAmountEditText.setText(billAmountString);
        calculateAndDisplay();

    }//end onResume

    //1****Event Handler for EditText Using Current Class Listiner****
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }
        return false;
    }//end onEditorAction
    //5*****Event Handler for Keyboard and DPad****
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:

                calculateAndDisplay();
                //hide soft keyboard
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        billAmountEditText.getWindowToken(), 0);
                //consume event
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (v.getId() == R.id.percentSeekBar) {
                    calculateAndDisplay();
                }
                break;
        }
        //don't consume event
        return false;
    }// end onKey Listener
    //2*****Event Handlers for the Seekbar*****
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        percentTextView.setText(progress + "%");
    }//end Seekbar onProgressChanged Listener
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //TODO Auto Generated Stub
    }//end Seekbar onStartTracking
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        calculateAndDisplay();
    }//end Seekbar onStopTracking
    //3****Event Handler for RadioGroup*****
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch(checkedId){
            case R.id.roundNoneRadioButton:
                rounding = ROUND_NONE;
                break;
            case R.id.roundTipRadioButton:
                rounding = ROUND_TIP;
                break;
            case R.id.roundTotalRadioButton:
                rounding = ROUND_TOTAL;
                break;
        }
        calculateAndDisplay();
    }//End RadioGroup event Handler
    //4****Event handlers for the Spinner***
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        split = position + 1;
        calculateAndDisplay();
    }//end Spinner onItemSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do Nothing
    }//end Spinner onNothingSelected

/*******************************************************
 Chapter 8 code how to work with menus and preferences *
 *******************************************************/
    /**********************************************************
     Code to display the menu this method called by Android *
     before it needs to display the menu the first time     *
     **********************************************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                //Start the Settings Activity
                startActivity(new Intent(getActivity().getApplicationContext(),SettingsActivity.class));
                return true;
            case R.id.menu_about:
                //Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                //Start the About Activity
                startActivity(new Intent(getActivity().getApplicationContext(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//end switch
    }//end onOptionsItemSelected

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_main, menu);
    }

}//End MainActivity