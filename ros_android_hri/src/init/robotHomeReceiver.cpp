#include <iostream>
#include <fstream>
#include "ros/ros.h"
#include "geometry_msgs/Pose.h"
#include "std_msgs/Header.h"

#include "../state/rsPublisher.cpp"  // robot Status Publisher (go one folder up)

void homeCallback(const geometry_msgs::Pose::ConstPtr& homePose)
{   
	// write Path data into file
	std::ofstream homeFile; 
	homeFile.open("catkin_ws/src/ros_android_hri/initial/robotHome.txt"); 
	if (homeFile.is_open()) {
		homeFile << homePose->position.x << "\n" ;
		homeFile << homePose->position.y << "\n" ;
		homeFile << homePose->position.z << "\n" ;
		homeFile << homePose->orientation.x << "\n" ;
		homeFile << homePose->orientation.x << "\n" ;
		homeFile << homePose->orientation.x << "\n" ;
		homeFile << homePose->orientation.w << "\n" ;		
    }
   	homeFile.close(); 

	// housekeeping  
    ROS_INFO("I Captured this HomePose: \nPosition: [%f, %f, %f] \nOrientation: [%f, %f, %f, %f]", 
				   homePose->position.x, homePose->position.x, homePose->position.x, 
				   homePose->orientation.x, homePose->orientation.y, homePose->orientation.z, homePose->orientation.w); 
				   
	// update status (inform android)
	updateStatus(0,"New Robot Home Captured");
			
	return;
}

int main(int argc, char **argv)
{
  ros::init(argc, argv, "robotHomeReceiver_ROS");
  ros::NodeHandle n;

  while (ros::ok())
  {
	ros::Subscriber homeSub = n.subscribe("robotHome", 100, homeCallback);  // array captured   
	rsPublisher = n.advertise<std_msgs::Header>("robotStatus", 1);   // robot JobStatus Publisher
	ros::spin();
  }
  return 0;
}



