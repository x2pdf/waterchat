package com.logan.utils;


import com.logan.config.InitSource;
import com.logan.config.SysConfig;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Logan Qin
 * @date 2021/11/19 13:50
 */
public class LocalFileUtils {
    // 设定缓存文件的保存路径
    private static String cacheResourcePath = SysConfig.APP_DOWNLOAD_PATH;
    private static String cachePath = SysConfig.APP_DOWNLOAD_PATH;
    private static String logPath = SysConfig.LOG_CACHE_PATH;

    public static String getLogPath() {
        return logPath;
    }

    /**
     * @param bytes        文件字节
     * @param fileFullName 文件全名， 如 ac_123456.pdf
     * @return 文件全路径，如 C:\Users\Administrator\AppData\Local\Temp\LocalFileUtils\ac_123456.pdf
     * @throws IOException
     */
    public static String save(byte[] bytes, String fileFullName) throws IOException {
        String targetFile = cachePath + fileFullName;
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream out = new FileOutputStream(targetFile);
        out.write(bytes, 0, bytes.length);
        out.flush();
        out.close();

        return targetFile;
    }


    public static String save2Path(byte[] bytes, String path, String fileFullName) throws IOException {
        String targetFile = path + fileFullName;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream out = new FileOutputStream(targetFile);
        out.write(bytes, 0, bytes.length);
        out.flush();
        out.close();

        return targetFile;
    }

    /**
     * 加载文件返回字节数组
     *
     * @param fileFullName
     * @return
     * @throws IOException
     */
    public static byte[] load(String fileFullName) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileFullName));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int size = 0;
        while ((size = in.read(temp)) != -1) {
            out.write(temp, 0, size);
        }
        in.close();

        return out.toByteArray();
    }


    public static void appendToMessageFile(String messages, String filePath) throws IOException {
        File file = new File(SysConfig.APP_DOWNLOAD_PATH);
        try {
            if (!file.exists()) {
                file.mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
                out.write(messages);
                out.close();
            } else {
                BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
                out.write(messages);
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String append2Log(String content) {
        return append2Log(content, SysConfig.SESSION_LOG_FILE_NAME);
    }

    public static String append2Log(String content, String fileFullName) {
        String targetFile = logPath + fileFullName;
        File file = new File(logPath);
        try {
            if (!file.exists()) {
                file.mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(targetFile));
                out.write(LocalDateTime.now().toString() + " " + content + "\n");
                out.close();
            } else {
                BufferedWriter out = new BufferedWriter(new FileWriter(targetFile, true));
                out.write(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS").format(LocalDateTime.now()) + " " + content + "\n");
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return targetFile;
    }

    public static String mkTempResourcesDir(String path) {
        if (path == null || "".equals(path)) {
            return cacheResourcePath;
        }
        String targetFile = cacheResourcePath + path;
        try {
            File file = new File(cacheResourcePath + path);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetFile;
    }


    public static String mkTempDir(String path) {
        if (path == null || "".equals(path)) {
            return cachePath;
        }
        String targetFile = cachePath + path + File.separator;
        try {
            File file = new File(cachePath + path);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetFile;
    }


    public static String save2TempDir(byte[] bytes, String tempDir, String name) throws IOException {
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream out = new FileOutputStream(tempDir + File.separator + name);
        out.write(bytes, 0, bytes.length);
        out.flush();
        out.close();

        return tempDir;
    }


    public static boolean isFileExist(String path, String fileName) {
        File filePath = new File(path);
        // 路径是否存在
        if (filePath.exists()) {
            String fileAbsPath = path + File.separator + fileName;
            File file = new File(fileAbsPath);
            // 文件是否存在
            if (file.isFile() && file.exists()) {
                return true;
            }
        }

        return false;
    }

    public static void makeDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            LogUtils.info("创建文件夹： " + path);
        }
    }


    // 只返回当前目录下的文件，子目录的文件不返回
    public static ArrayList<File> getFilesInFold(String path) {
        File filePath = new File(path);
        // 路径是否存在
        if (filePath.exists()) {
            ArrayList<File> res = new ArrayList<>();
            File[] files = filePath.listFiles();
            if (files == null) {
                return null;
            }
            for (File file : files) {
                if (file.isFile()) {
                    res.add(file);
                }
            }
            if (res.size() == 0) {
                return null;
            }
            return res;
        }
        return null;
    }


    public static boolean deleteFolder(String sPath) {
        System.gc();
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                return deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }

    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            boolean delete = file.delete();
            flag = delete;
//            System.out.println("delete file: " + delete + " - " + sPath);
        }
        return flag;
    }

    public static void deleteFolder(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFolder(f);
                }
            }
        }
        boolean success = file.delete();
        if (!success) {
            LogUtils.error("删除文件失败：" + file.getAbsolutePath());
        }
    }

    /**
     * 删除给定文件夹以及该文件下所有的内容（文件和文件夹）
     *
     * @param sPath
     * @return
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
//                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
//                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }


    public static InputStream getStreamFromResourcePath(String resourcePathAndFullName) throws IOException {
        byte[] aByte = is2Byte(new FileInputStream(new File("").getCanonicalPath() + "/src/main/resources/" + resourcePathAndFullName));
        return new ByteArrayInputStream(aByte);
    }

    public static byte[] is2Byte(InputStream is) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        try {
            int n = 0;
            while (-1 != (n = is.read(bytes))) {
                byteArrayOutputStream.write(bytes, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }


    /**
     * 读取文件的所有行数据
     *
     * @param fileFullName
     * @return
     */
    public static List<String> readFileLine(String fileFullName) {
        try {
            LinkedList<String> res = new LinkedList<String>();
            File file = new File(fileFullName);
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    res.add(lineTxt);
                }
                br.close();
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<String>(0);
    }


    /**
     * 读取文件的所有行数据
     *
     * @param fileFullName
     * @return
     */
    public static List<String> readFileLineByNIO(String fileFullName) {
        FileInputStream fis = null;
        FileChannel inChannel = null;
        int bufSize = 1024 * 10;
        try {
            fis = new FileInputStream(fileFullName);
            inChannel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(bufSize);
            String enterStr = "\n";
            StringBuffer strBuf = new StringBuffer("");
            LinkedList<String> res = new LinkedList<>();

            int lineNum = 0;
            while (inChannel.read(buffer) != -1) {
                int rSize = buffer.position();
                buffer.clear();
                String tempString = new String(buffer.array(), 0, rSize);
                if (fis.available() == 0) {
                    // 最后一行，加入"\n分割符"
                    tempString += "\n";
                }

                int fromIndex = 0;
                int endIndex = 0;
                while ((endIndex = tempString.indexOf(enterStr, fromIndex)) != -1) {
                    String line = tempString.substring(fromIndex, endIndex);
                    line = strBuf.toString() + line;

                    res.add(line);

                    strBuf.delete(0, strBuf.length());
                    fromIndex = endIndex + 1;
                    lineNum++;
                }


                if (rSize > tempString.length()) {
                    strBuf.append(tempString.substring(fromIndex, tempString.length()));
                } else {
                    strBuf.append(tempString.substring(fromIndex));
                }
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ArrayList<>(0);
    }


}
