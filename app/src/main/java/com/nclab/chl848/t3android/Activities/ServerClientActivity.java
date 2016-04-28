package com.nclab.chl848.t3android.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.nclab.chl848.t3android.Managers.GameManager;
import com.nclab.chl848.t3android.R;

public class ServerClientActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_client);

        Button btnServer = (Button)this.findViewById(R.id.btnServer);
        btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameManager.getInstance().setIsHost(true);
                GameManager.getInstance().getLocalPlayer().imageId = R.drawable.xpiece;
                gotoLobby();
            }
        });

        Button btnClient = (Button)this.findViewById(R.id.btnClient);
        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            btnClient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameManager.getInstance().setIsHost(false);
                    GameManager.getInstance().getLocalPlayer().imageId = R.drawable.opiece;
                    gotoLobby();
                }
            });
        } else {
            btnClient.setEnabled(false);
            btnClient.setVisibility(View.INVISIBLE);
        }

        ImageButton btnBack = (ImageButton)this.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLogin();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            gotoLogin();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void gotoLobby() {

        Intent intent = new Intent();
        intent.setClass(ServerClientActivity.this, LobbyActivity.class);

        ServerClientActivity.this.startActivity(intent);
        ServerClientActivity.this.finish();
    }

    private void gotoLogin() {

        Intent intent = new Intent();
        intent.setClass(ServerClientActivity.this, LoginActivity.class);

        ServerClientActivity.this.startActivity(intent);
        ServerClientActivity.this.finish();
    }
}
