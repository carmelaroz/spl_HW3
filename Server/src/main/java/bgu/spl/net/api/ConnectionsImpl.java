package bgu.spl.net.api;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
public class ConnectionsImpl implements Connections {
    private ConcurrentHashMap<String,User> ActiveUsersMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,User> UsersMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConnectionHandler> connectionsMap = new ConcurrentHashMap<>();
    private LinkedList<String> filteredWords = new LinkedList<String>();
    private int howMuchPostIPost;

    private static class SingletonHolder{
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }
    public static ConnectionsImpl getInstance(){
        return SingletonHolder.instance;
    }
    @Override
    public boolean send(int connectionId, Object msg) {
//        if(connectionsMap.get(connectionId) == null)
//            return false;
        ConnectionHandler handler = connectionsMap.get(connectionId);
        if(handler != null) {
            handler.send(msg);
            return true;
        }
        return false;



    }

    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }

    public ConcurrentHashMap<String,User> getUsersMap(){
        return this.UsersMap;
    }

    public ConcurrentHashMap<String,User> getActiveUsersMap(){
        return this.ActiveUsersMap;
    }

    public int getHowMuchPostIPost() {
        return howMuchPostIPost;
    }

    public void incrementPostIPost(){
        this.howMuchPostIPost++;
    }

    public LinkedList<String> getFilteredWords() {
        return filteredWords;
    }

    public ConcurrentHashMap<Integer, ConnectionHandler> getConnectionsMap() {
        return connectionsMap;
    }
}