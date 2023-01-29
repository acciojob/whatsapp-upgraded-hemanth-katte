package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Repository
public class WhatsappRepository {

    private HashMap<Group, List<User>> groupUsersDB;
    private HashMap<Group, User> adminDB;
    private HashMap<Group, List<Message>> groupMessagesBD;
    private HashMap<Message, User> senderDB;
    private HashSet<String> userMobileDB;
    private int groupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessagesBD = new HashMap<Group, List<Message>>();
        this.groupUsersDB = new HashMap<Group, List<User>>();
        this.senderDB = new HashMap<Message, User>();
        this.adminDB = new HashMap<Group, User>();
        this.userMobileDB = new HashSet<>();
        this.groupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {

        if(userMobileDB.contains(mobile)){
            throw new Exception("User already present in DB");
        }
        userMobileDB.add(mobile);
        User user = new User(name, mobile);
        return "New User Created";
    }

    public Group createGroup(List<User> users){

        if(users.size()==2){
            Group group = new Group(users.get(1).getName(), 2);

            adminDB.put(group, users.get(0));

            groupMessagesBD.put(group, new ArrayList<Message>());

            groupUsersDB.put(group, users);
            return group;
        }
        this.groupCount += 1;
        Group group = new Group(new String("Group "+this.groupCount), users.size());
        adminDB.put(group, users.get(0));

        groupMessagesBD.put(group, new ArrayList<Message>());

        groupUsersDB.put(group, users);


        return group;
    }


    public int createMessage(String content){

        Message message = new Message(messageId, content);
        this.messageId += 1;
        return message.getId();

    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{

        if(adminDB.containsKey(group)){

            List<User> users = groupUsersDB.get(group);

            Boolean userExists = false;

            for(User user: users){
                if(user.equals(sender)){
                    userExists = true;
                    break;
                }
            }

            if(userExists){
                senderDB.put(message, sender);
                List<Message> messages = groupMessagesBD.get(group);
                messages.add(message);
                groupMessagesBD.put(group, messages);
                return messages.size();
            }
            throw new Exception("User not part of group");
        }
        throw new Exception("Group does not exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{

        if(adminDB.containsKey(group)){
            if(adminDB.get(group).equals(approver)){
                List<User> groupMembers = groupUsersDB.get(group);

                Boolean userExists = false;

                for(User participant: groupMembers){
                    if(participant.equals(user)){
                        userExists = true;
                        break;
                    }
                }

                if(userExists){
                    adminDB.put(group, user);
                    return "Successfully updated";
                }
                throw new Exception("User is not member");
            }
            throw new Exception("Approver can't approve");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception{

        Boolean userExists = false;
        Group userGroup = null;
        for(Group group: groupUsersDB.keySet()){
            List<User> participants = groupUsersDB.get(group);
            for(User participant: participants){
                if(participant.equals(user)){
                    if(adminDB.get(group).equals(user)){
                        throw new Exception("Cannot remove admin");
                    }
                    userGroup = group;
                    userExists = true;
                    break;
                }
            }

            if(userExists){
                break;
            }
        }

        if(userExists){
            List<User> users = groupUsersDB.get(userGroup);

            List<User> updatedUsers = new ArrayList<>();

            for(User participant: users){

                if(participant.equals(user))
                    continue;
                updatedUsers.add(participant);
            }
            groupUsersDB.put(userGroup, updatedUsers);


            List<Message> messages = groupMessagesBD.get(userGroup);
            List<Message> updatedMessages = new ArrayList<>();
            for(Message message: messages){

                if(senderDB.get(message).equals(user))
                    continue;
                updatedMessages.add(message);
            }
            groupMessagesBD.put(userGroup, updatedMessages);


            HashMap<Message, User> updatedSenderMap = new HashMap<>();
            for(Message message: senderDB.keySet()){
                if(senderDB.get(message).equals(user))
                    continue;
                updatedSenderMap.put(message, senderDB.get(message));
            }
            senderDB = updatedSenderMap;
            return updatedUsers.size()+updatedMessages.size()+updatedSenderMap.size();
        }


        throw new Exception("User not found");
    }
}


