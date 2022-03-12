package com.zeyyo.common.adb;

import android.os.Build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProcessInstance {
    File targetDirectory = new File("/sdcard/Download/OsTestTowerSystem/");
    private String saveFile;
    private static volatile ProcessInstance instance;
    private Process process;
    private boolean isRunLogcat = false;

    public static ProcessInstance getProcessInstance() {
        if (instance == null) {
            synchronized (ProcessInstance.class) {
                if (instance == null) {
                    instance = new ProcessInstance();
                }
            }
        }
        return instance;
    }

    private boolean isActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return process.isAlive();
        } else {
            return true;
        }
    }

    private ProcessInstance() {
        try {
            process = Runtime.getRuntime().exec("sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startLogcat() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
        Date now = new Date(System.currentTimeMillis());
        String format = dataFormat.format(now);
        try {
            File file = new File("/sdcard/Download/OsTestTowerSystem/");
            if (!file.exists()) {
                file.mkdirs();
            }
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("logcat -G 16m\n");
//              os.writeBytes("logcat\n");
            String logcatCmd = "logcat -f  " + file + File.separator + format + "size.txt -r 1024 -n  100";
            System.out.println("执行logcat:" + logcatCmd);
            os.writeBytes(logcatCmd);
            os.flush();
            // NOTE: You can write to stdin of the command using
            // process.getOutputStream().
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            isRunLogcat = true;
            int read;
            char[] buffer = new char[1024];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0&&isRunLogcat) {
                output.append(buffer, 0, read);
//            System.out.println("输出logcat日志"+output.length());
                if (output.length() > 1024 * 4) {
                    System.out.println("给客户端发送日志" + output.length());
//                ByteBuf byteBuf = Unpooled.copiedBuffer(output.toString().getBytes(CharsetUtil.UTF_8));
                    output.setLength(0);
                }
            }
            reader.close();
            // Waits for the command to finish.
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startReadLog() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
        Date now = new Date(System.currentTimeMillis());
        String format = dataFormat.format(now);
        new Thread(()->{
            try {
//                File file = new File("/sdcard/Download/OsTestTowerSystem/");
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("logcat -G 16m\n");
                os.writeBytes("logcat\n");
                os.flush();
                // NOTE: You can write to stdin of the command using
                // process.getOutputStream().
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                saveFile = targetDirectory + File.separator + format + "systemLog.txt";
                System.out.println("日志保存地址:" + saveFile);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFile));
                isRunLogcat = true;
                char[] readChar = new char[2048];
                int length;
                System.out.println("正在保存文件------------------------:"+saveFile);
                while ((length = reader.read(readChar)) > 0&&isRunLogcat) {
                    bufferedWriter.write(readChar);
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void stopLogcat() {
        System.out.println("logcat 命令停止");
        if (process != null && isRunLogcat) {
            isRunLogcat = false;
            File file = new File(saveFile);
            if (file.exists()) {
                splitFileByLine(saveFile,targetDirectory.getAbsolutePath(),10000);
//                splitFile(new File(saveFile), 1024 * 1024);
            }
            process.destroy();
        }
    }

    /**
     * 按行分割文件
     * @param sourceFilePath 为源文件路径
     * @param targetPath 文件分割后存放的目标目录
     * @param rows 为多少行一个文件
     */
    public static int splitFileByLine(String sourceFilePath, String targetPath, int rows) {
        String sourceFileName = sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1, sourceFilePath.lastIndexOf("."));//源文件名
        String splitFileName = targetPath + File.separator + sourceFileName + "-%s.txt";//切割后的文件名
        File targetDirectory = new File(targetPath);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }

        PrintWriter pw = null;//字符输出流
        String tempLine;
        int lineNum = 0;//本次行数累计 , 达到rows开辟新文件
        int splitFileIndex = 1;//当前文件索引

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFilePath)))) {
            pw = new PrintWriter(String.format(splitFileName, splitFileIndex));
            while ((tempLine = br.readLine()) != null) {
                if (lineNum > 0 && lineNum % rows == 0) {//需要换新文件
                    pw.flush();
                    pw.close();
                    pw = new PrintWriter(String.format(splitFileName , ++splitFileIndex));
                }
                pw.write(tempLine + "\n");
                lineNum++;
            }
            return splitFileIndex;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }finally {
            if (null != pw) {
                pw.flush();
                pw.close();
            }
            File file=new File(sourceFilePath);
            if(file.exists()){
                file.delete();
            }
        }


    }

    /**
     * 按文件大小分割
     * @param targetFile
     * @param cutSize
     * @return
     */
    public static int splitFile(File targetFile, long cutSize) {
        //计算切割文件大小
        int count = targetFile.length() % cutSize == 0 ? (int) (targetFile.length() / cutSize) :
                (int) (targetFile.length() / cutSize + 1);

        RandomAccessFile raf = null;
        try {
            //获取目标文件 预分配文件所占的空间 在磁盘中创建一个指定大小的文件   r 是只读
            raf = new RandomAccessFile(targetFile, "r");
            long length = raf.length();//文件的总长度
            long maxSize = length / count;//文件切片后的长度
            long offSet = 0L;//初始化偏移量

            for (int i = 0; i < count - 1; i++) { //最后一片单独处理
                long begin = offSet;
                long end = (i + 1) * maxSize;
                offSet = getWrite(targetFile.getAbsolutePath(), i, begin, end);
            }
            if (length - offSet > 0) {
                getWrite(targetFile.getAbsolutePath(), count - 1, offSet, length);
            }

        } catch (FileNotFoundException e) {
//            System.out.println("没有找到文件");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * 指定文件每一份的边界，写入不同文件中
     *
     * @param file  源文件地址
     * @param index 源文件的顺序标识
     * @param begin 开始指针的位置
     * @param end   结束指针的位置
     * @return long
     */
    public static long getWrite(String file, int index, long begin, long end) {

        long endPointer = 0L;

        String a = file.split(suffixName(new File(file)))[0];

        try {
            //申明文件切割后的文件磁盘
            RandomAccessFile in = new RandomAccessFile(new File(file), "r");
            //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            //读取切片文件
            File mFile = new File(a + "_" + index + ".tmp");
            //如果存在
            if (!mFile.exists()) {
                RandomAccessFile out = new RandomAccessFile(mFile, "rw");
                //申明具体每一文件的字节数组
                byte[] b = new byte[1024];
                int n = 0;
                //从指定位置读取文件字节流
                in.seek(begin);
                //判断文件流读取的边界
                while ((n = in.read(b)) != -1 && in.getFilePointer() <= end) {
                    //从指定每一份文件的范围，写入不同的文件
                    out.write(b, 0, n);
                }

                //定义当前读取文件的指针
                endPointer = in.getFilePointer();
                //关闭输入流
                in.close();
                //关闭输出流
                out.close();
            } else {
                //不存在

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return endPointer - 1024;
    }

    /**
     * 获取文件后缀名 例如：.mp4 /.jpg /.apk
     *
     * @param file 指定文件
     * @return String 文件后缀名
     */
    public static String suffixName(File file) {
        String fileName = file.getName();
        String fileTyle = fileName.substring(fileName.lastIndexOf("."));
        return fileTyle;
    }
}
