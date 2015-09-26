package net.modelbased.proasense.adapter.file;

import net.modelbased.proasense.adapter.base.AbstractBaseAdapter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by shahzad on 18.07.15.
 */
public abstract class AbstractFileAdapter extends AbstractBaseAdapter {
    protected FileConsumerInput inputPort;

    WatchService watcher;

    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    protected String rootDirectoryPath;
    protected int delayValue;
    public String sensorId;
    public final static Logger logger = Logger.getLogger(AbstractFileAdapter.class);

    protected AbstractFileAdapter() {

        sensorId = adapterProperties.getProperty("proasense.adapter.file.sensor.id");
        keys = new HashMap<WatchKey,Path>();
        this.inputPort = new FileConsumerInput();
        rootDirectoryPath = adapterProperties.getProperty("proasense.adapter.file.folder.root");
        delayValue = Integer.parseInt(adapterProperties.getProperty("proasense.adapter.file.time.delay"));
        inputPort = new FileConsumerInput();
    }

    private void registerAll(final Path start) throws IOException {

        logger.debug("Traversing all directories..");
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

    protected void scanDirectory(String path, int delay) throws IOException, InterruptedException, ParseException {

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
                }else if (kind == ENTRY_MODIFY) {

                }else if (kind == ENTRY_CREATE){
                    logger.debug("delay begin");
                    Thread.sleep(delay);
                    logger.debug("delay end");
                    String suffix[] = (directory.toString()).split("\\.");
                    if((suffix.length > 1) && ((suffix[1].endsWith("evt")) || (suffix[1].endsWith("xlsx")))){
                        String adress = (directory.getParent().toAbsolutePath()+"\\"+directoryName+"\\"+filename);

                        if(directoryName == null){
                            System.out.println("Please create a folder first and only then add files to it!");
                        }else{
                            chechFileLength(adress);
                            convertToSimpleEvent(adress);
                        }

                    }else if(Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)){
                        directoryName = filename;
                        logger.debug("Made a file.");
                        registerAll(directory);

                        Thread.sleep(delay);
                    }else{
                        System.out.println("File not recognized, suffix should be .evt or .xlsx.");
                    }

                }else if (kind == ENTRY_DELETE){

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
        System.out.println("File in path:");
        System.out.println(path);
        System.out.println("is corrupt, please check format of this file");
    }
    int n = 0;
    public void chechFileLength(String filePath){
        n++;
            File file = new File(filePath);
            long prevFileSize = 0;
            long currentFileSize = 1;

        System.out.println("er i checkLength metoden, antall events er "+n+" currfileSize er "+currentFileSize);

        while(prevFileSize != currentFileSize){
            prevFileSize = currentFileSize;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentFileSize = file.length();
        }
    }

    public abstract void convertToSimpleEvent(String filePath) throws IOException, ParseException;
}