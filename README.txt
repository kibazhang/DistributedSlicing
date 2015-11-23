Erik Schilling eps528
Zaewei Zhang

This is the README for directions to run the online and offline version of the centralized algorithm and the online version of the distributed algorithm.

Offline Centralized
	This algorithm is designed to run with exactly 3 processes and a central slicer. So it is best to run CentralSlicer with output1.txt, output2.txt and output3.txt where they are in the project (which is at the same level as the src and bin directories).

Online Centralized
	You need to run the CentralSlicer first. Then, create the three processes that will run. Process requires 2 command line arguments. The first is the list of server hostnames and port numbers. The first line should be the hostname and port for the CentralSlicer. The next three lines are the hostname and port for the three processes. The second command line argument is the unique ID of the process (numbered 1-3).

	Once the four processes are running, then you can enter input in the form x,y where x is the destination for the message and y is the value that will be added to the local variable of the destination process.

	These messages will automatically be sent to the CentralSlicer and it will output updates as they are calculated.

Online Distributed

	This algorithm requires the same command line input and runtime input as the Online Centralized algorithm.