#include "ros/ros.h"
#include "std_msgs/Header.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "headerPubOnce3");
  
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<std_msgs::Header>("headerOnce_3", 1000);

 
  // wait till connection to commandListener is established
  ros::Rate poll_rate(100);
  while(commandPublisher.getNumSubscribers() == 0)
	poll_rate.sleep();
  
 
  int count = 0;
  ros::Rate loop_rate(10);
  
  while (ros::ok() && count <= 0){  // a simple hack to publish only once 	
	// build msg 
        	 
	std_msgs::Header msg;     		// create new msg obj - type NavSatFix 
	
	msg.stamp = ros::Time::now();                                // time jobstatus is updated
	msg.seq = 6;  		                                // default to 0
	msg.frame_id = "messageOnce: ";                                  // default to "Robot Status: Awaiting Commands"  	
	
	commandPublisher.publish(msg);     // publish message 
   
	loop_rate.sleep();
    ++count;
	ros::spinOnce();  
	}
}
