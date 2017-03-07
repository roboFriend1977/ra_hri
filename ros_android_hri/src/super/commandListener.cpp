#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <string>  
#include <ctime>
#include "../state/rsPublisher.cpp"  // robot Status Publisher (go one folder up)

void commandCallback(const std_msgs::Header::ConstPtr& msg){

	// capture robot command info
	std::string task_name = msg->frame_id.c_str();                                                      	// command_name (string)
	int input_id = (msg->seq)/10; int task_id = (msg->seq)%10;                                       	    // input_id (first digit of command_id)			                                                                   					 // task_id (second digit of command_id)
	double command_time = msg->stamp.toSec(); 		                                                        // command_time (time when command was initiated in android)
	// perform Time analysis
	double task_time = ros::Time::now().toSec();
	double response_time = task_time - command_time;
	ROS_INFO("Time Analysis: \ntaskID: \t%i,\ninputID: \t%i, \nrespT:\t%f ",
	                          task_id,      input_id,        response_time);
	// Call autonomous class with given flags
	switch(task_id) { // = (msg->seq)%10
		case 1: system("rosrun ros_android_hri goHomeRobot.py");               			break;
	    case 2: system("rosnode kill robotRoam_ROS");	           						break;
	    case 3: system("rosrun ros_android_hri robotWork.py _res:=true");            	break;
		case 4: system("rosrun ros_android_hri robotWork.py"); 		               		break;
		case 5: system("rosrun ros_android_hri robotWork.py _ran:=true");            	break;
		case 6: system("rosrun ros_android_hri robotWork.py _int:=true");            	break;
		case 7: system("rosrun ros_android_hri robotWork.py _ran:=true _int:=true"); 	break;}
	// Update robot Status
	updateStatus(task_id, task_name);



	// Bug: update status does not work if placed after robot commands
	// might require cocurrency, or bahaviors ... for future work

}
		
void mainMethod(const std::string& listenerTopic){
  
// ros::init(argc, argv, "node_name"); Defined in each container program
   ros::NodeHandle n; 

   while (ros::ok()){
		ros::Subscriber commandSubscriber = n.subscribe(listenerTopic, 1, commandCallback); // for receiving commands 
		rsPublisher = n.advertise<std_msgs::Header>("robotStatus", 1);   // robot JobStatus Publisher
		ros::spin();
	}	 
}
