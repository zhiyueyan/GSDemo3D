package com.dji.GSDemo.GaodeMap;


import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Util {
    static String format7f(double a){
        return String.format("%.7f",a);
    }

    static String format2f(double a){
        return String.format("%.2f",a);
    }

    static String getCurrentTime(){
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    static void saveInfo(String str){
        //File sdCardDir = Environment.getExternalStorageDirectory().getPath();//获取SDCard目录
        File file = new File(Environment.getExternalStorageDirectory(),"info.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file,true);
            fos.write(str.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    static String nulltoIntegerDefalt(String value){
        if(!isIntValue(value)) value="0";
        return value;
    }

    private static boolean isIntValue(String val) {
        try {
            val=val.replace(" ","");
            Integer.parseInt(val);
        } catch (Exception e) {return false;}
        return true;
    }

    static String getDirection(float heading){
        if (heading == 0 ){
            return "正北方向";
        }else if (heading > 0 && heading <90){
            return "北偏东"+format2f(heading)+"°";
        }else if (heading == 90){
            return "正东方向";
        }else if (heading >90 && heading <180){
            return "东偏南"+format2f(heading-90)+"°";
        }else if (heading >-90 && heading <0){
            return "北偏西"+format2f(- heading)+"°";
        }else if (heading> -180 && heading <-90){
            return "西偏南"+format2f(- heading -90)+"°";
        }else if (heading == -90){
            return "正西方向";
        }else {
            return "正南方向";
        }
    }
}
