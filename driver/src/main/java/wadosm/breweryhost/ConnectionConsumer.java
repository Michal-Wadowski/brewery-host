package wadosm.breweryhost;

public class ConnectionConsumer {

     private final DriverEntry driverEntry;
     private final MessagesProcessor messagesProcessor;

     public ConnectionConsumer(DriverEntry driverEntry, MessagesProcessor messagesProcessor) {
          this.driverEntry = driverEntry;
          this.messagesProcessor = messagesProcessor;
     }

     public native void attachListener();

     String receivedMessage(String message) {
          return messagesProcessor.processMessage(message);
     }

}
