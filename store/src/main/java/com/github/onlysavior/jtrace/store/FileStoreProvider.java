package com.github.onlysavior.jtrace.store;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:39
 * To change this template use File | Settings | File Templates.
 */
public class FileStoreProvider implements SequenceStroreProvider {
    static final String FILE_NAME = "filestore";
    static final int MAX_RETURN_SIZE = 2048;
    static final long INTERVAL = 60 * 60 * 1000;

    private String basePath;
    private File currentFile;
    private BufferedOutputStream bos;

    public FileStoreProvider(String base) {
        this.basePath = base;
    }

    @Override
    public void store(String traceId, String info) {
        String date = extractDate(traceId);
        try {
            if (currentFile != null && bos != null) {
                String parent = currentFile.getParent();
                if (parent != null && in(date, parent)) {
                    write(info, bos);
                } else {
                    File pFile = currentFile.getParentFile();
                    currentFile = new File(pFile.getAbsolutePath() +File.pathSeparator +date, FILE_NAME);
                    currentFile.mkdirs();
                    currentFile.createNewFile();

                    bos.close();
                    bos = new BufferedOutputStream(new FileOutputStream(currentFile));
                    write(info, bos);
                }
            } else {
                currentFile = new File(basePath + File.pathSeparator + date, FILE_NAME);
                currentFile.mkdirs();
                currentFile.createNewFile();

                if (bos != null) {
                    bos.close();
                }

                bos = new BufferedOutputStream(new FileOutputStream(currentFile));
                write(info, bos);
            }
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void store(String traceId, ByteBuffer info) {
        store(traceId, info.asCharBuffer().toString());
    }

    @Override
    public List<Serializable> byId(String traceId) {
        String date = extractDate(traceId);
        File baseFile = new File(basePath);

        List<File> files = new ArrayList<File>();
        String[] subDict = baseFile.list();
        for (String sub : subDict) {
            if (in(date, sub)) {
                files.add(new File(baseFile + File.pathSeparator + sub + File.pathSeparator + FILE_NAME));
            }
        }

        return collect(files, null);
    }

    @Override
    public List<Serializable> range(String min, String max) {
        File baseFile = new File(basePath);

        List<File> files = new ArrayList<File>();
        String[] subDict = baseFile.list();
        for (String sub : subDict) {
            if (between(min, sub, max)) {
                files.add(new File(baseFile + File.pathSeparator + sub + File.pathSeparator + FILE_NAME));
            }
        }
        return collect(files, null);
    }

    @Override
    public List<Serializable> visit(VisitFunction function) {
        File baseFile = new File(basePath);
        List<File> files = new ArrayList<File>();
        String[] subDict = baseFile.list();
        for (String sub : subDict) {
            files.add(new File(baseFile + File.pathSeparator + sub + File.pathSeparator + FILE_NAME));
        }

        return collect(files, function);
    }

    private String extractDate(String traceId) {
        assert traceId != null;
        assert traceId.length() == 25;
        return traceId.substring(8,21);
    }

    private void write(String info, BufferedOutputStream bos) throws IOException {
        assert bos != null;
        bos.write(info.getBytes());
    }

    private boolean in(String date, String parent) {
        int pos = parent.lastIndexOf(File.pathSeparator);
        if (pos != -1) {
            String pTime = parent.substring(pos);
            if (pTime != null && pTime.trim().length() > 0) {
                try {
                    Long pT = Long.valueOf(pTime);
                    Long dT = Long.valueOf(date);

                    return dT - pT < INTERVAL;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } else {
            try {
                Long pT = Long.valueOf(parent);
                Long dT = Long.valueOf(date);

                return dT - pT < INTERVAL;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private boolean between(String min, String toTest, String max) {
        try {
            Long minT = Long.valueOf(min);
            Long tT = Long.valueOf(toTest);
            Long maxT = Long.valueOf(max);

            return tT >= minT && tT <= maxT;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<Serializable> collect(List<File> files, VisitFunction function) {
        List<Serializable> rtn = new LinkedList<Serializable>();
        try {
            int count = 0;
            String line;
            for (File f : files) {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                while (count++ < MAX_RETURN_SIZE && (line = reader.readLine()) != null) {
                    if (function == null) {
                        rtn.add(line);
                    } else {
                        rtn.add(function.visit(line));
                    }
                }

                reader.close();
                if (count < MAX_RETURN_SIZE) {
                    continue;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new StoreException(e);
        }
        return rtn.size() > 0 ? rtn : null;
    }
}
