#include "ros/ros.h"
#include "smart_battery_msgs/SmartBatteryStatus.h"

void powerCallback(const smart_battery_msgs::SmartBatteryStatus::ConstPtr& msg)
{
  if (msg->charge_state > 0)  // charging 
	ROS_INFO("Turtlebot Plugged-in, Current Battery Percentage: (%i%%)", msg->percentage);
  else 
	ROS_INFO("Turtlebot Not Plugged-in, Current Battery Percentage: (%i%%)", msg->percentage);
}

int main(int argc, char **argv)
{
  ros::init(argc, argv, "listener_battery");
  ros::NodeHandle n;
  ros::Subscriber sub = n.subscribe("/laptop_charge", 1000, powerCallback);

  ros::spin();


  return 0;
}
