#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <string>

//#include <sstream> 


/* subscribes to the rosJobPublisher .. to obtain status_id and text 
*/

void rsCallback(const std_msgs::Header::ConstPtr& msg) // messageType = Header (containing text, int, and timestamp
{
 	
  if 	  (msg->seq == 0)	ROS_INFO("Configuration Update: %s", msg->frame_id.c_str());  
  else if (msg->seq == 1)	ROS_INFO("Completion Update: %s", msg->frame_id.c_str());  
  else						ROS_INFO("Job Status Update: %s", msg->frame_id.c_str());  
 
}

int main(int argc, char **argv)
{

  ros::init(argc, argv, "rsListener_ROS");
  ros::NodeHandle n;

  while (ros::ok()){
	ros::Subscriber sub = n.subscribe("robotStatus", 1, rsCallback);
	ros::spin(); // one listener = spin 
  }
  return 0;
}
