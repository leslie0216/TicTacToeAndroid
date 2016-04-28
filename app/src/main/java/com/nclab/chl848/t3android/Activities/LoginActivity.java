package com.nclab.chl848.t3android.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nclab.chl848.t3android.GameObject.Player;
import com.nclab.chl848.t3android.Managers.GameManager;
import com.nclab.chl848.t3android.Message;
import com.nclab.chl848.t3android.Parameters;
import com.nclab.chl848.t3android.R;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton btnStart=(ImageButton)this.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = getUserName();
                if (!user.isEmpty() && user.length() >= Parameters.MIN_USERNAME_LENGTH && user.length() <= Parameters.MAX_USERNAME_LENGTH) {
                    Random rnd = new Random();
                    Player localPlayer = new Player();
                    localPlayer.playerName = user;
                    localPlayer.playerId = rnd.nextInt(10000);
                    Log.d("T3", "LoginActivity: Local player name = " + localPlayer.playerName + ", id = " + localPlayer.playerId);
                    GameManager.getInstance().setLocalPlayer(localPlayer);

                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, ServerClientActivity.class);
                    LoginActivity.this.startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    String msg = "The length of user name must be between "+ Integer.valueOf(Parameters.MIN_USERNAME_LENGTH) + " and " +  Integer.valueOf(Parameters.MAX_USERNAME_LENGTH) + " letters";
                    new AlertDialog.Builder(LoginActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(msg).setPositiveButton(getResources().getString(R.string.ok), null).show();
                }
            }
        });
    }

    public String getUserName() {
        TextView user = (TextView)this.findViewById(R.id.userName);
        return  user.getText().toString();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exit() {
        new AlertDialog.Builder(LoginActivity.this).setTitle("Warning").setMessage("Do you want to quit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        }).show();
    }

}
