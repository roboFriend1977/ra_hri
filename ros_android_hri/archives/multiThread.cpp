#include <ros/ros.h>
#include <ros/callback_queue.h>
#include <ros/callback_queue_interface.h>
#include "std_msgs/Header.h"

#include <std_msgs/Empty.h>

void callback_1(const std_msgs::Header::ConstPtr& msg){
	
	ROS_INFO("Callback [%i]: %s", msg->seq, msg->frame_id.c_str()); 
}

void callback_2(const std_msgs::Header::ConstPtr& msg){
	
	ROS_INFO("Callback [%i]: %s", msg->seq, msg->frame_id.c_str()); 
}

void callback_3(const std_msgs::Header::ConstPtr& msg){
	
	ROS_INFO("Callback [%i]: %s", msg->seq, msg->frame_id.c_str()); 
}



int main(int argn, char* args[]){
	
    ros::init(argn, args, "multiThread_subscriber");
    
    ros::NodeHandle nh_1, nh_2, nh_3, nh_4, nh_5, nh_6;

    ros::CallbackQueue queue_1, queue_2, queue_3, queue_4, queue_5, queue_6;

    nh_1.setCallbackQueue(&queue_1);
    nh_2.setCallbackQueue(&queue_2);
    nh_3.setCallbackQueue(&queue_3);
    nh_4.setCallbackQueue(&queue_4);
    nh_5.setCallbackQueue(&queue_5);
    nh_6.setCallbackQueue(&queue_6);

    ros::Subscriber s_1 = nh_1.subscribe("header_1", 1, callback_1);
    ros::Subscriber s_2 = nh_2.subscribe("header_2", 1, callback_1);
    ros::Subscriber s_3 = nh_3.subscribe("header_3", 1, callback_1);
    
    ros::Subscriber s_4 = nh_4.subscribe("headerOnce_1", 1, callback_1);
    ros::Subscriber s_5 = nh_5.subscribe("headerOnce_2", 1, callback_2);
    ros::Subscriber s_6 = nh_6.subscribe("headerOnce_3", 1, callback_3);
    

    //pthread_t id_1, id_2, id_3;

	while (ros::ok()){
		queue_1.callAvailable(ros::WallDuration());
		queue_2.callAvailable(ros::WallDuration());
		queue_3.callAvailable(ros::WallDuration());
		queue_4.callAvailable(ros::WallDuration());
		queue_5.callAvailable(ros::WallDuration());
		queue_6.callAvailable(ros::WallDuration());  

		ros::spinOnce();
		}

    ROS_INFO("Done!");
    return 0;   
}
