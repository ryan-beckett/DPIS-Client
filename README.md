# Overview

The DPIS (Distributed Police Information System) is a collection of servers that 	make 	criminal and missing persons records available across a wide network of police stations. 	Each police station runs and maintains its own collection of person records which can be 	viewed and modified by officers within the station. These records can also be accessed by 	other officers in distant locations via a distributed computing architecture. A user must log
log into a client program with his or her badge id. From there, he or she has access to the 	station's server where he or she works. The client program allows the officer to create new 	criminal and missing persons records, to edit existing criminal records, and to get a record 	count for the entire set of stations.

# Design

Each client accesses the station server via RMI (Remote Machine Invocation). RMI is a 	piece of software that relies upon TCP/IP to allow clients to access functionality on remote 	servers such that the client interface is a plain Java object. Network communication is 	completely hidden. Station servers communicate with each other over UDP/IP multicast 	using a simple handshaking, request, and response 	protocol. Each server method that is 	exposed to the client is synchronized, so that operations can be done concurrently. Each 	server maintains a hash map of records for fast retrieval.
  
# Screenshot

![Screenshot](https://i.imgur.com/4K5t8AO.png)
