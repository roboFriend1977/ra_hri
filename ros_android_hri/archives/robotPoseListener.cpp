#include "ros/ros.h"
#include "nav_msgs/Odometry.h"


void robotPoseCallback(const nav_msgs::Odometry::ConstPtr& msg)
{
  double posX = msg->pose.pose.position.x; 
  double posY = msg->pose.pose.position.y; 
  double orientZ = msg->pose.pose.orientation.z; 
  	
  ROS_INFO(" X position: %f \n Y position: %f \n Z rotation: %f \n", posX, posY, orientZ);
}

int main(int argc, char **argv)
{
  
  ros::init(argc, argv, "robotPoselistener");

  ros::NodeHandle n;

  ros::Subscriber sub = n.subscribe("/odom", 1000, robotPoseCallback);

  ros::spin();

  return 0;
}
