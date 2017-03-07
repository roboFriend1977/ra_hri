#include "ros/ros.h"
#include "sensor_msgs/CompressedImage.h"

int main(int argc, char **argv)
  {
  
  // initiate 
  ros::init(argc, argv, "LiveFeedPublisher_ROS");
  ros::NodeHandle n;   
  ros::Publisher commandPublisher = n.advertise<sensor_msgs::CompressedImage>("camera/rgb/image_color/compressed", 1000);
   
 
  int count = 0;
  ros::Rate loop_rate(10);
  
  while (ros::ok())  
 {
          	 
   sensor_msgs::CompressedImage msg;     		
   commandPublisher.publish(msg); 

   loop_rate.sleep();

    ++count;
   ros::spinOnce();  
  }
}
