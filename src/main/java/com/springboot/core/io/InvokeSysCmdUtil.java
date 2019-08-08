package com.springboot.core.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 调用系统命令
 * Created by linyang on 2016/9/2.
 */
public class InvokeSysCmdUtil {

    public static int invoke(String command) throws Exception{
        String[] cmd = { "/bin/bash", "-c", command};
        String s;

        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = null;
        BufferedReader stdError = null;
        try {
            stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            StringBuilder std = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                std.append(s+"\n");
            }
            String info = std.toString();
            StringBuilder stdErr;
            if (info.length() > 0){
                System.out.println(" here is the standard output of the command:\n"+ info);
                stdErr = new StringBuilder();
            }else{
                stdErr = std;
            }
            while ((s = stdError.readLine()) != null) {
                stdErr.append(s+"\n");
            }
            info = stdErr.toString();
            if (info.length() > 0){
                System.out.println("Here is the standard error of the command \n"+ info);
            }
            int exitValue = p.waitFor();
            if(exitValue != 0){
                System.out.println("call shell failed. error code is :" + exitValue+",the message:"+info);
                throw new RuntimeException(info);
            }
            return exitValue;
        } finally {
            if(stdInput != null){
                stdInput.close();
            }
            if(stdError != null){
                stdError.close();
            }
        }
    }
}
