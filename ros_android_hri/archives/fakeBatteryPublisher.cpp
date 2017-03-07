#include "ros/ros.h"
#include "smart_battery_msgs/SmartBatteryStatus.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "BatteryPublisher_ROS");
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<smart_battery_msgs::SmartBatteryStatus>("/laptop_charge", 1000);
   
 
  int count = 0;
  ros::Rate loop_rate(10);
  
  while (ros::ok())  
 {
          	 
   smart_battery_msgs::SmartBatteryStatus msg;     		
   commandPublisher.publish(msg); 

   loop_rate.sleep();

    ++count;
   ros::spinOnce();  
  }
}
