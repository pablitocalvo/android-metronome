/*
 * Copyright: 2008 Akshat Aranya
 *
 *    This file is part of Metronome.
 *
 * Metronome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metronome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Metronome.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.example.android;



import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Metronome extends Activity {
	
	
	boolean mRunning = false;
	Button mStartStopButton;
	SeekBar mSeekBar;
	TickPlayer tp;
	TextView tempoVal;
	TextView mPeriodLabel;
	Button mPeriodButtons[];
	Button mPlus;
	Button mMinus;
	PowerManager.WakeLock mWakeLock;

	
	private static final int DEFAULT_TEMPO = 75;
	private static final int DEFAULT_PERIOD = 4;
	private int mTempo = DEFAULT_TEMPO;
	private int mPeriod = DEFAULT_PERIOD;
	private static final int numPeriods = 8;
	private static final int maxTempo = 200;
	private static final String KEY_TEMPO = "METRONOME_TEMPO";
	private static final String KEY_PERIOD = "METRONOME_PERIOD";
	
	private static final String PREFS = "metronome.prefs";
	
	private void bindPeriodButtons()
	{
		// There must be a better way to do this
		mPeriodButtons = new Button[numPeriods];
		mPeriodButtons[0] = (Button)findViewById(R.id.button1);
		mPeriodButtons[1] = (Button)findViewById(R.id.button2);
		mPeriodButtons[2] = (Button)findViewById(R.id.button3);
		mPeriodButtons[3] = (Button)findViewById(R.id.button4);
		mPeriodButtons[4] = (Button)findViewById(R.id.button5);
		mPeriodButtons[5] = (Button)findViewById(R.id.button6);
		mPeriodButtons[6] = (Button)findViewById(R.id.button7);
		mPeriodButtons[7] = (Button)findViewById(R.id.button8);
		
		for (int i = 0; i < numPeriods; i++) {
			mPeriodButtons[i].setOnClickListener(new Button.OnClickListener()
			{

				@Override
				public void onClick(View v) {
					Button b = (Button)v;
					mPeriod = Integer.parseInt(b.getText().toString());
					restart();
					
					
				}
			}
			);
			
		}
	}
	
	private void restart()
	{
		
		mSeekBar.setProgress(mTempo);
		tempoVal.setText("" + mTempo);
		mPeriodLabel.setText("" + mPeriod);
		mMinus.setClickable(mTempo > 0);
		mPlus.setClickable(mTempo < maxTempo);
		
		if (mRunning) {
			tp.onStop();
			tp.onStart(mPeriod, mTempo);
		}
		
	
	}
	/*
	protected void onPause()
	{
		Log.v("Metronome", "onPause called");
		super.onPause();
		if (mRunning)
			changeState();
		
		
	}*/
	
	protected void onStop() {
		//Log.v("Metronome", "onStop");
		super.onStop();
		/*
		if (mRunning) {
			changeState();
		}
		*/
		SharedPreferences settings = getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(KEY_PERIOD, mPeriod);
		editor.putInt(KEY_TEMPO, mTempo);
		editor.commit();
	}
	


	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//Log.v("Metronome", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MetronomeLock");
        tp = new TickPlayer(this);
        mStartStopButton = (Button) findViewById(R.id.startstop);
       
        mSeekBar = (SeekBar) findViewById(R.id.tempo);
        mSeekBar.setMax(maxTempo + 1);
        tempoVal = (TextView) findViewById(R.id.text);
        mMinus = (Button) findViewById(R.id.minus);
        mPlus = (Button) findViewById(R.id.plus);
        mPeriodLabel = (TextView) findViewById(R.id.period);
        
        SharedPreferences settings = getSharedPreferences(PREFS, 0);
        mPeriod = settings.getInt(KEY_PERIOD, DEFAULT_PERIOD);
        mTempo = settings.getInt(KEY_TEMPO, DEFAULT_TEMPO);
       

        bindPeriodButtons();
        
        mMinus.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTempo > 1) --mTempo;
				restart();
				
			}
        	
        });
        mPlus.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTempo < maxTempo) ++mTempo;
				restart();
				
			}
        	
        });
        
      
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        		{

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromTouch) {
						mTempo = progress;
						tempoVal.setText("" + mTempo);
						// TODO Auto-generated method stub
						
					}


					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
							
							mTempo = seekBar.getProgress();
							restart();
							//tp.onStop();
							//tp.onStart(4, val);
					}


					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
        			
        		}
        		);
        
        mStartStopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            		changeState();
            	
                
            }
        });
        
        restart();
    }
    
    private void changeState() {
     	mRunning = !mRunning;
    	if (mRunning) {
    		mWakeLock.acquire();
    		
    		mStartStopButton.setText(R.string.stop);
    		tp.onStart(mPeriod, mTempo);
    	} else {
    		mWakeLock.release();
    		tp.onStop();
    		
    		mStartStopButton.setText(R.string.start);
    	}
    	
    }
/*
    protected void onPause() {
    	Log.v("Metronome", "onPause");
    	super.onPause();
    }
    protected void onResume() {
    	Log.v("Metronome", "onResume");
    	super.onResume();
    }
    */
    
    protected void onDestroy() {
    	//Log.v("Metronome", "onDestroy");
    	if (mRunning) {
    		changeState();
    	}
    	tp.onDestroy();
    
    	super.onDestroy();
    
    }
}
