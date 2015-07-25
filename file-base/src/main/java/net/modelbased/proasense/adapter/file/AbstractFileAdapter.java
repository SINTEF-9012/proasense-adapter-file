package net.modelbased.proasense.adapter.file;

import net.modelbased.proasense.adapter.base.AbstractBaseAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Created by shahzad on 18.07.15.
 */
public abstract class AbstractFileAdapter extends AbstractBaseAdapter {
    protected FileConsumerInput inputPort;

    WatchService watcher;

    private final Map<WatchKey,Path> keys;
    private boolean trace = false;

    protected AbstractFileAdapter() {

        keys = new HashMap<WatchKey,Path>();
        this.inputPort = new FileConsumerInput();
    }

    private void registerAll(final Path start) throws IOException {

        System.out.println("Traversing all directories..");
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Done.");
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    protected void scanDirectory(String path, int delay) throws IOException, InterruptedException {

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

                    String suffix[] = (directory.toString()).split("\\.");
                    if((suffix.length > 1) && (suffix[1].endsWith("evt"))){
                        System.out.println("Laget fil.");
                        String adress = (directory.getParent().toAbsolutePath()+"/"+directoryName+"/"+filename);
                        convertToSimpleEvent(adress);

                    }else if(Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)){
                        directoryName = filename;
                        registerAll(directory);

                        Thread.sleep(delay);
                    }

                }else if (kind == ENTRY_DELETE){

                    System.out.println(kind.name() + " " + directory);

                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }


    public abstract void convertToSimpleEvent(String filePath)throws FileNotFoundException;
}