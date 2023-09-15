package com.xiaoleilu.loServer;

import cn.wildfirechat.proto.WFCMessage;
import io.moquette.BrokerConstants;
import io.moquette.server.config.IConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import win.liyufan.im.DBUtil;
import win.liyufan.im.MessageShardingUtil;
import win.liyufan.im.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.wildfirechat.common.IMExceptionEvent.EventType.RDBS_Exception;

/**
 * @author Rain
 * @date 2023/9/14 10:33
 */
public class FileBackupServer {
    private final Logger LOG = LoggerFactory.getLogger(FileBackupServer.class);
    private boolean encryptMessage;
    private int normalFileSaveTime, groupBackupFileSaveTime;

    public void start(IConfig config) {
        DBUtil.init(config);

        encryptMessage = Boolean.parseBoolean(config.getProperty(BrokerConstants.MESSAGES_ENCRYPT_MESSAGE_CONTENT, "false"));
        normalFileSaveTime = Integer.parseInt(config.getProperty(BrokerConstants.FILE_SERVER_NORMAL_FILE_SAVE_TIME, "7"));
        groupBackupFileSaveTime = Integer.parseInt(config.getProperty(BrokerConstants.FILE_SERVER_GROUP_BACKUP_FILE_SAVE_TIME, "60"));

        if ("true".equals(config.getProperty(BrokerConstants.FILE_SERVER_AUTO_BACKUP_GROUP_FILE))) {
            startBackupGroupFile();
        }
        if ("true".equals(config.getProperty(BrokerConstants.FILE_SERVER_AUTO_CLEAR_FILE))) {
            startClearFile();
        }
    }

    //region 文件清理

    private ScheduledExecutorService clearFileExecutor;

    private void startClearFile() {
        clearFileExecutor = Executors.newScheduledThreadPool(1);
        clearFile();
    }

    private void clearFile() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long delayTime = calendar.getTimeInMillis() - System.currentTimeMillis();
        LOG.info("clear file task delay {}", Duration.ofMillis(delayTime));
        if (DBUtil.ClearDBDebugMode) {
            delayTime = 60 * 1000;
        }
        clearFileExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                LOG.info("start clear file task delay");

                File root = ServerSetting.getRoot();

                File fsDir = new File(root, "fs");
                File backupDir = new File(root, "backup" + File.separator + "fs");

