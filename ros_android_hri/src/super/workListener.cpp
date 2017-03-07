#include "commandListener.cpp"

// Container Program for the Motion commands (GoHome, Work, Resume, Random)

int main(int argc, char **argv) {
	
  ros::init(argc, argv, "workCommandCentre_ROS");		
	
  mainMethod("workCommands"); 

  return 0;
}
