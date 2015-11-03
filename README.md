# proasense-adapter-file
Each specific adapter will read data from the specific source and convert the data into 
Simple Events that will be published to the ProaSense system. 

# Requirements
  * Internet access
  * Maven
  * Java 1.7 or above
  
# Setup
  --

# Parameters of the property file
    *   proasense.adapter.base.sensorid
    *   proasense.adapter.base.topic
    *   proasense.adapter.base.publish 
    
    *   proasense.adapter.file.system.windows           
    *   proasense.adapter.file.root.directory           
    *   proasense.adapter.file.traverse.subdirectories 
    *   proasense.adapter.file.delay.directory          
    *   proasense.adapter.file.delay.file               

# Folder structure
  This adapter has 4 modules, only 3 will are meant to be executable:
1.  file-materialmovement: reads .xlsx files.
2.  file-montrac: reads .evt files.
3.  file-productionplan: reads .txt files.

# User guide
Each specific adapter will read data from the specific source and convert the data into Simple Events that will be 
published to the ProaSense system. 

  * cd  to proasense-adapter-file.
  * use command "mvn clean install"
  * on "Build success" move to one of the folders shown above, depending on the function needed from 
    the program.
  * type "mvn exec:java" to run the program.
  
    # File material movement
    *	Material movements (delivery and consumption) are reported in the SAP system.
    *	Nightly batch job generates a daily report (.xlsx file) with relevant material movement data for the use case.
    *	Exported report files are placed in a folder on the (Windows 7) virtual machine.

    
    # File montrac
    *	New subfolder named created for each day
    *	Folder name format: [YEAR]-[MONTH]-[DAY], e.g. "2015-06-30"
    *	File name format: [DATE]-[NUMBER]-[SEQUENCE].evt, e.g. "2015-0630-000001-1.evts
    
    # File production plan
    *	Production plans are reported in the SAP system.
    *	Nightly batch job generates a daily report (.txt file) with the relevant production plan data for the use case.
    *	Exported report files are placed in a folder on the (Windows 7) virtual machine.


# Test data
  There is no test data for this module.


