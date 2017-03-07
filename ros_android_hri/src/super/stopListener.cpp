#include "commandListener.cpp"

// Container Program for the Stop command

int main(int argc, char **argv) {
	
  ros::init(argc, argv, "stopCommandCentre_ROS");
	
  mainMethod("stopCommands"); 
	
  return 0;
}
