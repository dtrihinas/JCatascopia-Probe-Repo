JCatascopia Monitoring Probe Library
====================================

Licence
---------------
The complete source code of the JCatascopia Monitoring System is open-source and available to the community under the [Apache 2.0 licence](http://www.apache.org/licenses/LICENSE-2.0.html)

JCatascopia Monitoring Probes developed by the University of Cyprus are as well made available to the community under the [Apache 2.0 licence](http://www.apache.org/licenses/LICENSE-2.0.html)

Getting Started
---------------
JCatascopia Monitoring Probes are the actual metric collectors for the JCatascopia Monitoring System. Monitoring Probes are managed on each node by the respected [JCatascopia Monitoring Agent](https://github.com/CELAR/cloud-ms/tree/master/JCatascopia-Agent) residing on that node. 

- First install a JCatascopia Monitoring Agent to the node which will be monitored. For a Monitoring Agent installation guide, please click [here](https://github.com/CELAR/cloud-ms/tree/master/JCatascopia-Agent) (Note: For CELAR users, JCatascopia Monitoring Agents are already installed on each VM).

- After a JCatascopia Monitoring Agent has been installed, you can then configure which Monitoring Probes to use. JCatascopia Monitoring Agents are bundled with a number of default system-level Monitoring Probes to monitor Linux nodes (e.g. CPU, Memory, Network, Disk I/O Probe, etc.). The list of default Monitoring Probes along with their source code can be found [here](https://github.com/dtrihinas/JCatascopia-Probe-Library/tree/master/ProbePack/src/main/java/eu/celarcloud/jcatascopia/probepack/probeLibrary).

**Add a JCatascopia Monitoring Probe Externally**

- To add a Monitoring Probe externally, simply select and download a Monitoring Probe (either source code or packaged as a JAR) from the available ones in the [JCatascopia Monitoring Probe Library](https://github.com/dtrihinas/JCatascopia-Probe-Library/). 

- Edit (and save) the Monitoring Agent config file to include the name and path location of the Monitoring Probe, as follows:

```shell
probes_external=ExampleProbe,/usr/local/bin/ExampleProbe.jar
```

- To add more than one Monitoring Probes, simply seperate each Probe declaration with a semi-colon (;) as follows:

```shell
probes_external=ExampleProbe,/usr/local/bin/ExampleProbe.jar; CassandraProbe,/opt/myCassandra/CassandraProbe.jar
```

- restart the JCatascopia Monitoring Agent (if installed as a service):

```shell
service JCatascopia-Agent restart
```

- JCatascopia Monitoring Probes can also be deployed at runtime without restarting the Monitoring Agent via a deployProbe Agent API call (for more details see [here]()).

**Developing Custom Monitoring Probes**

Monitoring Probes are developed by either the JCatascopia Probe Pack and JCatascopia Probe API.

- Download the JCatascopia Probe Package source code or JAR package located [here] (https://github.com/dtrihinas/JCatascopia-Probe-Library/tree/master/ProbePack)

- Create a new Java project in your favorite IDE and import the JCatascopia Probe Package to your build path

- The full JCatascopia Probe API can be [here](http://www.celarcloud.eu/wp-content/uploads/2013/11/Cloud-Monitoring-Tool-V1.pdf). However, for most novice developers they can ommit looking at the API and develop Monitoring Probes through examples.

- To create a Monitoring Probe, you must extend the abstract Probe class, define metrics to be collected in the constructor and override the collect method. Take a look at the Example Probe implementation [here](https://github.com/dtrihinas/JCatascopia-Probe-Library/blob/master/ExampleProbe/src/main/java/ExampleProbe.java) to understand how easy it is to create Monitoring Probes.

- Once you are finished package the Monitoring Probe as a JAR and follow the Monitoring Probe deployment steps.

Note: Please consider uploading your Monitoring Probes to our Library so it can continue to grow! Every contributor is welcome!

Contact Us
---------------
Please contact Demetris Trihinas trihinas{at}cs.ucy.ac.cy for any issue

Publications
---------------
For any research work in which JCatascopia is used, please cite the following article:

"JCatascopia: Monitoring Elastically Adaptive Applications in the Cloud", D. Trihinas and G. Pallis and M. D. Dikaiakos, "14th IEEE/ACM International Symposium on Cluster, Cloud and Grid Computing" (CCGRID 2014), Chicago, IL, USA 2014
http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6846458&isnumber=6846423

Website
---------------
[http://linc.ucy.ac.cy/CELAR/jcatascopia](http://linc.ucy.ac.cy/CELAR/jcatascopia)
