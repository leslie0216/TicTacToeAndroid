package com.nclab.chl848.t3android.Managers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.nclab.chl848.t3android.GameObject.Player;
import com.nclab.chl848.t3android.Message;
import com.nclab.chl848.t3android.Network.BLEHandler;
import com.nclab.chl848.t3android.Parameters;
import com.nclab.chl848.t3android.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Game logic
 */
public class GameManager {
    //region CONSTANTS
    public static final String GAMEMANAGER_REMOTE_INFO_UP_TO_DATE = "t3.chl848.nclab.com.GAMEMANAGER_REMOTE_INFO_UP_TO_DATE";
    public static final String GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME = "t3.chl848.nclab.com.GAMEMANAGER_SERVER_CLIENT_GO_TO_GAME";
    public static final String GAMEMANAGER_READY_TO_NEW_GAME = "t3.chl848.nclab.com.GAMEMANAGER_READY_TO_NEW_GAME";
    public static final String GAMEMANAGER_START_NEW_GAME = "t3.chl848.nclab.com.GAMEMANAGER_START_NEW_GAME";
    public static final String GAMEMANAGER_PLAYER_MOVE = "t3.chl848.nclab.com.GAMEMANAGER_PLAYER_MOVE";
    public static final String GAMEMANAGER_GAME_FINISHED = "t3.chl848.nclab.com.GAMEMANAGER_GAME_FINISHED";
    public static final String GAMEMANAGER_GAME_DRAW = "t3.chl848.nclab.com.GAMEMANAGER_GAME_DRAW";
    public static final String GAMEMANAGER_SWITCH_PLAYER = "t3.chl848.nclab.com.GAMEMANAGER_SWITCH_PLAYER";

    public static final String GAMEMANAGER_TILE_NUM = "t3.chl848.nclab.com.GAMEMANAGER_TILE_NUM";
    public static final String GAMEMANAGER_PLAYER_ID = "t3.chl848.nclab.com.GAMEMANAGER_PLAYER_ID";
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

    private Activity currentActivity = null;

