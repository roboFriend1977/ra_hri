#include "ros/ros.h"
#include "std_msgs/Header.h"
#include <string>

std::string commandTopic;
int command_id;
std::stringstream command_name; 
bool command_valid;

int main(int argc, char **argv)
{
  ros::init(argc, argv, "robotCommandPublisher");
  ros::NodeHandle n;   
   
    ros::NodeHandle np("~");
    np.param<int>("command_id", command_id, 12);
    // update status based on given command_id
    switch (command_id) {
      case 11: command_name << "doGoHome";   break;
      case 12: command_name << "doStop";     break;
      case 13: command_name << "doResume";   break;
      case 14: command_name << "doRoam";     break;
      case 15: command_name << "doRandom";   break;
      case 16: command_name << "doIntermit"; break;
      case 17: command_name << "doRandmitt"; break;      
      default: command_name << "InValid";  	 break; }

    commandTopic = (command_id == 12) ? "stopCommands" : "workCommands";
    ros::Publisher commandPublisher = n.advertise<std_msgs::Header>(commandTopic, 1000);

    // Set PublishOnce
    ros::Rate poll_rate(100);
    while(commandPublisher.getNumSubscribers() == 0)
    	poll_rate.sleep();
    // Send Flags to robot
    if (ros::ok()) {
      std_msgs::Header msg;  // setup msg
	  msg.stamp = ros::Time::now();
	  msg.seq = command_id;
	  msg.frame_id = command_name.str();
	  commandPublisher.publish(msg); } // Publish
  
   /*
       commandTopic = (command_id == 12) ? "stopCommands" : "workCommands";
       ros::Publisher commandPublisher = n.advertise<std_msgs::Header>(commandTopic, 1000);

   	int count = 0;
   	ros::Rate loop_rate(10);

    while (ros::ok() && count <= 0 ){ // a simple hack to publish only once
	  
	std_msgs::Header msg;
	msg.stamp = ros::Time::now();	  					// set time stamp
	msg.seq = command_id;  			  					// set status_id ... default to 0
	msg.frame_id = command_name.str();  				// set status_text ... default to "Robot Status: Awaiting Commands"
	double command_time = msg.stamp.toSec();	

	commandPublisher.publish(msg); 						// publish message   
	ROS_INFO("Current robotCommand: [%d]: %s @%f", msg.seq, msg.frame_id.c_str(), command_time);       // show string on command 


	loop_rate.sleep();
	++count;
	ros::spinOnce();  
  } */

  np.deleteParam("command_id");  // reset command_id for next run

  return 0;
}
