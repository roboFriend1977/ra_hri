#!/usr/bin/env python

""" robotPatrol.py - Version 1.1 2017-1-15

    given an array of points indicating a robot path, 
    execute move_goal to these points and visualize robot movements

    expanded from move_base_square.py - Version 1.1 2013-12-20
    created by Patrick Goebel from the book ROS By Example 1 (indigo)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.5
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details at:
    
    http://www.gnu.org/licenses/gpl.htmlPoint
      
"""

import rospy
import actionlib
from actionlib_msgs.msg import *
from geometry_msgs.msg import Pose, Point, Quaternion, Twist
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
from tf.transformations import quaternion_from_euler
from visualization_msgs.msg import Marker
from math import radians, pi, pow, sqrt
from random import sample
import math

"""
	Goal maybe given in the following methods: 

	- Goal pose (positions and orientation) relative to 0,0 postion on robot map ==> 
		postion ready
		orientation ready
		
	- x,y positions, relative to 0,0 postion on robot map ==> 
		postion ready
		orientation set to 0,0,0
		
	- x,y GPS coordinates, ==> 
		postion need to be converted to be relative to 0,0 position on robot map
		orientation set to 0,0,0
			
	init_goal is needed to process incoming data (or is it hard-coded?) 
"""

class GoHomeRobot():		
    def __init__(self):	
		
		# rospy housekeeping
        rospy.init_node('robotPatrol_ROS', anonymous=False)
        rospy.on_shutdown(self.shutdown)

		# init program
        self.init_goal()				# define & format target goal       
        self.init_visualization()		# Visualize each Pose with a square & arrow
        self.init_robot_motion()		# Setup robot with the move_base server
            
        # move robot
        if not rospy.is_shutdown(): 
						
            self.markers_pub.publish(self.homeVis)		# Visualize goal		 
            self.move_base.send_goal(self.homeGoal)		# Send robot to goal			 
            
            # define waiting period
            in_time = self.move_base.wait_for_result(rospy.Duration(60))
            
            # check goal status and update user
            if not in_time:
				self.move_base.cancel_goal()
				rospy.loginfo("Goal could not be reached: Timed out!")
            else:
				state = self.move_base.get_state()
				if state == GoalStatus.SUCCEEDED:
					rospy.loginfo("Goal reached!")

    def init_goal(self):
		rospy.loginfo("initiating goal ... ")
		homePose = list()		
		
		# Read raw coordinates of Home Position from File 
		homePose_file = open('catkin_ws/src/ros_android_hri/initial/robotHome.txt','r')	# read only (can be changed if needs be) 
		for line in homePose_file: homePose.append(float(line))
		homePose_file.close()
		
		# assign to Pose
		point = Point(homePose[0],homePose[1], homePose[2])
		quat = Quaternion(homePose[3],homePose[4],homePose[5],homePose[6])
		self.homePose = Pose(point, quat)
		
		# setup goal for move_base
		self.homeGoal = MoveBaseGoal()	
		self.homeGoal.target_pose.header.frame_id = 'map'		 	# define frame ref (map)
		self.homeGoal.target_pose.header.stamp = rospy.Time.now() 	# define time (now)
		self.homeGoal.target_pose.pose = self.homePose	       	 	# define goal (next waypoint)
		
    def init_visualization(self):
		# define eraser marker (to remove markers after target reached)
        self.eraser = Marker()
        self.eraser.action = 3
        
        # Init home visualization 
        self.homeVis = Marker()
        self.homeVis.type = Marker.CYLINDER
        self.homeVis.id = 0        
        self.homeVis.ns = 'homepoint'
        self.homeVis.lifetime = rospy.Duration(0)	# 0 for permenant
        self.homeVis.action = Marker.ADD
        # visuals - size
        self.homeVis.scale.x = 0.15		# dia in x
        self.homeVis.scale.y = 0.15		# dia in y, must equal x for circle
        self.homeVis.scale.z = 0.05		# height of cylinder
        # visuals - colors        
        self.homeVis.color.r = 1.0 
        self.homeVis.color.g = 0.7
        self.homeVis.color.b = 1.0
        self.homeVis.color.a = 1.0
        # Header settings
        self.homeVis.header.frame_id = 'odom'
        self.homeVis.header.stamp = rospy.Time.now()
        # Pose (set equal to goal pose) 
        self.homeVis.pose  = self.homePose
      
        # Start visualization publisher	(to publish squares & arrows) 
        self.markers_pub = rospy.Publisher('waypoint_markers', Marker, queue_size=5)

    def init_robot_motion(self):
        #self.cmd_vel_pub = rospy.Publisher('cmd_vel', Twist, queue_size=5)			# start motion publisher
        self.cmd_vel_pub = rospy.Publisher('/cmd_vel_mux/input/navi', Twist, queue_size=5)	# actual robot
        self.move_base = actionlib.SimpleActionClient("move_base", MoveBaseAction)		# start a client to the move_base server
        self.move_base.wait_for_server(rospy.Duration(60))					# allow time for action server to start
        rospy.loginfo("Moving robot ... ")

    def shutdown(self):
        # inform user
        rospy.loginfo("Stopping robot ...")

        # Cancel any active goals & stop the robot
        self.move_base.cancel_goal()
        self.cmd_vel_pub.publish(Twist())
        self.markers_pub.publish(self.eraser)	# erase all markers
        			
if __name__ == '__main__':
	try:
		GoHomeRobot()
	except rospy.ROSInterruptException:
		rospy.loginfo("Robot Motion ended")
