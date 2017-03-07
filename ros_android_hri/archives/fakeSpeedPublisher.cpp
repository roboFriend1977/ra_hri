#include "ros/ros.h"
#include "nav_msgs/Odometry.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "SpeedPublisher_ROS");
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<nav_msgs::Odometry>("/odom", 1000);
   
 
  int count = 0;
  ros::Rate loop_rate(10);
  
  while (ros::ok())  
 {
          	 
   nav_msgs::Odometry msg;     		
   commandPublisher.publish(msg); 

   loop_rate.sleep();

    ++count;
   ros::spinOnce();  
  }
}
