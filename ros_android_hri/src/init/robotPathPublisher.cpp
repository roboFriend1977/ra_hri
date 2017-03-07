#include "ros/ros.h"
#include "std_msgs/Float64MultiArray.h"
#include "std_msgs/MultiArrayDimension.h"
#include <iostream>
#include <vector>
#include <array>

double dRand(double fMin, double fMax) {
    double f = (double)rand() / RAND_MAX;
    return fMin + f * (fMax - fMin);
}

//int numbPoints = 5;
int ArrSize, path_id; 
//double pointsArray[16]; // 16 for gen sim, 12 for mte2017

int main(int argc, char **argv){
    // ROS housekeeping
    ros::init(argc, argv, "robotPathPublisher_ROS");
    ros::NodeHandle n;
    ros::Publisher pathPublisher = n.advertise<std_msgs::Float64MultiArray>("robotPath", 1);
    ros::Rate loop_rate(10);
    
      // params 	
	ros::NodeHandle np("~"); 
	np.param<int>("id", path_id, 0); // default is stop command
      
   // switch(path_id){ // a specific path for each purpose
	//	case 0:	
		//	double pointsArray[] = {0.5,-0.5, 1.0,0.0, 1.5,0.5, 1.0,1.0, 0.5,1.5, 0.0,1.0, -0.5,0.5, 0.0,0.0};  
	//		break; // general sim (default) 
	//	case 1: 
	//		double pointsArray[] = {-9.7, 10.0, 0.0,0.0};	
		//	break; 	// Office - Delivery Task 
	//	case 2: 
			double pointsArray[] = {-5.1,0.3, -3.5,5.4, -4.2,7.8, -7.7,5.8, -10.3, 9.9, -12.0,7.7, -11.3,3.6, 
								   -15.3, 1.8, -12.7, -1.9, -14.0, -4.4, -10.0,-6.8, -6.1,-6.0, -5.1,0.3};	
			//break; 	// Office - Tour Guide
		
		/*
		case 1: 
		double pointsArray[] = {1.264, -1.419, 2.814, -0.185, 2.658, 2.467, 1.481, 3.790, 0.901, 1.813}; 
		break; // mte2017, path 1	 
		case 2: 
		double pointsArray[] = {1.077, -2.765, 3.409, 0.244, 2.041, 1.553, 1.700, 3.577, -1.745, 2.478, 0.161, 0.526}; 
		break; // mte2017, path 2	
		*/
		
//	}
																	
    ArrSize = sizeof(pointsArray)/sizeof(pointsArray[0]);
	std::vector<double> vec (pointsArray, pointsArray + ArrSize);                  // assign array to vector
        
    // construct ROS Array msg type
    std_msgs::Float64MultiArray msgMatrix;
    msgMatrix.layout.dim.push_back(
                    std_msgs::MultiArrayDimension());
    // setup array
    msgMatrix.layout.dim[0].label = "locations";
    msgMatrix.layout.dim[0].size = ArrSize;
    msgMatrix.layout.dim[0].stride = 1;           
    msgMatrix.layout.data_offset = 0;
    // set array data (raw locations)
    msgMatrix.data = vec;
    // Set publishOnce
    ros::Rate poll_rate(1000);
    while(pathPublisher.getNumSubscribers() == 0)
		poll_rate.sleep();
    // publish array to robot
    if (ros::ok())  pathPublisher.publish(msgMatrix);

    /*

        // version two
        if (ros::ok())  {

            pathPublisher.publish(msgMatrix);   // publish array to robot

    	    ROS_INFO("I published this array:"); // Log
    	    int counter=1;
            for (int j=0; j<ArrSize; j++){
        	    ROS_INFO("%f, ",msgMatrix.data[j]);
        		//ROS_INFO("Position (%d): {%f, %f}",(counter),msgMatrix.data[j],msgMatrix.data[j+1]);
        		counter++;
            	}

        }


	int i = 0, counter=1;
    while (ros::ok() && i == 0) // publish just once
    {
        pathPublisher.publish(msgMatrix);

		ROS_INFO("I published this array:");
        for (int j=0; j<ArrSize; j++){
			ROS_INFO("%f, ",msgMatrix.data[j]);
			//ROS_INFO("Position (%d): {%f, %f}",(counter),msgMatrix.data[j],msgMatrix.data[j+1]);
			counter++;
			}

        loop_rate.sleep();
        i++;
    }
    */

	np.deleteParam("id");  // reset path_id for next run

    return 0;
}

