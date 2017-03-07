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

import sensor_msgs.NavSatStatus;

public class GPSPublisher<T> extends AbstractNodeMain {

    private String topic_name;
    private int publisherID = -1;
    private boolean publishOnce = false; // to set default behaviour, which is publish continously

    public GPSPublisher() {      // simple Construct with a default topic name, which can be changed later
        topic_name = "odometry/gps";
    }

    public GPSPublisher(String topic) { // this construct allows user to set topicName
        topic_name = topic;
    }

    public void setPublisherID(int publisher_id) {
        this.publisherID = publisher_id;
    }

    public void setPublishOnce(boolean publish_once) {
        this.publishOnce = publish_once;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/myGPSPublisher");
    }


    @Override
    public void onStart(ConnectedNode connectedNode) {

        final Publisher<sensor_msgs.NavSatFix> publisher = connectedNode.newPublisher(topic_name, sensor_msgs.NavSatFix._TYPE); // new Publisher

        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private int sequenceNumber;

            @Override
            protected void setup() {  // reset value
                sequenceNumber = 0;
            }

            @Override
            protected void loop() throws InterruptedException {

                if (publishOnce) { // publish just one message
                    while (publisher.getNumberOfSubscribers() == 0)
                        Thread.sleep(100);  //  wait till connection with listener established
                    if (sequenceNumber > 0)
                        throw new InterruptedException();           // to exit loop after first message
                    // to publish a specific number of messages, set sequenceNumber = numb - 1
                }

                sensor_msgs.NavSatFix msg = publisher.newMessage();     // create new message obj, based on message type

                // build NavSatFix message


                publisher.publish(msg); // Publish message
                sequenceNumber++;       // loop variable
                Thread.sleep(100);      // control Publisher rate, 1000 = 1 second
            }
        });

    }

}