                try {
                    clearFile(fsDir, normalFileSaveTime);
                    clearFile(backupDir, groupBackupFileSaveTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                clearFile();
            }
        }, delayTime, TimeUnit.MILLISECONDS);
    }

    private void clearFile(File parentDir, int saveDay) {
        LOG.info("start clear dir {} max day {}", parentDir, saveDay);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -saveDay);

        String[] yearsList = parentDir.list();
        if (yearsList == null) {
            LOG.warn("years list null, {}", parentDir);
            return;
        }
        Calendar calendarTemp = Calendar.getInstance();
        calendarTemp.set(Calendar.MINUTE, 0);
        calendarTemp.set(Calendar.SECOND, 0);
        calendarTemp.set(Calendar.MILLISECOND, 0);

        File[] buckets = parentDir.listFiles();
        if (buckets == null) {
            return;
        }
        for (File bucket : buckets) {
            if ("5".equals(bucket.getName()))// 表情包
                continue;
            for (File yearDir : listFilesAndDeleteEmptyDirectory(bucket)) {
                for (File monthDir : listFilesAndDeleteEmptyDirectory(yearDir)) {
                    for (File dayDir : listFilesAndDeleteEmptyDirectory(monthDir)) {
                        for (File hourDir : listFilesAndDeleteEmptyDirectory(dayDir)) {
                            if (!hourDir.exists() || !hourDir.isDirectory())
                                continue;
                            int year = Integer.parseInt(yearDir.getName());
                            int month = Integer.parseInt(monthDir.getName());
                            int day = Integer.parseInt(dayDir.getName());
                            int hour = Integer.parseInt(hourDir.getName());

                            calendarTemp.set(Calendar.YEAR, year);
                            calendarTemp.set(Calendar.MONTH, month - 1);
                            calendarTemp.set(Calendar.DAY_OF_MONTH, day);
                            calendarTemp.set(Calendar.HOUR_OF_DAY, hour);

                            if (calendarTemp.getTimeInMillis() < calendar.getTimeInMillis()) {
                                deleteDirectory(hourDir);
                            }
                        }
                    }
                }
            }
        }
    }

    private File[] listFilesAndDeleteEmptyDirectory(File parentDir) {
        if (!parentDir.exists()) {
            LOG.warn("clear file fail, {} not exists", parentDir);
            return new File[0];
        }
        if (!parentDir.isDirectory()) {
            LOG.warn("clear file fail, {} not is directory", parentDir);
            return new File[0];
        }
        File[] listFiles = parentDir.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            deleteDirectory(parentDir);
            return new File[0];
        }
        return listFiles;
    }

    private void deleteDirectory(File dir) {
        LOG.info("delete dir {}", dir);
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion

    private long backupGroupFileStartMid;

    private void startBackupGroupFile() {
        backupGroupFileStartMid = MessageShardingUtil.getMsgIdFromTimestamp(System.currentTimeMillis());
        new Thread(() -> {
            long usedTime = 0;
            while (true) {
                if (DBUtil.SystemExiting) {
                    break;
                }
                try {
                    long sleepTime;
                    if (DBUtil.ClearDBDebugMode) {
                        sleepTime = 60 * 1000;
                    } else {
                        sleepTime = 60 * 60 * 1000 - usedTime;
                    }
                    if (sleepTime < 0) {
                        sleepTime = 5 * 1000;
                    }

                    Thread.sleep(sleepTime);
                    LOG.info("Start backup group file from messages");

                    if (DBUtil.SystemExiting) {
                        break;
                    }
                    long start = System.currentTimeMillis();
                    backupGroupFile();
                    usedTime = System.currentTimeMillis() - start;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private final Pattern patternFilePath = Pattern.compile("\\/fs\\/(\\d)\\/(\\d\\d\\d\\d)\\/(\\d\\d)\\/(\\d\\d)\\/(\\d\\d)\\/(.*)$");

    private void backupGroupFile() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        long startMid = backupGroupFileStartMid;
        long endTime = System.currentTimeMillis();
        long endMid = MessageShardingUtil.getMsgIdFromTimestamp(endTime);
        String tableName = MessageShardingUtil.getMessageTable(startMid);

        try {
            connection = DBUtil.getConnection();
            String sql = "SELECT _mid, _data from " + tableName + " where _type = 1 and _content_type in (2,3,5,6) and _mid > " + startMid + " and _mid <= " + endMid;
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                int index = 1;
                long mid = rs.getLong(index++);
                Blob blob = rs.getBlob(index++);

                WFCMessage.MessageContent messageContent = WFCMessage.MessageContent.parseFrom(encryptMessageContent(toByteArray(blob.getBinaryStream()), false));
                String remoteMediaUrl = messageContent.getRemoteMediaUrl();
                Matcher matcher = patternFilePath.matcher(remoteMediaUrl);
                if (matcher.find()) {
                    String fullPath = matcher.group();
                    String bucket = matcher.group(1);
                    String year = matcher.group(2);
                    String month = matcher.group(3);
                    String day = matcher.group(4);
                    String hour = matcher.group(5);
                    String filename = matcher.group(6);

                    File root = ServerSetting.getRoot();
                    File sourceFile = new File(root.getAbsolutePath() + fullPath);
                    if (!sourceFile.exists() || !sourceFile.isFile() || sourceFile.length() == 0)
                        continue;
                    File targetFile = new File(root.getAbsolutePath() + File.separator + "backup" + fullPath);
                    if (targetFile.exists()) {
                        LOG.warn("backup target file is exists {}=>{}", sourceFile, targetFile);
                        continue;
                    }
                    LOG.info("backup file to {}=>{}", sourceFile, targetFile);
//                    FileUtils.copyFile(sourceFile, targetFile);
                    FileUtils.moveFile(sourceFile, targetFile);
                }
                backupGroupFileStartMid = mid;
            }
            if (rowCount == 0) {
                backupGroupFileStartMid = endMid;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utility.printExecption(LOG, e, RDBS_Exception);
        } finally {
            DBUtil.closeDB(connection, statement, rs);
        }
    }

    private byte[] encryptMessageContent(byte[] in, boolean force) {
        if (in != null && (encryptMessage || force)) {
            for (int i = 0; i < in.length; i++) {
                in[i] ^= 0xBD;
            }
        }
        return in;
    }

    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }


    public void shutdown() {
        clearFileExecutor.shutdown();
    }
}
