package lkphandev.com.luckynumber;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lkphandev.com.luckynumber.BuildConfig.DEBUG;

//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.JsonReader;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.FilenameFilter;
//import java.io.InputStreamReader;
//import java.io.RandomAccessFile;
//import java.lang.reflect.Array;
//import java.net.SocketException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import java.io.File;

/**
 * Created by kimiboo on 2018-02-17.
 */

public class Ultis {
    public static Boolean selfCensor(Context context, String str) {
        if (str != null)
            return true;

        Toast.makeText(context, "please double check your sticket", Toast.LENGTH_SHORT).show();
        return false;
    }


    //TODO: convert a json string to obj class
    public static Object json2Obj(String json, Class _class) {
        Object obj = null;
        if (json != null) {
            obj = new Gson().fromJson(json, _class);
        }
        return obj;
    }

    //TODO: return a json string from obj
    public static String obj2Json(Object obj) {
        String str = null;
        if (obj != null) {
            str = new Gson().toJson(obj);
        }
        return str;
    }

    //TODO read json file and turn into json obj
    /**
     * //011120lk - update for view local history tickets
     * @param: File file.json
     * @return: json obj
     * */
    public static Object jsonFile2Obj(File file){

        try{
            //read file json
            //convert reader to obj
            BufferedReader _reader = new BufferedReader(new FileReader(file));
            return new Gson().fromJson(_reader,Games.class);

        }catch (FileNotFoundException err){
            Log.d("jsonFile2Obj","err" + err.getMessage());
        }

        return null;
    }


//    TODO: make bg dim

    public static void applyDim( ViewGroup parent, float dimAmount) {
        Drawable dim = new ColorDrawable(Color.DKGRAY);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    //TODO: send request replay ticket to ftp server
    public static Boolean repReplayTicket(Context context, String ticketNo) {

        String SERVERFOLDER = context.getResources().getString(R.string.replay_ticket_folder);
        String server = context.getResources().getString(R.string.server);
        String port = context.getResources().getString(R.string.port);
        String usr = context.getResources().getString(R.string.usr);
        String pass = context.getResources().getString(R.string.pass);
        String nameFile = "req_" + ticketNo;
        FTPClient ftpClient = null;
        File path = context.getExternalFilesDir(null);
        File fileLoc = new File(path, nameFile + ".json");
        InputStream inputStream = null;


        try {
            ftpClient = ftpConnect(server, Integer.parseInt(port), usr, pass);
            ftpClient.changeWorkingDirectory(SERVERFOLDER);
            FileOutputStream fileOutputStream = new FileOutputStream(fileLoc);
            fileOutputStream.close();

            inputStream = new FileInputStream(fileLoc);
            Boolean success = ftpClient.storeFile(fileLoc.getName(), inputStream);

            if (success) {
                Log.i("repReplayTicket", " submit successfully.");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    logout(ftpClient);
                } catch (IOException e) {
                    if (DEBUG)
                        Log.e("request replay ticket", "request ticket replay, finally, e: '" + e + "'");
                }
            }
        }
        return true;
    }

    //TODO: get history local ticket by scanning local folder
    /**
     * 101720lk - get all files in dir
     * */
    public static  ArrayList<File> getLocalTicket(){
        String path = Environment.getExternalStorageDirectory().toString()+ "/Android/data/com.storematepos.luckynumber/files/";//R.string.lucky_local_path;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);

        //TRY TO SORT OUT FOLLOW LAST MODIFED DAY REVERSE
        File[] files = directory.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        ArrayList fileFilter = new ArrayList<File>();
        for(File _file : files){

//            deleteOldFile(_file);
            //CHECK FILE NAME FOLLOW STRUCTURE
            if (checkFileName(_file.getName())){
                //DELETE OLD FILE MORE THAN 1 MONTH AND ADD TO LATEST LOCAL FILES
                if (!deleteOldFile(_file))
                    fileFilter.add(_file);
            }

        }

//        for (int i = list.length-1; i >=0 ; i--) {
//            //use list.getName to get the name of the file
//        }

        return fileFilter;
    }

