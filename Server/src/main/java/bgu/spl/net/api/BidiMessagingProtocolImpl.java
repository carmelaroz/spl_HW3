package bgu.spl.net.api;


import javax.swing.text.html.HTMLDocument;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol {
    private boolean shouldTerminate = false;
    private int connectionId = -1;
    private Connections connections = ConnectionsImpl.getInstance();
    @Override
    public void start(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void process(Object message) {
        String optCode = message.toString().substring(0, 2);
        message = message.toString().substring(2);
        if (optCode.equals("01")) {  // Register
            String[] args = message.toString().split("\0");
            String username = args[0];
            String password = args[1];
            String birthday = args[2];
            User user = new User(username, password, birthday);
            Collection c2 = this.connections.getUsersMap().values();
            Iterator<User> iter2 = c2.iterator();
            boolean found = false;
            while (iter2.hasNext()) {
                if(iter2.next().getUsername().contains(username))
                    found = true;
            }
            if (!found) {
                this.connections.getUsersMap().put(username,user);
                this.connections.send(this.connectionId, "1001"); // replace
            } else {
                this.connections.send(this.connectionId, "1101");
            }
        } else if (optCode.equals("02")) {  // Login
            String[] args = message.toString().split("\0");
            String username = args[0];
            String password = args[1];
            String captcha = args[2];
            Collection c2 = this.connections.getUsersMap().values();
            boolean found = false;
            User user = null;
            Iterator<User> iter2 = c2.iterator();
            while (iter2.hasNext()) {
                user = iter2.next();
                if(user.getUsername().contains(username)) {
                    found = true;
                    break;
                }
            }

                if (found && user.getPassword().equals(password) && captcha.equals("1") && !user.getIsActivated()) {
                    this.connections.getActiveUsersMap().put(username, user);
                    user.activate();
                    user.setConnId(this.connectionId);
                    this.connections.send(this.connectionId, "1002");
                    if (!user.getAwaitingPosts().isEmpty()) {
                        String senderName = "";
                        int limit = 0;
                        for (String s : user.getAwaitingPosts()) {
                            for (int i = 0; i < s.length(); i++) {
                                if (s.charAt(i) != ' ') {
                                    senderName += s.charAt(i);
                                } else {
                                    limit = ++i;
                                    break;
                                }
                            }
                            s = s.substring(limit, s.length());
                            this.connections.send(user.getConnId(), "091" + senderName + "\0" + s + "\0");
                        }
                    }
                }

                else
                    this.connections.send(this.connectionId, "1102");

        } else if (optCode.equals("03")) {  // Logout
            boolean found = false;
            Collection c = this.connections.getActiveUsersMap().values();
            Iterator<User> iter = c.iterator();
            while(iter.hasNext()){
                User myUser = iter.next();
                if(myUser.getConnId() == this.connectionId) {
                    myUser.deActivate();
                    this.connections.getActiveUsersMap().remove(myUser);
                    this.connections.send(this.connectionId, "1003");
                    found = true;
                    break;
                }
            }
//            for (Object user : this.connections.getActiveUsersMap().keySet()) {
//                if (((User) user).getConnId() == this.connectionId) {
//                    ((User) user).deActivate();
//                    this.connections.getActiveUsersMap().remove(user);
//                    this.connections.send(this.connectionId, "1003");
//                    found = true;
//                    break;
//                }
//            }
            if (!found)
                this.connections.send(this.connectionId, "1103");
        }
        else if (optCode.equals("04")) {  // Follow/Unfollow
            boolean found = false;
            String action = message.toString().substring(0, 1);
            message = message.toString().substring(1);
            Collection c2 = this.connections.getUsersMap().values();
            User otherUser = new User("","","");
            Iterator<User> iter2 = c2.iterator();
            while(iter2.hasNext()){
                    otherUser = iter2.next();
                if(message.toString().contains(otherUser.getUsername())){
                    found = true;
                    break;
                }
            }
            if(found) {
                Collection c1 = this.connections.getActiveUsersMap().values();
                Iterator<User> iter1 = c1.iterator();
                while (iter1.hasNext()) {
                    User myUser = iter1.next();
                    if (myUser.getConnId() == this.connectionId) {
                        if (myUser.getIsActivated()) {
                            if (action.equals("0") && !myUser.getFollowers().contains(otherUser) && !otherUser.getBlockingUsers().contains(myUser)) {
                                myUser.addFollower(otherUser);
                                myUser.incrementFollowing();
                                this.connections.send(this.connectionId, "100400" + message + "\0");
                            } else if (action.equals("1") & myUser.getFollowers().contains(otherUser)) {
                                myUser.removeFollower(otherUser);
                                myUser.decreaseFollowing();
                                this.connections.send(this.connectionId, "100401" + message + "\0");
                            } else
                                this.connections.send(this.connectionId, "1104");
                        }
                        break;
                    }
                }
            }
            else
                this.connections.send(this.connectionId, "1104");
        } else if (optCode.equals("05")) {  // Post
            message = message.toString().substring(0, message.toString().length() - 1);
            String userName = "";
            BlockingQueue<User> otherUsers = new LinkedBlockingQueue<User>();
            boolean found = false;
            Collection c1 = this.connections.getActiveUsersMap().values();
            Iterator<User> iter1 = c1.iterator();
            User myUser = null;
            while (iter1.hasNext()) {
                myUser = iter1.next();
                if (myUser.getConnId() == this.connectionId) {
                    found = true;
                    break;
                }
            }
            if(found) {
                for (int i = 0; i < message.toString().length(); i++) {
                    if (message.toString().charAt(i) == '@') {
                        for (int j = i + 1; j < message.toString().length(); j++) {
                            if (message.toString().charAt(j) == ' ' || j == message.toString().length() - 1) {
                                Collection c2 = this.connections.getUsersMap().values();
                                Iterator<User> iter2 = c2.iterator();
                                while (iter2.hasNext()) {
                                    User checkUser = iter2.next();
                                    if (checkUser.getUsername().contains(userName)) {
                                        otherUsers.add(checkUser);
                                    }
                                }
                                userName = "";
                                i = j;
                                break;
                            } else {
                                userName += message.toString().charAt(j);
                            }
                        }
                    }
                }
                for (User s : otherUsers) {
                    if (s.getFollowers().contains(myUser)) {
                        otherUsers.remove(s);
                    }
                }
                Collection c4 = this.connections.getUsersMap().values();
                Iterator<User> iter4 = c4.iterator();
                while (iter4.hasNext()) {
                    User checker = iter4.next();
                    if(checker.getFollowers().contains(myUser)) {
                        if (!checker.getIsActivated()) {
                            checker.getAwaitingPosts().add(myUser.getUsername() + " " + message.toString());
                            this.connections.incrementPostIPost();
                        } else {
                            this.connections.send(checker.getConnId(), "091" + myUser.getUsername() + "\0" + message + "\0");
                            this.connections.incrementPostIPost();
                        }
                    }
                }
//                for (User u : myUser.getFollowers()) {
//                    if (!u.getIsActivated()) {
//                        u.getAwaitingPosts().add(myUser.getUsername() + " " + message.toString());
//                        this.connections.incrementPostIPost();
//                    } else {
//                        this.connections.send(u.getConnId(), "091" + myUser.getUsername() + "\0" + message + "\0");
//                    }
//                }
                for (User s : otherUsers) {
                    if (!s.getIsActivated()) {
                        s.getAwaitingPosts().add(myUser.getUsername() + " " + message.toString());
                        this.connections.incrementPostIPost();
                    } else {
                        this.connections.send(s.getConnId(), "091" + myUser.getUsername() + "\0" + message + "\0");
                        this.connections.incrementPostIPost();
                    }
                }
                this.connections.send(this.connectionId, "1005");
            } else
                this.connections.send(this.connectionId, "1105");

        }
        else if (optCode.equals("06")) { // PM
            message = message.toString().substring(0, message.toString().length() - 1);
            Collection c1 = this.connections.getActiveUsersMap().values();
            Iterator<User> iter1 = c1.iterator();
            User myUser = null;
            while (iter1.hasNext()) {
                myUser = iter1.next();
                if (myUser.getConnId() == this.connectionId) {
                    break;
                }
            }
            Collection c2 = this.connections.getUsersMap().values();
            User otherUser = null;
            Iterator<User> iter2 = c2.iterator();
            while(iter2.hasNext()){
                otherUser = iter2.next();
                if(message.toString().contains(otherUser.getUsername())){
                    break;
                }
            }
                if (myUser == null || otherUser == null || !myUser.getFollowers().contains(otherUser) || otherUser.getBlockingUsers().contains(myUser))
                    this.connections.send(this.connectionId, "1106");
                else {
                    this.connections.incrementPostIPost();
                    Iterator it = this.connections.getFilteredWords().iterator();
                    while (it.hasNext()){
                        message = message.toString().replaceAll("\\b" + it.next() + "\\b", "<filtered>");
                    }
                    message = message.toString().substring(otherUser.getUsername().length() + 1);
//                    for (String s : this.connections.getFilteredWords()) {
//                        message.toString().replaceAll("\\b" + s + "\\b", "<filtered>");
//                    }
                    this.connections.send(otherUser.getConnId(), "090" + myUser.getUsername() + "\0" + message.toString() + "\0");
                    this.connections.send(this.connectionId, "1006");
                }
            }

        else if(optCode.equals("07")) { // LOGSTAT
            User myUser = null;
            Collection c = this.connections.getActiveUsersMap().values();
            Iterator<User> iter = c.iterator();
            while (iter.hasNext()) {
                myUser = iter.next();
                if (myUser.getConnId() == this.connectionId) {
                    break;
                }
            }
//            for (Object user : this.connections.getActiveUsersMap().keySet()) {
//                if (((User) user).getConnId() == this.connectionId) {
//                    myUser = (User) user;
//                }
//            }
            if (myUser != null) {
                Collection c2 = this.connections.getActiveUsersMap().values();
                Iterator<User> iter2 = c2.iterator();
                while (iter2.hasNext()) {
                    User user = iter2.next();
                    if (!user.getBlockingUsers().contains(myUser)) {
//                        LocalDate date = LocalDate.now();
//                        LocalDate birthdate = LocalDate.parse(user.getBirthday());
//                        int age = Period.between(birthdate, date).getYears();
                        String year = user.getBirthday().substring(6);
                        int yearsToInt = Integer.parseInt(year);
                        ZonedDateTime now = ZonedDateTime.now();
                        int yearsNow = now.getYear();
                        int age = yearsNow - yearsToInt - 1;
                        int numFollowing = user.getFollowers().size();
                        int numFollowers = user.getFollowing();
                        BlockingQueue<String> listOfPosts = user.getAwaitingPosts();
                        int numOfPosts = listOfPosts.size();
                        this.connections.send(this.connectionId, "1007" + age + " " +  numFollowing + " " + numFollowers + " " + numOfPosts);
//                        message = "";
                    }

//                for (Object u : this.connections.getActiveUsersMap().keySet()) {
//                    User user = (User) u;
//                    if (!user.getBlockingUsers().contains(myUser)) {
//                        LocalDate date = LocalDate.now();
//                        LocalDate birthdate = LocalDate.parse(user.getBirthday());
//                        int age = Period.between(birthdate, date).getYears();
//                        int numFollowing = user.getFollowers().size();
//                        int numFollowers = user.getFollowing();
////                        LinkedList<String> listOfPosts = (LinkedList<String>) this.connections.getPosts().get(user.getUsername()); // fix this with the new awaitingPosts in User
////                        int numOfPosts = listOfPosts.size();
////                        message += Integer.toString(age) + " " + Integer.toString(numOfPosts) + " " + Integer.toString(numFollowers) + " " + Integer.toString(numFollowing);
//                        this.connections.send(this.connectionId, "1007" + message);
//                        message = "";
//                    }
//                }
//                this.connections.send(this.connectionId, "1007" + message);

                    else
                        this.connections.send(this.connectionId, "1107");
                }
            }
        }
        else if(optCode.equals("08")) {  // STAT
            User myUser = null;
            for (Object user : this.connections.getActiveUsersMap().keySet()) {
                if (((User) user).getConnId() == this.connectionId) {
                    myUser = (User) user;
                }
            }
            if(myUser != null) {
                int size = message.toString().length();
                message = message.toString().substring(0, size - 1);
                String ans = "";
                String[] args = message.toString().split("|");
                for(String username : args) {
                    User user = (User) this.connections.getUsersMap().get(username);
                    if (!user.getBlockingUsers().contains(myUser)) {
                        LocalDate date = LocalDate.now();
                        LocalDate birthdate = LocalDate.parse(user.getBirthday());
                        int age = Period.between(birthdate, date).getYears();
                        int numFollowing = user.getFollowers().size();
                        int numFollowers = user.getFollowing();
//                        LinkedList<String> listOfPosts = (LinkedList<String>) this.connections.getPosts().get(user.getUsername());
//                        int numOfPosts = listOfPosts.size();
//                        ans += Integer.toString(age) + " " + Integer.toString(numOfPosts) + " " + Integer.toString(numFollowers) + " " + Integer.toString(numFollowing);
                        this.connections.send(this.connectionId,"1008" + ans);
                        ans = "";
                    }
                }
//                this.connections.send(this.connectionId,"1008" + ans);
            }
            else
                this.connections.send(this.connectionId,"1108");
        }
        else if(optCode.equals("12")) {  // BLOCK
            Collection c1 = this.connections.getActiveUsersMap().values();
            Iterator<User> iter1 = c1.iterator();
            boolean found1 = false;
            boolean found2 = false;
            User myUser = null;
            while (iter1.hasNext()) {
                myUser = iter1.next();
                if (myUser.getConnId() == this.connectionId) {
                    found1 = true;
                    break;
                }
            }
            Collection c2 = this.connections.getUsersMap().values();
            User otherUser = null;
            Iterator<User> iter2 = c2.iterator();
            while(iter2.hasNext()){
                otherUser = iter2.next();
                if(message.toString().contains(otherUser.getUsername())){
                    found2 = true;
                    break;
                }
            }
            if(found1 && found2 && !myUser.getBlockingUsers().contains(otherUser)) {
                int size = message.toString().length();
                message = message.toString().substring(0, size - 1);

                if(otherUser != null){
                    myUser.getBlockingUsers().add(otherUser);
                    if(myUser.getFollowers().contains(otherUser)){
                        myUser.getFollowers().remove(otherUser);
                    }
                    if(otherUser.getFollowers().contains(myUser)){
                        otherUser.getFollowers().remove(myUser);
                    }
                    this.connections.send(this.connectionId,"1012");
                }
                else {
                    this.connections.send(this.connectionId,"1112");
                }
            }
            else {
                this.connections.send(this.connectionId,"1112");
            }
        }
    }



    @Override
    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }
}
