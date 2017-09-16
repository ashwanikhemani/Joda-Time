Joda-Time
---------

Joda-Time provides a quality replacement for the Java date and time classes.
The design allows for multiple calendar systems, while still providing a simple API.
The 'default' calendar is the ISO8601 standard which is used by XML.
The Gregorian, Julian, Buddhist, Coptic, Ethiopic and Islamic systems are also included, and we welcome further additions.
Supporting classes include time zone, duration, format and parsing. 

Joda-Time is licensed under the business-friendly [Apache 2.0 licence](http://www.joda.org/joda-time/license.html).


### Documentation
Various documentation is available:

* The [home page](http://www.joda.org/joda-time/)
* Two user guides - [quick](http://www.joda.org/joda-time/quickstart.html) and [full](http://www.joda.org/joda-time/userguide.html)
* The [Javadoc](http://www.joda.org/joda-time/apidocs/index.html)
* The [FAQ](http://www.joda.org/joda-time/faq.html) list
* Information on [downloading and installing](http://www.joda.org/joda-time/installation.html) Joda-Time including release notes


### Releases
[Release 2.9.9](http://www.joda.org/joda-time/download.html) is the current latest release.
This release is considered stable and worthy of the 2.x tag.
It depends on JDK 1.5 or later.


### Implementation of Junit test cases details:
                                 
Existing junit test cases were checked and run to the functionality of the joda time library  
   
   
###Implementation of build scripts:

1. Maven : The joda time library taken had maven build configuration 

2. Gradle : The build.gradle and settings.gradle files were create to configure the build for gradle 
    The following tasks were run to test the project 


   Performing Tests:
   
        gradle clean
        
        gradle test
   
   Compiling classes:
   
        gradle assemble
    
   Creating build:
    
        gradle build
      

3. SBT : The build.sbt file is created to configure the build for SBT project 
    The following tasks were run to test the project 

    Performing Tests:
       
            sbt clean
            
            sbt test
       
    Compiling classes:
       
            sbt compile
        
    Creating build:
        
            sbt package
        
4. Running the project using jar file :

Create one utility program to test the library .
The driver program written to test the joda time library is below .
The import for the corresponding class used was done to make use of that functionality from the library .

    
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.IOException;

public class TestJoda {

    public static void main(String [] args) throws IOException , InterruptedException{

        LocalDate date = new LocalDate(2017, 9, 15);

        int year = date.getYear();
        int month = date.getMonthOfYear();
        int day = date.getDayOfMonth();

        System.out.println(day+":"+month+":"+ year);
        for (int i = 0;i < 100;i++) {
            //Pause for 5 seconds
            Thread.sleep(5000);
            //Print a message
            System.out.println(day+":"+month+":"+ year);
        }


    }
}


The above driver program make use of the localdate class from joda time to find out the local date and the program makes use of java sleep functionality so that the monitoring of program can be done with help of java monitoring tools .
    
5. Monitoring Tools:

Screenshot attached in the java monitoring document file 

Jcmd : 

The jcmd utility is used to send diagnostic command requests to the JVM, where these requests are useful for controlling Java Flight Recordings, troubleshoot, and diagnose JVM and Java Applications. It must be used on the same machine where the JVM is running, and have the same effective user and group identifiers that were used to launch the JVM.

The below functionality to print thread contents was tested with jcmd : 
•	Print all threads with stack traces
jcmd <process id/main class> Thread.print

Java Mission Control : 
Java Mission Control (JMC) is a new JDK profiling and diagnostic tools platform for HotSpot JVM. It’s a tool suite basic monitoring, managing, and production time profiling and diagnostics with high performance. Java Mission Control minimizes the performance overhead that's usually an issue with profiling tools. 
Java mission control was run for the driver program and data about memory and processor usage was captured 
Command : jmc


Java VisualVM
This utility provides a visual interface for viewing detailed information about Java applications while they are running on a Java Virtual Machine. This information can be used in troubleshooting local and remote applications, as well as for profiling local applications. 
Java Visual VM was used to capture the details about thread , heap , garbage collection and data captured for the driver program 
Command : jvisualvm

JConsole utility
This utility is a monitoring tool that is based on Java Management Extensions (JMX). The tool uses the built-in JMX instrumentation in the Java Virtual Machine to provide information about performance and resource consumption of running applications. 
Java Visual VM was used to capture the CPU usage, memory usage, number of classes loaded by the Java process , number of threads created and data has been captured for the driver program 
  jconsole <process_id>

jmap utility
This utility can obtain memory map information, including a heap histogram, from a Java process, a core file, or a remote debug server. 

jmap was used to capture the details about heap like capacity, available space, used space, and data has been captured for the driver program 
jmap -J-d64 -heap 9768


jps utility

This utility lists the instrumented Java HotSpot VMs on the target system. The utility is very useful in environments where the VM is embedded, that is, it is started using the JNI Invocation API rather than the java launcher. 

JPS command was used to list out all the JVM running on the local machine 
Comman : jps 
2096
12356 Boot
12740 Launcher
4196 Jps
13004 RemoteMavenServer
1516 Mai

jstack utility

This utility can obtain Java and native stack information from a Java process. On Oracle Solaris and Linux operating systems the utility can alos get the information from a core file or a remote debug server. 

Jstack command was used to print the stack details and data has been captured for the driver program 
jstack -J-d64 -m 2580


jstat utility

This utility uses the built-in instrumentation in Java to provide information about performance and resource consumption of running applications. The tool can be used when diagnosing performance issues, especially those related to heap sizing and garbage collection

Jstat command was used to collect information regarding garbage collection with a given time interval and data has been captured for the driver program 
jstat -gcutil <process_id>  250 7  


jstatd daemon
This tool is a Remote Method Invocation (RMI) server application that monitors the creation and termination of instrumented Java Virtual Machines and provides an interface to allow remote monitoring tools to attach to VMs running on the local host. 
jstatd -J-Djava.security.policy=all.policy

visualgc utility
This utility provides a graphical view of the garbage collection system. 
As with jstat, it uses the built-in instrumentation of Java HotSpot VM. 




