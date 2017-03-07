#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <string>  	

int count = 1; 
std::string msg_con;

int main(int argc, char **argv)
{  
  
  // ROOS initiate 
  ros::init(argc, argv, "noticePublisher_ROS");
  ros::NodeHandle n;   
  ros::Rate loop_rate(10);
  ros::Publisher commandPublisher = n.advertise<std_msgs::Header>("commandNotice", 1);
  
  // params 	
  ros::NodeHandle np("~"); 
  np.param<std::string>("msg_con", msg_con, ""); // default is stop command
     
  // PublishOnce
  ros::Rate poll_rate(100);
  while(commandPublisher.getNumSubscribers() == 0)
	poll_rate.sleep();
		
  // Actual Publish 	
  while (ros::ok()  && count <= 1){
	  
	std_msgs::Header msg;       	// build msg
	
	msg.frame_id = msg_con;     // default to "Robot Status: Awaiting Commands"
   
	commandPublisher.publish(msg); // publish message

	// Housekeeping: view message content in terminal 	
	//ROS_INFO("noticePublisher : Current message is [%d]: %s", msg.seq, msg.frame_id.c_str());       // show string on command prompt

	loop_rate.sleep();
	++count;
	ros::spinOnce(); 
   }
   
    np.deleteParam("msg_con");  // reset param
   
  return 0;
}
