#include "ros/ros.h"
#include "std_msgs/Header.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "headerPub1");
  
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<std_msgs::Header>("header_1", 1000);
 
  ros::Rate loop_rate(10);
  int i = 0;
  while (ros::ok()) {        	 
	std_msgs::Header msg; 
	
	msg.stamp = ros::Time::now();                                // time jobstatus is updated
	msg.seq = 1;  		                                // default to 0
	msg.frame_id = "message: ";                                  // default to "Robot Status: Awaiting Commands"    	
	
	commandPublisher.publish(msg);     
	ROS_INFO("Header (%i), %s %i", msg.seq, msg.frame_id.c_str(), i);       // show string on command prompt
   
	loop_rate.sleep();
	ros::spinOnce();  
	i++;
	}
}
