#include "ros/ros.h"
#include "geometry_msgs/Pose.h"
#include "geometry_msgs/Point.h"
#include "geometry_msgs/Quaternion.h"

double x, y;

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
	 
    // Define msg obj and its sub objs
    geometry_msgs::Pose pose;			// Pose
    geometry_msgs::Point point;			// Position
    geometry_msgs::Quaternion quat;		// Orientation
    
    // Position of robotHome
    point.x = x; 
    point.y = y; 
    point.z = 0.0; 
    
    // Default orientation (edit later) 
    quat.x = 0.0; 
    quat.y = 0.0; 
    quat.z = 0.0; 
    quat.w = 1;
    
    // set Position & orientation of Pose
    pose.position = point; 
    pose.orientation = quat; 
        
      
    // Publish Pose (publishOnce)
    ros::Rate poll_rate(1000);
    while(homePublisher.getNumSubscribers() == 0)
		poll_rate.sleep();

	if (ros::ok())  {

	homePublisher.publish(pose);   // publish initial Position to robot

	ROS_INFO("I published this Home: \nPosition: [%f, %f, %f] \nOrientation: [%f, %f, %f, %f]", // Log
    		   pose.position.x, pose.position.y, pose.position.z, pose.orientation.x, pose.orientation.y, pose.orientation.z, pose.orientation.w);
	}

    np.deleteParam("x");  // reset default x for next run
    np.deleteParam("y");  // reset default y for next run

    return 0;
}

