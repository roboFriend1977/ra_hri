package org.ros.android.main_app;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * Created by samihajjaj on 1/7/17.
 */

public class pathPublisher<T> extends AbstractNodeMain {

    private String topic_name;
    private double pointsArray[];
    private boolean publishOnce = false; // to set default behaviour, which is publish continuously

    public pathPublisher() {
        topic_name = "default_topic_name";  // starts this publisher with this default topic
    }

    public pathPublisher(String topic) {  // starts this publisher with the given topic
        topic_name = topic;
    }

    public void setPointsArray(double points_array[]) {
        this.pointsArray = points_array;  // array was passed from calling method
    }

    public void setPublishOnce(boolean publish_once) {
        this.publishOnce = publish_once;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/myPublisher"); // doesnt matter, would be renamed in App later
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {

        final Publisher<std_msgs.Float64MultiArray> publisher =
                connectedNode.newPublisher(topic_name, std_msgs.Float64MultiArray._TYPE);    // new Publisher

        connectedNode.executeCancellableLoop(new CancellableLoop() {

            private int count;

            @Override
            protected void setup() {
                count = 0;
            }

            @Override
            protected void loop() throws InterruptedException {


                if (publishOnce) {
                    while (publisher.getNumberOfSubscribers() == 0)
                        Thread.sleep(100);
                    if (count > 0)
                        throw new InterruptedException();
                }

                // init msgMatrix
                std_msgs.Float64MultiArray msgMatrix = publisher.newMessage();

                // Default layout settings are applied
                msgMatrix.setData(pointsArray);
                publisher.publish(msgMatrix); // publish msgMatrix to robot
                count++;
                Thread.sleep(100);
            }
        });
    }


}
