package io.nqa.model;

import io.nqa.model.Banishment;
import io.nqa.model.Complaint;
import io.nqa.model.Message;

import java.util.List;

public class EventFunctions {
    /**
     * Hosts functions executed based on notify messages.
     * Edit these functions for your liking. :)
     */


    public void talkStatusChange(boolean isTalking, boolean isWhisper, int clientId) {
        //
    }

    public void message(Message message) {
        // Offline message
    }

    public void messageList(List<Message> messageList) {
        // Offline message list
    }

    public void complainList(List<Complaint> complaintList) {
        // List of complaints
    }

    public void banList(List<Banishment> banList) {
        // List of bans (Banishment)
    }

    public void clientMoved(int channelId, int clientId) {
        // this is for reasonid 0
        // 0 = switched | 1 = moved | 4 = kicked from channel
        // clientId switched to channelId
    }

    public void clientLeftView() {
        //
    }

    public void clientEnteredView() {
        //
    }

    public void clientPoke(int invokerId, String invokerName, String invokerUniqueId, String message) {
        //
    }

    public void clientChatClosed(int clientId, String clientUniqueId) {
        //
    }

    public void clientChatComposing(int clientId, String clientUniqueId) {
        //
    }

    public void clientUpdate() {
        //
    }

    public void clientIds() {
        //
    }

    public void clientDbidFromUid() {
        //
    }


}