    //TODO: get history local ticket by scanning local folder
    /**
     * 101720lk - change temp file to local file
     * */
    public static  Boolean reNameLocal( File oldFile, String total){
        String path = Environment.getExternalStorageDirectory().toString()+ "/Android/data/com.storematepos.luckynumber/files/";//R.string.lucky_local_path;
//        int totalFile = scanFile(path,"local_").length;
        Log.d("reNameLocal", "Path: " + path);
//        String prefix = "local_" + ;
        File newFile = new File(path,"local_" + total + "_" + oldFile.getName());
        oldFile.renameTo(newFile);
        return oldFile.renameTo(newFile);
    }

    //TODO: get replay ticket by reading file from ftp server
    public static JsonObject getReplayTicket(Context context, String ticketNo) {

        String SERVERFOLDER = context.getResources().getString(R.string.replay_ticket_folder);
        String server = context.getResources().getString(R.string.server);
        String port = context.getResources().getString(R.string.port);
        String usr = context.getResources().getString(R.string.usr);
        String pass = context.getResources().getString(R.string.pass);
        String fileServer = "res_" + ticketNo + ".json";
        FTPClient ftpClient = null;
        String nameFile = String.valueOf(System.currentTimeMillis());
        File path = context.getExternalFilesDir(null);
        File fileLoc = new File(path, nameFile + ".json");
        OutputStream outputStream = null;
        JsonObject _return = null;

        try {
            ftpClient = ftpConnect(server, Integer.parseInt(port), usr, pass);
            ftpClient.changeWorkingDirectory(SERVERFOLDER);
            outputStream = new FileOutputStream(fileLoc);
            boolean success = ftpClient.retrieveFile(fileServer, outputStream);

            // read downloaded file
            if (success) {
                JsonParser parser = new JsonParser();
                _return = (JsonObject) parser.parse(new FileReader(fileLoc));
                Log.i("getReplayTicket", " retrived successfully.");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                    logout(ftpClient);
                } catch (IOException e) {
                    if (DEBUG)
                        Log.e("get replay ticket", "get ticket replay, finally, e: '" + e + "'");
                }
            }
        }
        return _return;
    }

    //TODO: get info by reading file from ftp server
    public static JsonObject getAccBalance(Context context, String accNo) {

        String SERVERFOLDER = "/" + context.getResources().getString(R.string.acc_balance_folder);
        String server = context.getResources().getString(R.string.server);
        String port = context.getResources().getString(R.string.port);
        String usr = context.getResources().getString(R.string.usr);
        String pass = context.getResources().getString(R.string.pass);
        String fileServer = "req_" + accNo + ".json";
        FTPClient ftpClient = null;
        String nameFile = String.valueOf(System.currentTimeMillis());
        File path = context.getExternalFilesDir(null);
        File fileLoc = new File(path, nameFile + ".json");
        OutputStream outputStream = null;

        try {
            ftpClient = ftpConnect(server, Integer.parseInt(port), usr, pass);
            ftpClient.changeWorkingDirectory(SERVERFOLDER); //server folder: "/Acct"
            outputStream = new FileOutputStream(fileLoc);
            boolean success = ftpClient.retrieveFile(fileServer, outputStream);

            // read downloaded file
            if (success) {
                JsonParser parser = new JsonParser();
                JsonObject _return = (JsonObject) parser.parse(new FileReader(fileLoc));
                Log.i("getAccBalance", " retrived successfully.");
                return _return;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("getAccBalance", "FileNotFoundException e: '" + e + "'");
            Log.e("getAccBalance","file path: " + fileLoc.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getAccBalance", "IOException e: '" + e + "'");
            Log.e("getAccBalance","file path: " + fileLoc.getAbsolutePath());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                    logout(ftpClient);
                } catch (IOException e) {
                    if (DEBUG) Log.e("getAccBalance", "get acc info, finally, e: '" + e + "'");
                }
            }
        }
        return null;
    }

    //TODO: save file with game to ftp server
    /**
     * generate local file and put it to server
     * filter file was filled games
     * rename filled games file
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean save2Sever(Context context, Games games, String dir) {
        boolean flag = false;
        File path = context.getExternalFilesDir(null);
        //get all local file within condition: local_[money]_[phone]_[orderNo]_[unique]
        int orderNo = getLocalTicket().size();
        String nameFile = games.getPhone_no() + "_" + orderNo + "_" + UUID.randomUUID().toString();
        File file = new File(path, nameFile + ".json");
        OutputStream outputStream = null;
        InputStream inputStream = null;
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting()
                .create();

        String server = context.getResources().getString(R.string.server);
        String port = context.getResources().getString(R.string.port);
        String usr = context.getResources().getString(R.string.usr);
        String pass = context.getResources().getString(R.string.pass);
        FTPClient ftpClient = null;

        try {
            ftpClient = ftpConnect(server, Integer.parseInt(port), usr, pass);

            //TODO: check acc info from registered user
            if (checkFileExists(file.getPath(), ftpClient, dir)) {
                nameFile = file.getName() + new Random().nextInt(1000000000);
                file = new File(path, nameFile + ".json");
                Log.i("save2Sever","connected and get acc info successful");
                //risk: in a session, the server just return a file with the latest acc info. If user continue play game, the acc balance not deduct
                //--> need to realtime update acc balance
            }



            //TODO: generate a new acc info
            //risk: new acc doesnt have acc balance and no way in-app to reload acc balance
            //--> need add payment method
            Log.i("save2Sever","generate a new acc balance");

            outputStream = new FileOutputStream(file);
            BufferedWriter bufferedWriter;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,
                        StandardCharsets.UTF_8));

            } else {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            }

            gson.toJson(games, bufferedWriter);
            bufferedWriter.close();
            flag = true;

            Log.i("save2sever","generate local file " + file.getAbsolutePath());


            inputStream = new FileInputStream(file);
            ftpClient.storeFile(dir + "/" + file.getName(), inputStream);

            Log.i("save2sever","updload file to server " + file.getAbsolutePath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("save2sever", "FileNotFoundException e: '" + e + "'");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("save2sever", "IOException e: '" + e + "'");
        } finally {

            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                    logout(ftpClient);
                } catch (IOException e) {
                    if (DEBUG) Log.e("SAVE_JSON", "saveUserData, finally, e: '" + e + "'");
                }
            }
        }

        //refresh dir files
        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);


        //101720lk - change file name for filter local file
        // choose only file has games
        if(!games.getGames().isEmpty()){
            reNameLocal(file,games.getTotals());
        }



        return flag;
    }

    //TODO: get file from local folder
    /**
     * @param: path of files
     * @param: fileType need to scan
     * @return: file[]
     * */
    public static File[] scanFile(String path, final String fileType){
        File dir = new File(path);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
//                return file.getAbsolutePath().matches(".*\\.json");
                return file.getAbsolutePath().matches(".*\\." + fileType);
            }
        };
        File[] files = dir.listFiles(filter);
        return files;
    }

    /**
     * Connects to a remote FTP server
     */
    public static FTPClient ftpConnect(String hostname, int port, String username, String password)
            throws IOException {
        FTPClient ftpClient = new FTPClient();
//        ftpClient.setControlEncoding("UTF-8");
//        ftpClient.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX)); //--> for linux sever
        ftpClient.connect(hostname, port);
        int returnCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(returnCode)) {
            throw new IOException("Could not connect");
        }
        boolean loggedIn = ftpClient.login(username, password);
        if (!loggedIn) {
            throw new IOException("Could not login");
        }
        System.out.println("Connected and logged in.");
        return ftpClient;
    }



    /**
     * Logs out and disconnects from the server
     */
    public static void logout(FTPClient ftpClient) throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
            System.out.println("Logged out");
        }
    }

    /**
     * Determines whether a directory exists or not
     *
     * @param dirPath
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    public static boolean checkDirectoryExists(String dirPath, FTPClient ftpClient) throws IOException {
        ftpClient.changeWorkingDirectory(dirPath);
        int returnCode = ftpClient.getReplyCode();
        return returnCode != 550;
    }

    /**
     * Determines whether a file exists or not
     *
     * @param file
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    public static boolean checkFileExists(String file, FTPClient ftpClient, String remoteDir) throws IOException {

        InputStream inputStream = ftpClient.retrieveFileStream(remoteDir + "/" + file);
        int returnCode = ftpClient.getReplyCode();
        Log.i("checkFileExists", "code-" + returnCode);
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        inputStream.close();
        return true;
    }

    /**
     * Determines whether a file exists or not
     *
     * @param file
     * @return true if exists, false otherwise
     * @throws IOException thrown if any I/O error occurred.
     */
    public static boolean checkFileExists(String file, FTPClient ftpClient) throws IOException {

        InputStream inputStream = ftpClient.retrieveFileStream(file);
        int returnCode = ftpClient.getReplyCode();
        System.out.println("checkFileExists, code-" + returnCode);
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        inputStream.close();
        return true;
    }

    //TODO: random number in range
    public static String getRandomNumberInRange(int min, int max, String formater) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return String.format(formater, r.nextInt((max - min) + 1) + min);//"%02d"
    }

    //TODO: random number in times
    public static String getRandomNumberInTimes(int times) {
        String quickPick = "";
        for (int i = 0; i < times; i++) {
            String nextStr = Ultis.getRandomNumberInRange(1, 49, "%02d");
            if (i == 0)
                quickPick += deDup(quickPick, nextStr);
            else
                quickPick += "-" + deDup(quickPick, nextStr);
        }
        return quickPick;
    }

    //TODO: random number in times with range number
    public static String getRandomNumberInTimes(int times, int min, int max) {
        String quickPick = "";
        for (int i = 0; i < times; i++) {
            String nextStr = Ultis.getRandomNumberInRange(min, max, "%02d");
            if (i == 0)
                quickPick += deDup(quickPick, nextStr, min, max);
            else
                quickPick += "-" + deDup(quickPick, nextStr, min, max);
        }
        return quickPick;
    }

    //TODO: check and generate no dup string/ recursive
    public static String deDup(String str, String subStr) {

        //011320lk - update no 50 for lucky MAX
        if (str.indexOf(subStr) != -1) {
            subStr = Ultis.getRandomNumberInRange(1, 50, "%02d");
            deDup(str, subStr);
        }
        return subStr;
    }

    //TODO: check and generate no dup string/ recursive in range
    public static String deDup(String str, String subStr, int min, int max) {

        if (str.indexOf(subStr) != -1) {
            subStr = Ultis.getRandomNumberInRange(min, max, "%02d");
            deDup(str, subStr);
        }
        return subStr;
    }

    //TODO: convert string to array with pair of each element
    public static ArrayList str2PaiArray(String str, String separator) {
        ArrayList list = new ArrayList();
        // find all occurrences forward
        for (int i = -1; (i = str.indexOf(separator, i + 1)) != -1; i += separator.length()) {
            int tmp = Integer.parseInt(str.substring(i - 2, i));

            list.add(tmp);
        }

        list.add(Integer.parseInt(str.substring(str.length() - 2)));// --> workaround for adding the last element
        return list;
    }

    //TODO: popup loading
    public static PopupWindow popLoading(Context context, View v) {

        //popup add money
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popup_loading = layoutInflater.inflate(R.layout.pop_loading, null);

        final ProgressBar progressBar_cyclic = popup_loading.findViewById(R.id.progressBar_cyclic);
        progressBar_cyclic.setVisibility(View.VISIBLE);

        //instantiate popup window
        final PopupWindow popupWindow = new PopupWindow(popup_loading, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        //get parent size and set size to popup
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        double measuredWidth;
        double measuredHeight;

        final ViewGroup root = (ViewGroup) ((Activity) context).getWindow().getDecorView().getRootView();
        //add blur for background
        Ultis.applyDim(root, 0.8f); //or clearDim()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            measuredWidth = size.x * 0.8;
            measuredHeight = size.y * 0.5;
        } else {
            Display d = wm.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }

        //set size for popup
        popupWindow.setWidth((int) measuredWidth);
        popupWindow.setHeight((int) measuredHeight);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true); //set focus for get effected from keyboard
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();

