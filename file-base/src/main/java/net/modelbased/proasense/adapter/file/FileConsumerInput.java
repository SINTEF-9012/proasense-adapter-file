package net.modelbased.proasense.adapter.file;

import java.util.Properties;

/**
 * Created by shahzad on 18.07.15.
 */
public class FileConsumerInput {

    public String rootDirectoryPath;
    public int delayValue;

    FileConsumerInput(){
        getPathForRootDirectory();
    }

    void getPathForRootDirectory(){

        Properties properties = new Properties();
        rootDirectoryPath = properties.getProperty("proasense.adapter.file.folder.root");
        delayValue = Integer.parseInt(properties.getProperty("proasense.adapter.file.time.delay"));
    }
}