    private int numBoardSpots = Parameters.GRID_SIZE * Parameters.GRID_SIZE;
    private int numFilledBoardSpots = 0;

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
        if (currentActivity != null) {
            Intent i = new Intent(action);
            currentActivity.sendBroadcast(i);
        }
    }

    private void broadcastStatus(String action, String msg) {
        if (currentActivity != null) {
            Intent i = new Intent(action);
            i.putExtra(BLEHandler.BLE_EXTRA_DATA, msg);
            currentActivity.sendBroadcast(i);
        }
    }

    private void broadcastStatus(Intent intent) {
        if (currentActivity != null) {
            currentActivity.sendBroadcast(intent);
        }
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

    public void sendClientSceneLoadedEvent() {
        if (!isHost) {
            byte[] msg = packMessageWithType(Parameters.MSG_CLIENT_SERVER_SCENE_LOADED);
            sendMessage(msg);
        }
    }

    public void startNewGame(String firstPlayerName) {
        activePlayerId = firstPlayerName.equalsIgnoreCase(localPlayer.playerName) ? localPlayer.playerId : remotePlayer.playerId;

        if (isHost) {
            Message.T3StartNewGameMessage.Builder mb = Message.T3StartNewGameMessage.newBuilder();
            mb.setInitPlayerId(activePlayerId);
            byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_START_NEW_GAME, mb.build());
            sendMessage(msg);
        }

        if (localPlayer.tileNumPlayed == null) {
            localPlayer.tileNumPlayed = new HashSet<>();
        } else {
            localPlayer.tileNumPlayed.clear();
        }

        if (remotePlayer.tileNumPlayed == null) {
            remotePlayer.tileNumPlayed = new HashSet<>();
        } else {
            remotePlayer.tileNumPlayed.clear();
        }

        numFilledBoardSpots = 0;

        broadcastStatus(GAMEMANAGER_START_NEW_GAME);
    }

    public void playerMove(int playerId, int tileNum) {
        if (isHost) {
            numFilledBoardSpots++;
            if (localPlayer.playerId == playerId) {
                localPlayer.tileNumPlayed.add(tileNum);
            } else {
                remotePlayer.tileNumPlayed.add(tileNum);
            }

            Message.T3PlayerMoveMessage.Builder mb = Message.T3PlayerMoveMessage.newBuilder();
            mb.setPlayerId(activePlayerId);
            mb.setTileNum(tileNum);
            byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_PLAYER_MOVE, mb.build());
            sendMessage(msg);

            Intent i = new Intent(GAMEMANAGER_PLAYER_MOVE);
            i.putExtra(GAMEMANAGER_PLAYER_ID, playerId);
            i.putExtra(GAMEMANAGER_TILE_NUM, tileNum);
            broadcastStatus(i);

            updateGame();
        } else {
            Message.T3PlayerMoveMessage.Builder mb = Message.T3PlayerMoveMessage.newBuilder();
            mb.setPlayerId(playerId);
            mb.setTileNum(tileNum);
            byte[] msg = packMessageWithType(Parameters.MSG_CLIENT_SERVER_MOVE_ACTION, mb.build());
            sendMessage(msg);
        }
    }

    private void updateGame() {
        Player currentPlayer = activePlayerId == localPlayer.playerId ? localPlayer : remotePlayer;

        if (isPlayerWin(currentPlayer)) {
            Intent i = new Intent(GAMEMANAGER_GAME_FINISHED);
            i.putExtra(GAMEMANAGER_PLAYER_ID, currentPlayer.playerId);
            broadcastStatus(i);
            // send msg to client
            Message.T3GameFinishedMessage.Builder mb = Message.T3GameFinishedMessage.newBuilder();
            mb.setWinnerId(currentPlayer.playerId);
            byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_GAME_FINISHED, mb.build());
            sendMessage(msg);

            return;
        }

        if (numFilledBoardSpots == numBoardSpots) {
            byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_GAME_DRAW);
            sendMessage(msg);

            broadcastStatus(GAMEMANAGER_GAME_DRAW);

            return;
        }

        activePlayerId = activePlayerId == localPlayer.playerId ? remotePlayer.playerId : localPlayer.playerId;

        Message.T3SwitchPlayerMessage.Builder mb = Message.T3SwitchPlayerMessage.newBuilder();
        mb.setActivePlayerId(activePlayerId);
        byte[] msg = packMessageWithType(Parameters.MSG_SERVER_CLIENT_SWITCH_PLAYER, mb.build());
        sendMessage(msg);

        broadcastStatus(GAMEMANAGER_SWITCH_PLAYER);
    }

    private boolean isPlayerWin(Player player) {
        if (player.tileNumPlayed.size() < 3) {
            return false;
        }

        int numWinningCombos = winnerCommbo.size();
        int index = 0;
        while (index < numWinningCombos) {
            if (player.tileNumPlayed.contains(winnerCommbo.get(index).get(0)) &&
                    player.tileNumPlayed.contains(winnerCommbo.get(index).get(1)) &&
                    player.tileNumPlayed.contains(winnerCommbo.get(index).get(2))) {
                return true;
            }

            index++;
        }

        return false;
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
                        remotePlayer.imageId = R.drawable.opiece;;
                    } else {
                        remotePlayer.imageId = R.drawable.xpiece;;
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
                if (isHost) {
                    broadcastStatus(GAMEMANAGER_READY_TO_NEW_GAME);
                }

                break;
            }
            case Parameters.MSG_SERVER_CLIENT_START_NEW_GAME :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_START_NEW_GAME");
                try {
                    Message.T3StartNewGameMessage msg = Message.T3StartNewGameMessage.parseFrom(data);
                    startNewGame(msg.getInitPlayerId() == localPlayer.playerId ? localPlayer.playerName : remotePlayer.playerName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }
            case Parameters.MSG_SERVER_CLIENT_PLAYER_MOVE :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_PLAYER_MOVE");
                if (!isHost) {
                    try {
                        Message.T3PlayerMoveMessage msg = Message.T3PlayerMoveMessage.parseFrom(data);
                        numFilledBoardSpots++;

                        int playerId = msg.getPlayerId();
                        int tileNum = msg.getTileNum();
                        if (localPlayer.playerId == playerId) {
                            localPlayer.tileNumPlayed.add(tileNum);
                        } else {
                            remotePlayer.tileNumPlayed.add(tileNum);
                        }

                        Intent i = new Intent(GAMEMANAGER_PLAYER_MOVE);
                        i.putExtra(GAMEMANAGER_PLAYER_ID, playerId);
                        i.putExtra(GAMEMANAGER_TILE_NUM, tileNum);
                        broadcastStatus(i);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            case Parameters.MSG_CLIENT_SERVER_MOVE_ACTION :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_CLIENT_SERVER_MOVE_ACTION");
                if (isHost) {
                    try {
                        Message.T3PlayerMoveMessage msg = Message.T3PlayerMoveMessage.parseFrom(data);
                        playerMove(msg.getPlayerId(), msg.getTileNum());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            case Parameters.MSG_SERVER_CLIENT_GAME_FINISHED :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_GAME_FINISHED");
                if (!isHost) {
                    try {
                        Message.T3GameFinishedMessage msg = Message.T3GameFinishedMessage.parseFrom(data);
                        Intent i = new Intent(GAMEMANAGER_GAME_FINISHED);
                        i.putExtra(GAMEMANAGER_PLAYER_ID, msg.getWinnerId());
                        broadcastStatus(i);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            case Parameters.MSG_SERVER_CLIENT_GAME_DRAW :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_GAME_DRAW");
                if (!isHost) {
                    broadcastStatus(GAMEMANAGER_GAME_DRAW);
                }
                break;
            }
            case Parameters.MSG_SERVER_CLIENT_SWITCH_PLAYER :
            {
                Log.d(Parameters.TAG, "processMessage: MSG_SERVER_CLIENT_SWITCH_PLAYER");
                if (!isHost) {
                    try {
                        Message.T3SwitchPlayerMessage msg = Message.T3SwitchPlayerMessage.parseFrom(data);
                        activePlayerId = msg.getActivePlayerId();
                        broadcastStatus(GAMEMANAGER_SWITCH_PLAYER);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                break;
            }
        }
    }
    //endregion

}