//        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        //specific layout by id --> avoid null pointer
        v = ((Activity) context).findViewById(R.id.mainView);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        // OverWrite dismiss with include clear dim and softkey
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Ultis.clearDim(root);
                progressBar_cyclic.setVisibility(View.GONE);
                popupWindow.dismiss();
            }
        });

        return popupWindow;
    }

    //TODO: read local file
    public static String readLocFile(Context context) {

        try{
            File path = context.getExternalFilesDir(null);
            String contents = FileUtils.readFileToString(new File(path + "/pin"), "UTF8");

            return contents;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    //TODO: write local file
    public static boolean writeLocFile(Context context, String content) {
        try{
            File path = context.getExternalFilesDir(null);
            File file = new File(path, "/pin");
            FileOutputStream out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //TODO: convert long date to string format date
    public static String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("MM dd yy");
        return format.format(date);
    }

    //TODO: get ticket name from ticket file name
    /**
     * @param: String fileName
     * @return: String phone_xxxxxxxx and total money
     *
     * */
    public static String[] getTicketLocal(String fileName){



        if(checkFileName(fileName)){
            int pos_money = fileName.indexOf("_");
            int pos_phone = fileName.indexOf('_',pos_money + 1);
            int pos_orderNo = fileName.lastIndexOf('_');
//            int pos_unique = fileName.indexOf('-');
            String _money = fileName.substring(pos_money + 1,pos_phone);
            String _ticketNo = fileName.substring(pos_phone + 1,pos_orderNo);
            String[] _return  = new String[]{_money,_ticketNo};
            return _return;
        }

        Log.d("getTicketLocal","check local file is incorrect");

        return null;
    }

    //TODO: check ticket name follow structure
    /**
     * @example: local_[]money]_[phone]_[orderNo]_[unique string]{8}-[random text]
     * @param: String file name
     * @return: boolean
     * */
    public static boolean checkFileName(@org.jetbrains.annotations.NotNull String fileName){

        //check1: fileName is local or not
        if(!fileName.contains("local_") || fileName.contains("_null_"))
            return false;

        //check2: fileName has structure: local_[*]_[*]_[*]_[*]
        String wordToFind = "_";
        Pattern word = Pattern.compile(wordToFind);
        Matcher match = word.matcher(fileName);
        int count_wordToFind = 0;

        while(match.find()){
            match.start();
            count_wordToFind++;

        }

        if (count_wordToFind < 4)
            return false;

        //check3: the unique string after phone no has length = 8 - magic no
        String uniqueString = fileName.substring(fileName.lastIndexOf("_") + 1,fileName.indexOf("-"));
        if (!(uniqueString.length() == 8))
            return false;

        return  true;
    }

    //TODO: check file last modified
    /**
     * @param: File file
     * @process: check currentday - file.lastModified > 30 --> delete file
     * @return: boolean
     * @ex data: 86400000 - 1 day | 2592000000 - 1 month | 777600000L - 3 months
     * */

    public static boolean deleteOldFile(File file){
        // Note the L that tells the compiler to interpret the number as a Long
        final long MAXFILEAGE = 86400000; // 1 month in milliseconds
        if(System.currentTimeMillis() - file.lastModified() > MAXFILEAGE ){
            file.delete();
            return !file.exists();
        }

        return false;
    }

}
