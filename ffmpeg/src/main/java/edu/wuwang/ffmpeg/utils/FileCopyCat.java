package edu.wuwang.ffmpeg.utils;

import android.content.res.AssetManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by aiya on 2017/6/12.
 */

public class FileCopyCat {

    private static FileCopyCat instance;

    private FileCopyCat(){

    }

    public static FileCopyCat getInstance(){
        if(instance==null){
            synchronized(FileCopyCat.class){
                if(instance==null){
                    instance=new FileCopyCat();
                }
            }
        }
        return instance;
    }

    //递归复制assets文件到指定目录
    public boolean copyFolder(AssetManager manager,String src, String dst) {
        try {
            String[] files = manager.list(src);
            if (files.length > 0) {     //如果是文件夹
                File folder = new File(dst);
                if(!folder.exists()){
                    boolean b = folder.mkdirs();
                    if (!b) {
                        return false;
                    }
                }
                for (String fileName : files) {
                    if (!copyFolder(manager,src + File.separator + fileName, dst +
                        File.separator + fileName)) {
                        return false;
                    }
                }
            } else {  //如果是文件
                if(!copyFile(manager,src, dst)){
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean copyFile(AssetManager manager,String src, String dst) {
        InputStream in;
        OutputStream out;
        try {
            File file = new File(dst);
            if (!file.exists()) {
                in = manager.open(src);
                out = new FileOutputStream(dst);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                out.close();
                in.close();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                return true;
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
        return false;
    }

    public boolean copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (String aFile : file) {
                if (oldPath.endsWith(File.separator)) {
                    temp=new File(oldPath + aFile);
                } else {
                    temp=new File(oldPath + File.separator + aFile);
                }

                if (temp.isFile()) {
                    FileInputStream input=new FileInputStream(temp);
                    FileOutputStream output=new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] b=new byte[1024 * 5];
                    int len;
                    while ((len=input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + aFile, newPath + "/" + aFile);
                }
            }
            return true;
        }catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
        return false;
    }


}
