package com.nclab.chl848.t3android.Managers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.nclab.chl848.t3android.GameObject.Player;
import com.nclab.chl848.t3android.Message;
import com.nclab.chl848.t3android.Network.BLEHandler;
import com.nclab.chl848.t3android.Parameters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Game logic
 */
public class GameManager {
    //region CONSTANTS
    public static final String GAMEMANAGER_REMOTE_INFO_UP_TO_DATE = "t3.chl848.nclab.com.GAMEMANAGER_REMOTE_INFO_UP_TO_DATE";
    public static final String GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME = "t3.chl848.nclab.com.GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME";
    //endregion

    //region singleton
    private static GameManager ourInstance = new GameManager();
    public static GameManager getInstance() {
        return ourInstance;
    }
    //endregion

    //region variables
    private Player localPlayer;
    private Player remotePlayer;
    private boolean isHost;
    private int activePlayerId;

    private Activity currentActivity;

    private int numBoardSpots;
    private int numFilledBoardSpots;

    private static final List<List<Integer>> winnerCommbo = new ArrayList<>(Arrays.asList(Arrays.asList(0,1,2),
                                                                                            Arrays.asList(0,3,6),
                                                                                            Arrays.asList(0,4,8),
                                                                                            Arrays.asList(1,4,7),
                                                                                            Arrays.asList(2,4,6),
                                                                                            Arrays.asList(2,5,8),
                                                                                            Arrays.asList(3,4,5),
                                                                                            Arrays.asList(6,7,8)));
    //endregion

    //region getters/setters
    public Player getLocalPlayer() {
        return localPlayer;
    }

    public void setLocalPlayer(Player player) {
        localPlayer = player;
    }

    public Player getRemotePlayer() {
        return remotePlayer;
    }

    public void setRemotePlayer(Player player) {
        remotePlayer = player;
    }

    public int getActivePlayerId() {
        return activePlayerId;
    }

    public boolean getIsHost() {
        return isHost;
    }

    public void setIsHost(boolean host) {
        isHost = host;
    }

    public void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }
    //endregion

    //region message functions
    private void broadcastStatus(String action) {
        Intent i = new Intent(action);
        currentActivity.sendBroadcast(i);
    }

    private void broadcastStatus(String action, String msg) {
        Intent i = new Intent(action);
        i.putExtra(BLEHandler.BLE_EXTRA_DATA, msg);
        currentActivity.sendBroadcast(i);
    }

    public void sendPlayerInfo() {
        Message.T3PlayerDataMessage.Builder mb = Message.T3PlayerDataMessage.newBuilder();
        mb.setPlayerName(localPlayer.playerName);
        mb.setPlayerId(localPlayer.playerId);

        byte[] msg = packMessageWithType(Parameters.MSG_PLAYER_DATA, mb.build());
        sendMessage(msg);
    }

    public void goToGame() {
        if (isHost) {
            byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_GO_TO_GAME);
            sendMessage(msg);
        }
    }

    private void sendMessage(byte[] message) {
        if (isHost) {
            BLEHandler.getInstance().sendDataToAllPeripherals(message);
        } else {
            BLEHandler.getInstance().sendDataToCentral(message);
        }
    }

    public byte[] packMessageWithType(char msgType) {
        return String.valueOf(msgType).getBytes();
    }

    public byte[] packMessageWithType(char msgType, com.google.protobuf.GeneratedMessage msg) {
        byte[] target = packMessageWithType(msgType);
        byte[] message = msg.toByteArray();

        byte[] result = new byte[target.length + message.length];

        System.arraycopy(target, 0, result, 0, target.length);
        System.arraycopy(message, 0, result, target.length, message.length);

        return result;
    }

    public void processMessage(byte[] message) {
        byte target = message[0];
        byte[] data = new byte[message.length - 1];
        System.arraycopy(message, 1, data, 0, message.length-1);

        switch ((char)target) {
            case Parameters.MSG_PLAYER_DATA :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_PLAYER_DATA");
                try {
                    Message.T3PlayerDataMessage player = Message.T3PlayerDataMessage.parseFrom(data);
                    if (remotePlayer == null) {
                        remotePlayer = new Player();
                    }

                    if (isHost) {
                        remotePlayer.imageName = "@drawable/opiece";
                    } else {
                        remotePlayer.imageName = "@drawable/xpiece";
                    }

                    remotePlayer.playerName = player.getPlayerName();
                    remotePlayer.playerId = player.getPlayerId();

                    if (!isHost) {
                        broadcastStatus(BLEHandler.BLE_CONNECTION_PERIPHL_FOUND_CENTRAL_ACTION, remotePlayer.playerName);
                    } else {
                        broadcastStatus(GAMEMANAGER_REMOTE_INFO_UP_TO_DATE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }
            case Parameters.MSG_SERVER_CLIENT_GO_TO_GAME :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_GO_TO_GAME");

                if (!isHost) {
                    broadcastStatus(GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME);
                }
                break;
            }
            case Parameters.MSG_CLIENT_SERVER_SCENE_LOADED :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_CLIENT_SERVER_SCENE_LOADED");

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_START_NEW_GAME :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_START_NEW_GAME");

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_PLAYER_MOVE :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_PLAYER_MOVE");

                break;
            }
            case Parameters.MSG_CLIENT_SERVER_MOVE_ACTION :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_CLIENT_SERVER_MOVE_ACTION");

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_GAME_FINISHED :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_GAME_FINISHED");

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_GAME_DRAW :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_GAME_DRAW");

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_SWITCH_PLAYER :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_SWITCH_PLAYER");

                break;
            }
        }
    }
    //endregion

}
