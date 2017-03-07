#include "ros/ros.h"
#include "sensor_msgs/NavSatFix.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "GPSPublisher_ROS");
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<sensor_msgs::NavSatFix>("fake_fix", 1000);
   
  double lat = 2.9739523474476197; 
  double lon = 101.72868847846985;    // Starting position @ uniten GPS coordinates  
 
 
  // wait till connection to commandListener is established
  ros::Rate poll_rate(100);
  while(commandPublisher.getNumSubscribers() == 0)
	poll_rate.sleep();
  
 
  int count = 0;
  ros::Rate loop_rate(10);
  
  while (ros::ok() && count <= 0)  // a simple hack to publish only once 
 {
   // build msg 
        	 
   sensor_msgs::NavSatFix msg;     		// create new msg obj - type NavSatFix 

   msg.latitude = lat ; 
   msg.longitude = lon;
  
   commandPublisher.publish(msg);     // publish message 

   // Housekeeping: view message content in terminal 	
   ROS_INFO("GPSPublisher: latitude = %f, Longitude = %f", msg.latitude, msg.longitude );       // show string on command prompt

   loop_rate.sleep();
	// change coordinats
    lat+=0.00001; 
    lon+=0.00001;   
    ++count;
   ros::spinOnce();  
}







}
