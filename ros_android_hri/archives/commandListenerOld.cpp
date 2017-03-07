#include "ros/ros.h"
#include "std_msgs/Header.h"
#include "geometry_msgs/Twist.h"
#include "nav_msgs/Odometry.h" // to capture robot speed for confirmation
#include <sstream>  
#include <string>  
#include <ctime>
#include <ros/callback_queue.h>
#include <ros/callback_queue_interface.h>

// Definitions

ros::Publisher rjsPublisher, rmvPublisher, noticePublisher; 
ros::Subscriber commandSubscriber, robotSpeedListener; 
std::string task_name;
int input_id, task_id; 
double command_time, task_time, response_time, linearSpeed, angularSpeed;
bool robotStopped=true, randomPath=false, robotStillMoving=true;

// Utility methods

void sendNotice(const std::string& conMsg){	
	// send a header confirmation msg back to rpPublisher  
	
	// PublishOnce - first wait for subscriber to connect
	ros::Rate poll_rate(100);
	while(noticePublisher.getNumSubscribers() == 0)
		poll_rate.sleep();
	
	int count = 0;
	ros::Rate loop_rate(10);
	while (ros::ok() && count <= 0 ){ // publish once !

		std_msgs::Header msg;
		
		msg.stamp = ros::Time::now();
		msg.seq = 1;
		msg.frame_id = conMsg;      // publish confirmation msg to android
		
		noticePublisher.publish(msg);
		loop_rate.sleep();
		++count;
		ros::spinOnce();     
     }
	
	
}

void extractMsgInfo(const std_msgs::Header::ConstPtr& msg){
	// capture msg data
	task_name = msg->frame_id.c_str(); 		// command_name (string) 
	input_id = (msg->seq)/10; 				// input_id (first digit of command_id)
	task_id = (msg->seq)%10; 				// task_id (second digit of command_id)
	command_time = msg->stamp.toSec(); 		// command_time (time when command was initiated in android) 
	
	// Time Analysis
	task_time = ros::Time::now().toSec();
	response_time = task_time - command_time;
	ROS_INFO("Time Analysis: \ntaskID: \t%i,\ninputID: \t%i, \nrespT:\t%f ",
	                          task_id,      input_id,        response_time);
}

void updateJobStatus(int task_id, std::string task_name) { 

	// give it time for command to take hold  (why do i ned
	ros::Rate wait_rate(1);
    wait_rate.sleep();  
    		
	ros::Rate loop_rate(10);
	while (ros::ok()){
     std_msgs::Header msg;          // build msg
     msg.stamp = ros::Time::now();  // time jobstatus is updated
     msg.seq = task_id;  		    // default to 0
     msg.frame_id = task_name;      // default to "Robot Status: Awaiting Commands"
     rjsPublisher.publish(msg); 	// robot job status updates only task, not source of input 	
     loop_rate.sleep();
     ros::spinOnce();     
     }
}	
/*
void updateJobStatus(int task_id, std::string task_name) { 
	
	std::stringstream ss;  // xterm -e 
	ss << "rosrun ros_android_hri rjsPublisher"; 
	ss << " _status_id:=" << task_id;
	ss << " _status_name:=" << task_name;
	
	system(ss.str().c_str());
}	
*/

// Robot Commands

void commandGoHome(const std_msgs::Header::ConstPtr& msg){
	extractMsgInfo(msg);  // capture msg data and perform time analysis logs 
	
	// For now, this looks redundant, but later (in paper 5) each of these functions will be unique
	// peform required task 
	ROS_INFO("robot job status: %s", task_name.c_str());  // placeholder for now
	
	// update robot job status 
	updateJobStatus(task_id, task_name);   // later, infuse this in robot task, use a While loop, while(tasks is being performed) update rjs 
	// Logging 
}

