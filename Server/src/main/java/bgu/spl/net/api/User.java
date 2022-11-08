package bgu.spl.net.api;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    private String username;
    private String password;
    private String birthday;
    private boolean isActivated;
    private int connId;
    private BlockingQueue<User> followers = new LinkedBlockingQueue<>();
    private BlockingQueue<String> awaitingPosts = new LinkedBlockingQueue<>();
    private Short following = 0;
    private BlockingQueue<User> blockingUsers = new LinkedBlockingQueue<>();
    public User(String username,String password,String birthday){
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.isActivated = false;
        this.connId = -1;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

    public boolean getIsActivated(){
        return isActivated;
    }

    public void activate(){
        if(!isActivated)
            this.isActivated = true;
    }

    public void deActivate(){
        if(isActivated)
            this.isActivated = false;
    }

    public int getConnId() {
        return this.connId;
    }

    public void setConnId(int connId) {
        this.connId = connId;
    }

    public BlockingQueue<User> getFollowers(){
        return this.followers;
    }

    public Short getFollowing() {
        return following;
    }

    public void incrementFollowing(){
        this.following++;
    }
    public void decreaseFollowing(){
        this.following--;
    }

    public void addFollower(User user){
        this.followers.add(user);
    }

    public void removeFollower(User user){
        this.followers.remove(user);
    }

    public BlockingQueue<User> getBlockingUsers() {
        return blockingUsers;
    }

    public BlockingQueue<String> getAwaitingPosts() {
        return awaitingPosts;
    }
}
