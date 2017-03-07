#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <sstream>  

/* this listener waits for commandCodes,
   Once received, it uses captured data to start the corresponding ROS node 
   as well as updating the robotJobStatusPublisher
*/

void commandCallback(const std_msgs::Header::ConstPtr& msg) {
  std::stringstream ss; 
  ss << "rosrun ra_hri " << msg->frame_id << ";";   // start robot program
  ss << "rosrun ra_hri rjsPublisher";				// start rjsPublisher	
  ss << " _status_id:=" << msg->seq; 				// pass command_id	
  ss << " _status_name:=" << msg->frame_id.c_str(); // pass command_name
  system(ss.str().c_str()); }  // start ROS nodes 


int main(int argc, char **argv) {
  ros::init(argc, argv, "commandListener");
  ros::NodeHandle n;  
  
while (ros::ok()){
  ros::Subscriber sub = n.subscribe("turtlebotCommands", 1000, commandCallback);
  ros::spin();
  }
  return 0;
}
