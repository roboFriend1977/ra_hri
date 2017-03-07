#include "ros/ros.h"
#include "sensor_msgs/NavSatFix.h"

/* subscribes to the rosJobPublisher .. to obtain status_id and text 
*/

void fakeGPSCallback(const sensor_msgs::NavSatFix::ConstPtr& msg) // messageType = NavSatFix (contains only Lat and Long) 
{
  
  ROS_INFO("fakeGPSListener: latitude = %f, Longitude = %f", msg->latitude, msg->longitude);
  system("rosnode kill robotPatrol_ROS"); // gets stuck here 
  
    

}

int main(int argc, char **argv)
{

  ros::init(argc, argv, "fakeGPSListener");
  ros::NodeHandle n;

while (ros::ok())
  {
  ros::Subscriber sub = n.subscribe("fake_fix", 1000, fakeGPSCallback);
  ros::spin();
  }
  return 0;
}
