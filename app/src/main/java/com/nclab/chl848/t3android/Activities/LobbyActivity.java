package com.nclab.chl848.t3android.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nclab.chl848.t3android.Managers.GameManager;
import com.nclab.chl848.t3android.Network.BLEHandler;
import com.nclab.chl848.t3android.R;

public class LobbyActivity extends Activity {

    private TextView lbMsg;
    private TextView txtPlayer1;
    private TextView txtPlayer2;
    private ImageButton btnStartGame;

    private final IntentFilter m_intentFilter = new IntentFilter();

    private BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BLEHandler.BLE_CONNECTION_BT_DISABLE_ACTION: {
                    new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(getResources().getString(R.string.btdisabled)).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backToServerClientActivity();
                        }
                    }).show();
                    break;
                }
                case BLEHandler.BLE_CONNECTION_CENTRAL_FOUND_PERIPH_ACTION: {
                    Bundle bundle = intent.getExtras();
                    if (bundle.containsKey(BLEHandler.BLE_EXTRA_DATA) && bundle.containsKey(BLEHandler.BLE_EXTRA_DATA_ADDRESS)) {
                        String name = bundle.getString(BLEHandler.BLE_EXTRA_DATA);
                        final String addr = bundle.getString(BLEHandler.BLE_EXTRA_DATA_ADDRESS);
                        String msg = " \"" + name + "\" " + getResources().getString(R.string.periphfound);
                        new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(msg).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BLEHandler.getInstance().stopScan();
                                BLEHandler.getInstance().connectToPeripheral(addr);
                            }
                        }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        }).show();
                    }
                    break;
                }
                case BLEHandler.BLE_CONNECTION_PERIPHL_FOUND_CENTRAL_ACTION: {
                    Bundle bundle = intent.getExtras();
                    String name = bundle.getString(BLEHandler.BLE_EXTRA_DATA);
                    String msg = getResources().getString(R.string.centalfoundp1) + " \"" + name + "\" " + getResources().getString(R.string.centalfoundp2);
                    new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(msg).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            remoteInfoUpToDateWithNotification();
                            //send client info to server
                            GameManager.getInstance().sendPlayerInfo();
                        }
                    }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BLEHandler.getInstance().disconnect();
                            if (!BLEHandler.getInstance().getIsInit()) {
                                BLEHandler.getInstance().setup();
                            }
                            BLEHandler.getInstance().setIsAdvertise(true);
                        }
                    }).show();
                    break;
                }
                case BLEHandler.BLE_CONNECTION_AUTO_STOP_SCAN_ACTION: {
                    String msg;
                    if (BLEHandler.getInstance().isCentral())
                    {
                        msg = getResources().getString(R.string.scanfinish);
                    } else {
                        msg = getResources().getString(R.string.advfinish);
                    }
                    new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(msg).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (BLEHandler.getInstance().isCentral()) {
                                BLEHandler.getInstance().startScan();
                            } else {
                                BLEHandler.getInstance().setIsAdvertise(true);
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    }).show();
                    break;
                }
                case BLEHandler.BLE_GATT_CONNECTED_ACTION: {
                    // if host, send server info to client
                    if (GameManager.getInstance().getIsHost()) {
                        GameManager.getInstance().sendPlayerInfo();
                    }
                    break;
                }
                case BLEHandler.BLE_GATT_DISCONNECTED_ACTION: {
                    new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(getResources().getString(R.string.gattdisconnect)).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backToServerClientActivity();
                        }
                    }).show();
                    break;
                }
                case BLEHandler.BLE_CONNECTION_NOT_SUPPORT_ACTION: {
                    new AlertDialog.Builder(LobbyActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(getResources().getString(R.string.notsupport)).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backToServerClientActivity();
                        }
                    }).show();
                    break;
                }
                case BLEHandler.BLE_RECEIVED_DATA_ACTION: {
                    Bundle bundle = intent.getExtras();
                    byte[] data = bundle.getByteArray(BLEHandler.BLE_EXTRA_DATA);
                    GameManager.getInstance().processMessage(data);
                    break;
                }
                case GameManager.GAMEMANAGER_REMOTE_INFO_UP_TO_DATE: {
                    remoteInfoUpToDateWithNotification();
                    break;
                }
                case GameManager.GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME: {
                    goToGame();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        ImageButton btnBack = (ImageButton)this.findViewById(R.id.btnBackToSC);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToServerClientActivity();
            }
        });

        btnStartGame = (ImageButton)this.findViewById(R.id.btnStartGame);


        TextView lbRole = (TextView)this.findViewById(R.id.lbRole);
        lbMsg = (TextView)this.findViewById(R.id.lbMsg);
        txtPlayer1 = (TextView)this.findViewById(R.id.txtPlayer1);
        txtPlayer2 = (TextView)this.findViewById(R.id.txtPlayer2);

        if (GameManager.getInstance().getIsHost()) {
            lbRole.setText(getResources().getString(R.string.server));
            lbMsg.setText(getResources().getString(R.string.waitingplayer));
            txtPlayer1.setTextColor(Color.WHITE);
            txtPlayer1.setText(GameManager.getInstance().getLocalPlayer().playerName);
            txtPlayer2.setTextColor(Color.GRAY);
            txtPlayer2.setText(getResources().getString(R.string.empty));

            btnStartGame.setEnabled(false);
            btnStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameManager.getInstance().goToGame();

                    goToGame();
                }
            });
        } else {
            lbRole.setText(getResources().getString(R.string.client));
            lbMsg.setText(getResources().getString(R.string.searchinggame));
            txtPlayer1.setTextColor(Color.GRAY);
            txtPlayer1.setText(getResources().getString(R.string.empty));
            txtPlayer2.setTextColor(Color.WHITE);
            txtPlayer2.setText(GameManager.getInstance().getLocalPlayer().playerName);

            btnStartGame.setEnabled(false);
            btnStartGame.setVisibility(View.INVISIBLE);
        }

        if (GameManager.getInstance().getIsHost()) {
            m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_CENTRAL_FOUND_PERIPH_ACTION);
            m_intentFilter.addAction(BLEHandler.BLE_GATT_CONNECTED_ACTION);
            m_intentFilter.addAction(GameManager.GAMEMANAGER_REMOTE_INFO_UP_TO_DATE);
        } else {
            m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_PERIPHL_FOUND_CENTRAL_ACTION);
            m_intentFilter.addAction(GameManager.GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME);
        }

        m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_BT_DISABLE_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_AUTO_STOP_SCAN_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_GATT_DISCONNECTED_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_NOT_SUPPORT_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_RECEIVED_DATA_ACTION);


        BLEHandler.getInstance().setIsCentral(GameManager.getInstance().getIsHost());
        BLEHandler.getInstance().setLocalName(GameManager.getInstance().getLocalPlayer().playerName);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(m_broadcastReceiver, m_intentFilter);

        BLEHandler.getInstance().setCurrentActivity(LobbyActivity.this);
        GameManager.getInstance().setCurrentActivity(LobbyActivity.this);

        if (!BLEHandler.getInstance().getIsInit()) {
            BLEHandler.getInstance().setup();
        }

        if (BLEHandler.getInstance().isScanningOrAdvertising())
        {
            if (BLEHandler.getInstance().isCentral()) {
                BLEHandler.getInstance().stopScan();
            } else {
                BLEHandler.getInstance().setIsAdvertise(false);
            }
        }

        if (BLEHandler.getInstance().isCentral()) {
            BLEHandler.getInstance().startScan();
        } else {
            BLEHandler.getInstance().setIsAdvertise(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(m_broadcastReceiver);
        BLEHandler.getInstance().setCurrentActivity(null);

        if (BLEHandler.getInstance().isCentral()) {
            BLEHandler.getInstance().stopScan();
        } else {
            BLEHandler.getInstance().setIsAdvertise(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToServerClientActivity();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void backToServerClientActivity() {
        BLEHandler.getInstance().disconnect();

        Intent intent = new Intent();
        intent.setClass(LobbyActivity.this, ServerClientActivity.class);
        LobbyActivity.this.startActivity(intent);
        LobbyActivity.this.finish();
    }

    private void remoteInfoUpToDateWithNotification() {
        if (BLEHandler.getInstance().isCentral()) {
            BLEHandler.getInstance().stopScan();
            if (BLEHandler.getInstance().getConnectionCount() > 0) {
                btnStartGame.setEnabled(true);
            }
            txtPlayer2.setTextColor(Color.WHITE);
            txtPlayer2.setText(GameManager.getInstance().getRemotePlayer().playerName);
        } else {
            BLEHandler.getInstance().setIsAdvertise(false);
            txtPlayer1.setTextColor(Color.WHITE);
            txtPlayer1.setText(GameManager.getInstance().getRemotePlayer().playerName);
        }

        lbMsg.setText(getResources().getString(R.string.readytogame));
    }

    private void goToGame() {
        Intent intent = new Intent();
        intent.setClass(LobbyActivity.this, GameActivity.class);
        LobbyActivity.this.startActivity(intent);
        LobbyActivity.this.finish();
    }
}