void commandWork(const std_msgs::Header::ConstPtr& msg){
	extractMsgInfo(msg);  // capture msg data and perform time analysis logs 

    // Send Work command to robot = patrol given path, from starting point
	system("rosrun ros_android_hri robotPatrol.py");
	//ros::Rate wait_rate(10); // give it some time to take hold
    //wait_rate.sleep();  
    
    updateJobStatus(task_id, task_name); 
    
/*	if (!robotStopped){ // if robot is indeed moving 
		updateJobStatusPublisher(task_id, task_name); 
		sendNotice("Command Successfull: Robot is Working");
	} else {
		sendNotice("Command Failed: Robot not moving");
	}
	*/
}

void commandResume(const std_msgs::Header::ConstPtr& msg){
	extractMsgInfo(msg);  // capture msg data and perform time analysis logs 

    // Send Work command to robot = patrol given path, from last completed point
	system("rosrun ros_android_hri robotPatrol.py _resume_path:=true ");
//	ros::Rate wait_rate(10); // give it some time to take hold
//    wait_rate.sleep();  
	
	updateJobStatus(task_id, task_name); 
	
/*	if (!robotStopped){ // if robot is indeed moving 
		
		sendNotice("Command Successfull: Robot Resumed Work");
	} else {
		sendNotice("Command Failed: Robot not moving");
	}*/
}

void commandStop(const std_msgs::Header::ConstPtr& msg){
	extractMsgInfo(msg);  // capture msg data and perform time analysis logs 
 
    // Send Stop command to robot
	system("rosnode kill robotPatrol_ROS"); // gets stuck here 

	if (robotStopped){ // if robot speed is really 0, update status and notify us	
		sendNotice("Command Successfull: Robot Stopped");
		updateJobStatus(task_id, task_name); 
	} else {
		sendNotice("Robot NOT stopped");
		sendNotice("Command Failed: Robot Still moving");
	} 
};

// CallBacks (ROS listeners) 

void commandCallback(const std_msgs::Header::ConstPtr& msg){
	switch((msg->seq)%10) { // (msg->seq)%10 = task_id
		case 1: commandGoHome(msg); break;
		case 2: commandStop(msg); break; 
		case 3: commandResume(msg); break;
		case 4: commandWork(msg); break;
		default: break;	} }
		
void speedCallback(const nav_msgs::Odometry::ConstPtr& msg) {
	
	linearSpeed = (msg->twist.twist.linear.x)*100;
    angularSpeed = (msg->twist.twist.angular.z)*100;
    
    if (linearSpeed == 0.0 && angularSpeed == 0.0 )	robotStopped = true; 
	else robotStopped = false; 
}

// Main Program

int main(int argc, char **argv) {
	
   ros::init(argc, argv, "robotCommandCentre_ROS");
  
  // Concurrency 
  ros::NodeHandle nh1, nh2, nh3, nh4;  		
  ros::CallbackQueue q1, q2, q3, q4; 	
  
  nh1.setCallbackQueue(&q1);	
  nh2.setCallbackQueue(&q2);
  nh3.setCallbackQueue(&q3);	
  nh4.setCallbackQueue(&q4);
  
  commandSubscriber = nh1.subscribe("robotCommands", 1000, commandCallback); // for receiving commands 
  robotSpeedListener = nh2.subscribe("/odom", 1000, speedCallback);			 // speed listener, for confirmation
  
  while (ros::ok()){
	q1.callAvailable(ros::WallDuration());
	q2.callAvailable(ros::WallDuration());
  
    noticePublisher = nh3.advertise<std_msgs::Header>("commandNotice", 1000); // publish notices (for updates and confirmations) 
	rjsPublisher = nh4.advertise<std_msgs::Header>("robotJobStatus", 1000);   // robot JobStatus Publisher

	ros::spinOnce();
  }
  
   /*  
   
  // Concurrency
  ros::NodeHandle nh1, nh2;  		
  ros::CallbackQueue q1; 	
  
  nh1.setCallbackQueue(&q1);	
  
  commandSubscriber = nh1.subscribe(listenerTopic, 1000, commandCallback); // for receiving commands 
  
  while (ros::ok()){
	q1.callAvailable(ros::WallDuration(1.0));
	rjsPublisher = nh2.advertise<std_msgs::Header>("robotJobStatus", 1000);   // robot JobStatus Publisher
	ros::spinOnce();
  }  
// */ 
  
  return 0;
}
