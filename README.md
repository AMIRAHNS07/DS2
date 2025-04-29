📄 Project Description
This project extends Assignment 1 into a distributed Java system using Java RMI and multithreading across two servers.
Each server handles part of the search space and runs multiple threads.

🚀 How to Set Up and Run
1. Prepare Environment
  3 machines (or virtual machines): Server 1, Server 2, and Client.
  Static IPs example:
  Server 1: 192.168.122.101
  Server 2: 192.168.122.102
  Client: 192.168.122.103
  Make sure Java and RMI are installed and configured

2. Compile the Program on Each Machine: javac *.java

3. Start the RMI Registry
  On Server 1:
  rmiregistry &
  java -Djava.rmi.server.hostname=192.168.122.101 CrackingServer

 On Server 2:
  rmiregistry &
  java -Djava.rmi.server.hostname=192.168.122.102 CrackingServer

4. Start the Client
On Client:
java CrackingClient

You will be prompted to input:
  MD5 hash value
  Password length (3–6)
  Number of threads per server (1–10)
