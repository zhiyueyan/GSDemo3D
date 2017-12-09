package com.dji.GSDemo.GaodeMap;


import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
            fos.flush();
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

    static float getRotateAngle(float heading){
        if (heading >= -180 && heading <=0){
            return -heading;
        }else {
            return 180+180-heading;
        }
    }

    static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static void saveXmlInfo(List<String> informations){
        File file = new File(Environment.getExternalStorageDirectory(),"info.xml");
        FileOutputStream fos;
        XmlSerializer xmlSerializer = Xml.newSerializer();//获取对象
        try {
            fos = new FileOutputStream(file,true);
            xmlSerializer.setOutput(fos,"UTF-8");//设置输出流
            xmlSerializer.startDocument("UTF-8",true);//设置文档标签
            xmlSerializer.startTag(null,"Informations");//设置根标签

            xmlSerializer.startTag(null,"Time");
            xmlSerializer.text(informations.get(0));
            xmlSerializer.endTag(null,"Time");

            xmlSerializer.startTag(null,"Lat");
            xmlSerializer.text(informations.get(1));
            xmlSerializer.endTag(null,"Lat");

            xmlSerializer.startTag(null,"Lng");
            xmlSerializer.text(informations.get(2));
            xmlSerializer.endTag(null,"Lng");

            xmlSerializer.endTag(null,"Informations");
            xmlSerializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void uploadServer(String information,String ip,int port){
        Socket socket ;
        String message = information;
        try {
            //创建Socket
            socket = new Socket(ip,port);
            //向服务器端发送消息
            PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(message);
            //关闭流
            out.close();
            //关闭Socket
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
