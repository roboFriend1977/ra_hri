package org.ros.android.main_app;

/**
 * Created by samihajjaj on 10/9/16.
 */

import org.ros.concurrent.CancellableLoop;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

public class rcPublisher<T> extends AbstractNodeMain {

    private String topic_name, command_name;
    private int command_id =-1;
    private boolean publishOnce=false; // to set default behaviour, which is publish continously
    private Time command_time;

    public rcPublisher() {      // simple Construct with a default topic name, which can be changed later
        topic_name = "counter";
    }

    public rcPublisher(String topic) { // this construct allows user to set topicName
        topic_name = topic;
    }

    public void setCommandID(int command_id){
        this.command_id = command_id;
    }

    public void setCommandName(String command_name){
        this.command_name = command_name;
    }

    public void setPublishOnce(boolean publish_once){
        this.publishOnce = publish_once;
    }

    public void setCommandTime(Time command_time){
        this.command_time = command_time;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/myPublisher");
    }


    @Override
    public void onStart(final ConnectedNode connectedNode) {

        final Publisher<std_msgs.Header> publisher =
                connectedNode.newPublisher(topic_name, std_msgs.Header._TYPE);    // new Publisher

        connectedNode.executeCancellableLoop(new CancellableLoop() {

            private int sequenceNumber;

            @Override
            protected void setup() {  sequenceNumber = 0; }

            @Override
            protected void loop() throws InterruptedException {

                if (publishOnce) {
                    while (publisher.getNumberOfSubscribers() == 0)
                        Thread.sleep(100);
                    if (sequenceNumber > 0)
                        throw new InterruptedException();}

                std_msgs.Header msg = publisher.newMessage();
                msg.setStamp(command_time);     // command time is when option was clicked from android
                msg.setSeq((byte) command_id);
                msg.setFrameId(command_name);

                publisher.publish(msg);
                sequenceNumber++;
                Thread.sleep(100);


            }
        });
    }
}