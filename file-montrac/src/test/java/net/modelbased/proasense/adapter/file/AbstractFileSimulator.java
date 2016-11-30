/**
 * Copyright (C) 2014-2016 SINTEF
 *
 *     Brian Elvesæter <brian.elvesater@sintef.no>
 *     Shahzad Karamat <shazad.karamat@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.modelbased.proasense.adapter.file;

import net.modelbased.proasense.adapter.base.AbstractBaseAdapter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;


public abstract class AbstractFileSimulator extends AbstractBaseAdapter {
    public final static Logger logger = Logger.getLogger(AbstractFileSimulator.class);

    protected FileConsumerInput inputPort;

    private boolean trace = false;
    public String sensorId;
    protected Boolean isWindowsFileSystem;
    protected String rootDirectoryPath;
    protected int directoryDelayValue;
    protected int fileDelayValue;
    protected Boolean isDeleteFile;
    protected Boolean isArchiveFile;
    protected String archiveDirectoryPath;
    private int eventsProcessed = 0;
    private boolean traverseSubs = Boolean.parseBoolean(adapterProperties.getProperty("proasense.adapter.file.traverse.subdirectories"));


    protected AbstractFileSimulator() {}


    protected AbstractFileSimulator(String folderPath) {
        // Adapter properties
        sensorId = adapterProperties.getProperty("proasense.adapter.base.sensorid");
        isWindowsFileSystem = Boolean.parseBoolean(adapterProperties.getProperty("proasense.adapter.file.system.windows"));
        rootDirectoryPath = adapterProperties.getProperty("proasense.adapter.file.root.directory");
        directoryDelayValue = Integer.parseInt(adapterProperties.getProperty("proasense.adapter.file.delay.directory"));
        fileDelayValue = Integer.parseInt(adapterProperties.getProperty("proasense.adapter.file.delay.file"));
        isDeleteFile = Boolean.parseBoolean(adapterProperties.getProperty("proasense.adapter.file.delete.file"));

        this.inputPort = new FileConsumerInput();

        try {
            simulateEventsFromFolder(folderPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void simulateEventsFromFolder(String folderPath) throws FileNotFoundException, InterruptedException, ParseException, IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                convertToSimpleEvent(file.getPath());
                Thread.sleep(1000);
            }
        }
    }


    public void warningMessage(String path){
        System.out.println("File in path: ");
        System.out.println(path);
        System.out.println(" is corrupt, please check format of this file.");
    }


    public abstract void convertToSimpleEvent(String filePath) throws IOException, ParseException;
}
