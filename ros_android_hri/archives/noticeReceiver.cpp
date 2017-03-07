#include "ros/ros.h"
#include "std_msgs/Header.h"

//#include <sstream> 


/* subscribes to the rosJobPublisher .. to obtain status_id and text 
*/

void noticeCallback(const std_msgs::Header::ConstPtr& msg) // messageType = Header (containing text, int, and timestamp
{
  
  ROS_INFO("Notice Received: [%d]: %s", msg->seq, msg->frame_id.c_str());  

}

int main(int argc, char **argv)
{

  ros::init(argc, argv, "noticeReceiver_ROS");
  ros::NodeHandle n;

  while (ros::ok()){
	ros::Subscriber sub = n.subscribe("commandNotice", 1, noticeCallback);
	ros::spin(); // once sub = spin
  }
  return 0;
}
