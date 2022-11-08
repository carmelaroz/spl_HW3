package bgu.spl.net.api;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);

    ConcurrentHashMap<String,User> getUsersMap();

    ConcurrentHashMap<String,User> getActiveUsersMap();

    int getHowMuchPostIPost();

    void incrementPostIPost();

    Queue<String> getFilteredWords();

    ConcurrentHashMap<Integer, ConnectionHandler> getConnectionsMap();
}
