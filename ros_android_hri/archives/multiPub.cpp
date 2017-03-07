#include "ros/ros.h"
#include "std_msgs/Header.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "headerMultiPub");
  
  ros::NodeHandle n;   
  ros::Publisher headPub1 = n.advertise<std_msgs::Header>("header_1", 1000);
  ros::Publisher headPub2 = n.advertise<std_msgs::Header>("header_2", 1000);
  ros::Publisher headPub3 = n.advertise<std_msgs::Header>("header_3", 1000);
 
  ros::Rate loop_rate(10);
  int i = 0;
  while (ros::ok()) {        	 
	
	std_msgs::Header msg1, msg2, msg3; 
	
	msg1.stamp = ros::Time::now();                                // time jobstatus is updated
	msg2.stamp = ros::Time::now();                                // time jobstatus is updated
	msg3.stamp = ros::Time::now();                                // time jobstatus is updated
	
	msg1.seq = 1;  		                                // default to 0
	msg2.seq = 2;  		                                // default to 0
	msg3.seq = 3;  		                                // default to 0	
	
	msg1.frame_id = "message: ";                                  // default to "Robot Status: Awaiting Commands"    	
	msg2.frame_id = "message: ";                                  // default to "Robot Status: Awaiting Commands"    	
	msg3.frame_id = "message: ";                                  // default to "Robot Status: Awaiting Commands"    	
	
	headPub1.publish(msg1);     
	ROS_INFO("Header (%i), %s %i", msg1.seq, msg1.frame_id.c_str(), i);       // show string on command prompt
	
	headPub2.publish(msg2);     
	ROS_INFO("Header (%i), %s %i", msg2.seq, msg2.frame_id.c_str(), i);       // show string on command prompt
	
	headPub3.publish(msg3);     		
	ROS_INFO("Header (%i), %s %i", msg3.seq, msg3.frame_id.c_str(), i);       // show string on command prompt
	
	loop_rate.sleep();
	ros::spinOnce();  
	i++;
	}
}
