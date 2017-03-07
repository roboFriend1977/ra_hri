#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <string>  	

int status_id, count = 1; 
std::string status_name;

int main(int argc, char **argv)
{  
  
  // ROOS initiate 
  ros::init(argc, argv, "rjsPublisher_ROS");
  ros::NodeHandle n;   
  ros::Rate loop_rate(10);
  ros::Publisher commandPublisher = n.advertise<std_msgs::Header>("robotJobStatus", 1000);
  
  // params 	
  ros::NodeHandle np("~"); 
  np.param<int>("status_id", status_id, 0); // default is stop command
  np.param<std::string>("status_name", status_name, "Robot Status: Awaiting Commands"); // default is stop command
     
  /*	
  // PublishOnce
  ros::Rate poll_rate(100);
  while(commandPublisher.getNumSubscribers() == 0)
	poll_rate.sleep();
// */
		
  // Actual Publish 	
  while (ros::ok()  /*&& count <= 1*/){
	  
	std_msgs::Header msg;                                         // build msg
	msg.stamp = ros::Time::now();                                // time jobstatus is updated
	msg.seq = status_id;  		                                // default to 0
	msg.frame_id = status_name;                                  // default to "Robot Status: Awaiting Commands"
   
	commandPublisher.publish(msg); // publish message

	// Housekeeping: view message content in terminal 	
	//ROS_INFO("rjsPublisher : Current Job [%d]: %s", msg.seq, msg.frame_id.c_str());       // show string on command prompt

	loop_rate.sleep();
	++count;
	ros::spinOnce(); 
   }
   
     np.deleteParam("status_id");  // reset command_id for next run
     np.deleteParam("status_name");  // reset command_id for next run
   
  return 0;
}
