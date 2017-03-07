ros::Publisher rsPublisher;

void updateStatus(int update_id, std::string update_text) {

	// Create msg, and populate
	std_msgs::Header msg;
    msg.stamp = ros::Time::now();
	msg.seq = update_id;
	msg.frame_id = update_text;

	// insure Listener is listening
    ros::Rate poll_rate(100);
	while(rsPublisher.getNumSubscribers() == 0)
		poll_rate.sleep();

	// Publish notification (Publish Once)
	if (ros::ok())	rsPublisher.publish(msg); 	
		
}	

   // robot Status Publisher
   // rsPublisher = n.advertise<std_msgs::Header>("robotStatus", 1);   
