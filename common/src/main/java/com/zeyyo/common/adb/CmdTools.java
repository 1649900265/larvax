package com.zeyyo.common.adb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author yuning.zeng
 */
public class CmdTools {
    private static String TAG="CmdTools";
    private static ExecutorService cachedExecutor = Executors.newCachedThreadPool();

    /**
     * Callable的使用
     * @return
     */
    public static String  execAdbLogcatCmd() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() {
                try {
                    Process process = Runtime.getRuntime().exec("sh");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("logcat -G 16m\n");
                    os.writeBytes("logcat\n");
                    os.flush();
                    // NOTE: You can write to stdin of the command using
                    // process.getOutputStream().
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    System.out.println("执行logcat");
                    int read;
                    char[] buffer = new char[1024];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                    }
                    reader.close();
                    // Waits for the command to finish.
                    process.waitFor();
                   return output.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
            return null;
            }
        };
        Future<String> result = cachedExecutor.submit(callable);
        // 等待执行完毕
        try {
            return result.get();
        } catch (InterruptedException e) {

        } catch (ExecutionException e) {

        }
        return null;
    }
}
