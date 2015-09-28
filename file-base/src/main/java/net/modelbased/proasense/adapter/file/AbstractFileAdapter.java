/**
 * Copyright (C) 2014-2015 SINTEF
 *
 *     Brian Elves�ter <brian.elvesater@sintef.no>
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;


public abstract class AbstractFileAdapter extends AbstractBaseAdapter {
    public final static Logger logger = Logger.getLogger(AbstractFileAdapter.class);

    protected FileConsumerInput inputPort;

    WatchService watcher;

    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    public String sensorId;
    protected String rootDirectoryPath;
    protected int directoryDelayValue;
    protected int fileDelayValue;
    private int eventsProcessed = 0;


    protected AbstractFileAdapter() {
        // Adapter properties
        sensorId = adapterProperties.getProperty("proasense.adapter.base.sensorid");
        rootDirectoryPath = adapterProperties.getProperty("proasense.adapter.file.root.directory");
        directoryDelayValue = Integer.parseInt(adapterProperties.getProperty("proasense.adapter.file.delay.directory"));
        fileDelayValue = Integer.parseInt(adapterProperties.getProperty("proasense.adapter.file.delay.file"));

        keys = new HashMap<WatchKey,Path>();
        this.inputPort = new FileConsumerInput();

        try {
            scanDirectory(rootDirectoryPath, directoryDelayValue, fileDelayValue);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void registerAll(final Path start) throws IOException {
        logger.debug("Traversing all directories...");
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        logger.debug("Done.");
    }


    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }


    protected void scanDirectory(String path, int directoryDelay, int fileDelay) throws IOException, InterruptedException, ParseException {
        watcher = FileSystems.getDefault().newWatchService();

        Path directoryName = null;
        Path dir = Paths.get(path);

        registerAll(dir);

        for(;;) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }


            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path directory;

                if(filename != null) {
                    directory = dir.resolve(filename);
                }else{
                    continue;
                }

                if (kind == OVERFLOW) {
                    continue;
                }
                else if (kind == ENTRY_MODIFY) {
                    //
                }
                else if (kind == ENTRY_CREATE) {
//                    logger.debug("delay begin");
//                    Thread.sleep(delay);
//                    logger.debug("delay end");
                    String suffix[] = (directory.toString()).split("\\.");
                    if((suffix.length > 1) && ((suffix[1].endsWith("evt")) || (suffix[1].endsWith("xlsx")))){
                        String filePath = (directory.getParent().toAbsolutePath()+"\\"+directoryName+"\\"+filename);

                        if (directoryName == null) {
                            System.out.println("Please create a folder first and only then add files to it!");
                        }
                        else {
                            checkFileLength(filePath, fileDelay);
                            convertToSimpleEvent(filePath);
                        }
                    }
                    else if (Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                        directoryName = filename;
                        Thread.sleep(directoryDelay);
                        logger.debug("Directory " + directoryName + " created.");
                        registerAll(directory);
                    }
                    else {
                        System.out.println("File not recognized, suffix should be .evt or .xlsx.");
                    }
                }
                else if (kind == ENTRY_DELETE) {
                    logger.debug(kind.name() + " " + directory);
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }


    public void warningMessage(String path){
        System.out.println("File in path: ");
        System.out.println(path);
        System.out.println(" is corrupt, please check format of this file.");
    }


    public void checkFileLength(String filePath, int fileDelay) {
        eventsProcessed++;

        try {
            Thread.sleep(fileDelay);
            File file = new File(filePath);
//            while (!checkFileExists(file)) {
//                Thread.sleep(fileDelay);
//                file = new File(filePath);
//            }

            long prevFileSize = 0;
            long currentFileSize = 1;
            while (prevFileSize != currentFileSize) {
                prevFileSize = currentFileSize;
                Thread.sleep(fileDelay);
                currentFileSize = file.length();
                logger.debug("prevFileSize = " + prevFileSize);
                logger.debug("currentFileSize = " + currentFileSize);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.debug("checkFileLength(): # events = " + eventsProcessed);
    }


    public static boolean checkFileExists(File file) {
        try {
            byte[] buffer = new byte[4];
            InputStream is = new FileInputStream(file);
            if (is.read(buffer) != buffer.length) {
                // do something
            }
            is.close();
            return true;
        }
        catch (java.io.IOException e) {
            //
        }
        return false;
    }


    public abstract void convertToSimpleEvent(String filePath) throws IOException, ParseException;
}