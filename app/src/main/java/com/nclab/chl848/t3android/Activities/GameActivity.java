package com.nclab.chl848.t3android.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.nclab.chl848.t3android.Managers.GameManager;
import com.nclab.chl848.t3android.Network.BLEHandler;
import com.nclab.chl848.t3android.Parameters;
import com.nclab.chl848.t3android.R;

public class GameActivity extends Activity {

    private TextView lbTurnMsg;
    private final IntentFilter m_intentFilter = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ImageButton btnQuit = (ImageButton)this.findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuitWindow();
            }
        });

        setTileEnabled(false);
        initTileEvent();

        initIntentFilter();

        lbTurnMsg = (TextView)findViewById(R.id.lbTurnMsg);

        if (!GameManager.getInstance().getIsHost()) {
            GameManager.getInstance().sendClientSceneLoadedEvent();
            lbTurnMsg.setText(getResources().getString(R.string.waiting));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(m_broadcastReceiver, m_intentFilter);

        BLEHandler.getInstance().setCurrentActivity(GameActivity.this);
        GameManager.getInstance().setCurrentActivity(GameActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(m_broadcastReceiver);
        BLEHandler.getInstance().setCurrentActivity(null);
        GameManager.getInstance().setCurrentActivity(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showQuitWindow();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void initIntentFilter() {
        m_intentFilter.addAction(BLEHandler.BLE_CONNECTION_UPDATE_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_GATT_DISCONNECTED_ACTION);
        m_intentFilter.addAction(BLEHandler.BLE_RECEIVED_DATA_ACTION);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_READY_TO_NEW_GAME);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_START_NEW_GAME);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_PLAYER_MOVE);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_GAME_FINISHED);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_GAME_DRAW);
        m_intentFilter.addAction(GameManager.GAMEMANAGER_SWITCH_PLAYER);
    }

    private void setTileEnabled(boolean enabled) {
        findViewById(R.id.tile0).setEnabled(enabled);
        findViewById(R.id.tile1).setEnabled(enabled);
        findViewById(R.id.tile2).setEnabled(enabled);
        findViewById(R.id.tile3).setEnabled(enabled);
        findViewById(R.id.tile4).setEnabled(enabled);
        findViewById(R.id.tile5).setEnabled(enabled);
        findViewById(R.id.tile6).setEnabled(enabled);
        findViewById(R.id.tile7).setEnabled(enabled);
        findViewById(R.id.tile8).setEnabled(enabled);
    }

    private void resetTiles() {
        ((ImageButton)findViewById(R.id.tile0)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile1)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile2)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile3)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile4)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile5)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile6)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile7)).setImageResource(R.drawable.blankpiece);
        ((ImageButton)findViewById(R.id.tile8)).setImageResource(R.drawable.blankpiece);
    }

    private void setTileImage(int tileNum, int resId) {
        if (tileNum < 0 || tileNum > 8) {
            Log.d(Parameters.TAG, "setTileImage: invalid tileNum");
            return;
        }

        ((ImageButton)findViewById(getTileId(tileNum))).setImageResource(resId);
    }

    private int getTileId(int tileNum) {
        switch (tileNum) {
            case 0:
                return R.id.tile0;
            case 1:
                return R.id.tile1;
            case 2:
                return R.id.tile2;
            case 3:
                return R.id.tile3;
            case 4:
                return R.id.tile4;
            case 5:
                return R.id.tile5;
            case 6:
                return R.id.tile6;
            case 7:
                return R.id.tile7;
            case 8:
                return R.id.tile8;
        }

        Log.d(Parameters.TAG, "getTileId: Invalid tileNum");
        return R.id.tile0; // should not be here!!!
    }

    private int getTileNum(int tileId) {
        switch (tileId) {
            case R.id.tile0:
                return 0;
            case R.id.tile1:
                return 1;
            case R.id.tile2:
                return 2;
            case R.id.tile3:
                return 3;
            case R.id.tile4:
                return 4;
            case R.id.tile5:
                return 5;
            case R.id.tile6:
                return 6;
            case R.id.tile7:
                return 7;
            case R.id.tile8:
                return 8;
        }

        Log.d(Parameters.TAG, "getTileNum: Invalid tileId");
        return 0; // should not be here!!!
    }

    private void initTileEvent() {
        for (int i=0; i<9; ++i) {
            final int id = getTileId(i);
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GameManager.getInstance().getActivePlayerId() == GameManager.getInstance().getLocalPlayer().playerId) {
                        GameManager.getInstance().playerMove(GameManager.getInstance().getLocalPlayer().playerId, getTileNum(id));
                    }
                }
            });
        }
    }

    private void showQuitWindow() {
        new AlertDialog.Builder(GameActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(getResources().getString(R.string.quitconfirm)).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backToServerClientActivity();
            }
        }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        }).show();
    }
    
    private void showLoseConnectionWindow() {
        new AlertDialog.Builder(GameActivity.this).setTitle(getResources().getString(R.string.title)).setMessage(getResources().getString(R.string.gattdisconnect)).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backToServerClientActivity();
            }
        }).show();
    }

    private void showFirstPlayerChooseWindow(String title) {
        if (GameManager.getInstance().getIsHost()) {
            new AlertDialog.Builder(GameActivity.this).setTitle(title).setMessage(getResources().getString(R.string.chooseplayer)).setPositiveButton(GameManager.getInstance().getRemotePlayer().playerName, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GameManager.getInstance().startNewGame(GameManager.getInstance().getRemotePlayer().playerName);
                }
            }).setNegativeButton(GameManager.getInstance().getLocalPlayer().playerName, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GameManager.getInstance().startNewGame(GameManager.getInstance().getLocalPlayer().playerName);
                }
            }).show();
        }
    }

    private void showNewGameWindow(String title) {
        if (GameManager.getInstance().getIsHost()) {
            new AlertDialog.Builder(GameActivity.this).setTitle(title).setMessage(getResources().getString(R.string.startnewgame)).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showFirstPlayerChooseWindow(getResources().getString(R.string.title));
                }
            }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
            }).show();
        }
    }

    private void switchPlayer() {
        if (GameManager.getInstance().getActivePlayerId() == GameManager.getInstance().getLocalPlayer().playerId) {
            setTileEnabled(true);
            lbTurnMsg.setText(getResources().getString(R.string.yourTurn));
        } else {
            setTileEnabled(false);
            lbTurnMsg.setText(getResources().getString(R.string.waiting));
        }
    }

    private void backToServerClientActivity() {
        BLEHandler.getInstance().disconnect();

        Intent intent = new Intent();
        intent.setClass(GameActivity.this, ServerClientActivity.class);
        GameActivity.this.startActivity(intent);
        GameActivity.this.finish();
    }

    private BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BLEHandler.BLE_CONNECTION_UPDATE_ACTION:
                case BLEHandler.BLE_GATT_DISCONNECTED_ACTION: {
                    if (BLEHandler.getInstance().getConnectionCount() == 0) {
                        showLoseConnectionWindow();
                    }
                    break;
                }
                case BLEHandler.BLE_RECEIVED_DATA_ACTION: {
                    Bundle bundle = intent.getExtras();
                    byte[] data = bundle.getByteArray(BLEHandler.BLE_EXTRA_DATA);
                    GameManager.getInstance().processMessage(data);
                    break;
                }
                case GameManager.GAMEMANAGER_READY_TO_NEW_GAME :{
                    showFirstPlayerChooseWindow(getResources().getString(R.string.title));
                    break;
                }
                case GameManager.GAMEMANAGER_START_NEW_GAME :{
                    resetTiles();

                    switchPlayer();
                    break;
                }
                case GameManager.GAMEMANAGER_PLAYER_MOVE :{
                    Bundle bundle = intent.getExtras();
                    int tileNum = bundle.getInt(GameManager.GAMEMANAGER_TILE_NUM);
                    int playerId = bundle.getInt(GameManager.GAMEMANAGER_PLAYER_ID);
                    int resId = playerId == GameManager.getInstance().getLocalPlayer().playerId ?
                            GameManager.getInstance().getLocalPlayer().imageId :
                            GameManager.getInstance().getRemotePlayer().imageId;
                    setTileImage(tileNum, resId);
                    break;
                }
                case GameManager.GAMEMANAGER_GAME_FINISHED :{
                    setTileEnabled(false);
                    Bundle bundle = intent.getExtras();
                    int winnerId = bundle.getInt(GameManager.GAMEMANAGER_PLAYER_ID);
                    if (winnerId == GameManager.getInstance().getLocalPlayer().playerId) {
                        lbTurnMsg.setText(getResources().getString(R.string.win));
                    } else {
                        lbTurnMsg.setText(getResources().getString(R.string.lose));
                    }
                    showNewGameWindow(lbTurnMsg.getText().toString());
                    break;
                }
                case GameManager.GAMEMANAGER_GAME_DRAW :{
                    setTileEnabled(false);
                    lbTurnMsg.setText(getResources().getString(R.string.draw));
                    showNewGameWindow(lbTurnMsg.getText().toString());
                    break;
                }
                case GameManager.GAMEMANAGER_SWITCH_PLAYER :{
                    switchPlayer();
                    break;
                }
            }
        }
    };
}
