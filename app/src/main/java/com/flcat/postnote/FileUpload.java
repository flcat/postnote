package com.flcat.postnote;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class FileUpload {

    public static final String UPLOAD_URL= "http://flcat.vps.phps.kr/upload.php";

    private int serverResponseCode;
    HttpURLConnection conn = null; //네트워크 연결 객체
    DataOutputStream dos = null;
    String lineEnd = "\r\n";    //구분자
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 10 * 1024 * 1024;
    public static String fileName;
    public static String fileExtension;
    public static String stUploadtime;
    /**파일 확장자 가져오기
     * @param fileStr 경로나 파일이름
     * @return*/

    public static String getExtension(String fileStr){
        String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1);
        return TextUtils.isEmpty(fileExtension) ? null : fileExtension;
    }

    /**파일 이름 가져오기
     * @param fileStr 파일 경로
     * @param isExtension 확장자 포함 여부
     * @return */
    public String getFileName(String fileStr, boolean isExtension){
        fileName = null;
        if(isExtension)
        {
            fileName = fileStr.substring(fileStr.lastIndexOf("/"),fileStr.lastIndexOf("."));
        }else{
            fileName = fileStr.substring(fileStr.lastIndexOf("/")+1);
        }
        return fileName;
    }
    public String uploadImage(String file) {

        fileExtension = getExtension(file);
        //fileName = file; //파일위치
        fileName = getFileName(file,true);
        File sourceFile = new File(file);
        if (!sourceFile.isFile()) { //해당 위치에 파일 존재하는지 검사
            Log.e("Huzza", "Source File Does not exist");
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(UPLOAD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploadedfile", fileName);
            conn.setRequestProperty("extension","."+fileExtension);
            stUploadtime = System.currentTimeMillis()+"";

            // dataoutput은 outputstream이란 클래스를 가져오며, outputStream는 FileOutputStream의 하위 클래스이다.
            // output은 쓰기, input은 읽기, 데이터를 전송할 때 전송할 내용을 적는 것으로 이해할 것
            dos = new DataOutputStream(conn.getOutputStream());


            // 이미지 전송, 데이터 전달 uploadded_file라는 php key값에 저장되는 내용은 fileName
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + fileName+ stUploadtime+ "." + fileExtension + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data..., 마지막에 two~~ lineEnd로 마무리 (인자 나열이 끝났음을 알림)
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage =conn.getResponseMessage();
            Log.i("Http Response is ",serverResponseMessage + " : " + serverResponseCode);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream(), "UTF-8"));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("lineEE",line);
            }
            rd.close();

            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream(), "UTF-8"));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (IOException ioex) {
            }
            return sb.toString();
        }else {
            return "Could not upload";
        }
    }
}