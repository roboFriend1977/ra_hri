#include "ros/ros.h"
#include "geometry_msgs/Pose.h"
#include "geometry_msgs/Point.h"
#include "geometry_msgs/Quaternion.h"

#include <math.h>       /* sqrt */

double x, y, distance;

int main(int argc, char **argv){
    // ROS init
    ros::init(argc, argv, "robotHomePublisher_ROS");
    ros::NodeHandle n;
    ros::Publisher homePublisher = n.advertise<geometry_msgs::Pose>("robotHome", 1);
    ros::Rate loop_rate(10);
    
    // params 	
	ros::NodeHandle np("~"); 
	np.param<double>("x", x, 0); // default x position
	np.param<double>("y", y, 0); // default y position
	 
   
	distance = sqrt(pow(x,2) + pow(y,2));
	
	ROS_INFO("The distance is: %f", distance); 
    

    np.deleteParam("x");  // reset default x for next run
    np.deleteParam("y");  // reset default y for next run

    return 0;
}

