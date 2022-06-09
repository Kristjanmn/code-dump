package io.nqa.model;

public class EventHandler {
    /**
     * Handles incoming notify events.
     */

    EventFunctions eventFunctions;

    private EventHandler(/*String event*/) {
        /*try {
            if(event.isBlank() || !event.startsWith("notify")) return;
        }*/
        eventFunctions = new EventFunctions();
    }

    private static volatile EventHandler eventHandler;
    public static EventHandler getEventHandler() {
        if(eventHandler == null) {
            eventHandler = new EventHandler();
        }
        return eventHandler;
    }

    public void handleEvent(String event) {
        System.out.println("event: " + event);
        if(event.isBlank()) return;

        String eventName = "full name of the notify message";
        switch(eventName.toLowerCase()) {   // These come in lower case anyway.
            default:
                System.out.println(eventName + " not handled in switch");
                break;
            case "notifytalkstatuschange":
                //eventFunctions.talkStatusChange();
                break;
            case "notifymessage":
                /*Message message = new Message();
                eventFunctions.message(message);*/
                break;
        }
    }
}
