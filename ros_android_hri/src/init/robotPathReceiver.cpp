#include <iostream>
#include <fstream>
#include "ros/ros.h"
#include "std_msgs/Float64MultiArray.h"
#include "std_msgs/Header.h"
#include <algorithm>  // for std::fill_n

#include "../state/rsPublisher.cpp"  // robot Status Publisher (go one folder up)

void arrayCallback(const std_msgs::Float64MultiArray::ConstPtr& array)
{   
	int dataSize = array->data.size();
	int numbPoints = (dataSize)/2;
	
	// write Path data into file
	std::ofstream pointsFile; 
	pointsFile.open("catkin_ws/src/ros_android_hri/initial/deliveryPoints.txt"); 
	if (pointsFile.is_open()) {
		for(int i = 0; i < dataSize; i++)
			pointsFile << array->data[i] << "\n" ;
    }
   	pointsFile.close(); 

	int poseResults[numbPoints]; std::fill_n(poseResults, numbPoints, 0); // init PoseStatus Array with 0, for Resume Task
	// write PoseResults Array into file
	std::ofstream poseResultsFile; 
	poseResultsFile.open("catkin_ws/src/ros_android_hri/initial/completionStatus.txt"); 
	if (poseResultsFile.is_open()) {
		for(int i = 0; i < numbPoints; i++)
			poseResultsFile << poseResults[i] << "\n" ;
    }
   	poseResultsFile.close(); 
	
	// write PoseStatus Array in file 2
		
	// housekeeping
    printf("I captured this array: \n");        
    for (int i=0; i<numbPoints; i++) {
		ROS_INFO("PoseStatus: %i - X: %f ", poseResults[i], array->data[i]);  // 1  
	}
	
	// update status (inform android)
	updateStatus(0, "New Robot Path Captured"); // 0 => configuration update
			
	return;
}

int main(int argc, char **argv)
{
  ros::init(argc, argv, "robotPathReceiver_ROS");
  ros::NodeHandle n;

  while (ros::ok())
  {
	ros::Subscriber pathSub = n.subscribe("robotPath", 100, arrayCallback);  // array captured   
	rsPublisher = n.advertise<std_msgs::Header>("robotStatus", 1);   // robot Status Publisher
	ros::spin();
  }
  return 0;
}



