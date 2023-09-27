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
    /**
     * 普通消息文件保存时间
     */
    private int normalFileSaveTime;
    /**
     * 群聊消息文件保存时间
     */
    private int groupBackupFileSaveTime;
    private boolean clearFile,backupGroupFile;
    private ScheduledExecutorService executorService;
    private long backupGroupFileStartMid;

    public void start(IConfig config) {
        DBUtil.init(config);

        encryptMessage = Boolean.parseBoolean(config.getProperty(BrokerConstants.MESSAGES_ENCRYPT_MESSAGE_CONTENT, "false"));
        normalFileSaveTime = Integer.parseInt(config.getProperty(BrokerConstants.FILE_SERVER_NORMAL_FILE_SAVE_TIME, "7"));
        groupBackupFileSaveTime = Integer.parseInt(config.getProperty(BrokerConstants.FILE_SERVER_GROUP_BACKUP_FILE_SAVE_TIME, "60"));

        if ("true".equals(config.getProperty(BrokerConstants.FILE_SERVER_AUTO_BACKUP_GROUP_FILE))) {
            backupGroupFile = true;
            backupGroupFileStartMid = MessageShardingUtil.getMsgIdFromTimestamp(System.currentTimeMillis());
        }
        if ("true".equals(config.getProperty(BrokerConstants.FILE_SERVER_AUTO_CLEAR_FILE))) {
            clearFile = true;
        }
        if (backupGroupFile || clearFile){
            executorService = Executors.newScheduledThreadPool(1);
            startDelayTask();
        }
    }

    private void startDelayTask() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long delayTime = calendar.getTimeInMillis() - System.currentTimeMillis();
        LOG.info("file task delay {}", Duration.ofMillis(delayTime));
        if (DBUtil.ClearDBDebugMode) {
            delayTime = 60 * 1000;
        }
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (DBUtil.SystemExiting)return;

                if (backupGroupFile){
                    LOG.info("Start backupGroupFile");
                    long start = System.currentTimeMillis();
                    backupGroupFile();
                    long usedTime = System.currentTimeMillis() - start;
                    LOG.info("backupGroupFile use "+usedTime);
                }
                if (clearFile){
                    LOG.info("Start clearFile");
                    long start = System.currentTimeMillis();
                    clearFile();
                    long usedTime = System.currentTimeMillis() - start;
                    LOG.info("clearFile use "+usedTime);
                }

                startDelayTask();
            }
        }, delayTime, TimeUnit.MILLISECONDS);
    }

    //region 文件清理

    /**
     * 根据文件路径规则 判断时间 删除
     */
    private void clearFile(){
        LOG.info("start clear file task delay");
        File root = ServerSetting.getRoot();
        File fsDir = new File(root, "fs");
        File backupDir = new File(root, "backup" + File.separator + "fs");
        clearFile(fsDir, normalFileSaveTime);
        clearFile(backupDir, groupBackupFileSaveTime);
    }

    private void clearFile(File parentDir, int saveDay) {
        LOG.info("start clear dir {} max day {}", parentDir, saveDay);
        File[] buckets = parentDir.listFiles();
        if (buckets == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -saveDay);

        Calendar calendarTemp = Calendar.getInstance();
        calendarTemp.set(Calendar.MINUTE, 0);
        calendarTemp.set(Calendar.SECOND, 0);
        calendarTemp.set(Calendar.MILLISECOND, 0);
        for (File bucket : buckets) {
            if (!bucket.isDirectory())
                continue;
            if (
                "1".equals(bucket.getName()) //图片
                    || "2".equals(bucket.getName())// 语音
                    || "3".equals(bucket.getName())// 视频
                    || "4".equals(bucket.getName())// 文件
            ) {
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

    private final Pattern patternFilePath = Pattern.compile("\\/fs\\/(\\d)\\/(\\d\\d\\d\\d)\\/(\\d\\d)\\/(\\d\\d)\\/(\\d\\d)\\/(.*)$");

    /**
     * 从im数据库查询群聊消息 包括图片视频语音文件四种
     * 再判断本地文件是否存在 移动到backup目录下去
     */
    private void backupGroupFile() {
        LOG.info("Start backup group file from messages");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        long startMid = backupGroupFileStartMid;
        String tableName = MessageShardingUtil.getMessageTable(startMid);
        long endMid;

        {
            long startT = MessageShardingUtil.getTimestampFromMsgId(startMid);
            Calendar startC = Calendar.getInstance();
            startC.setTimeInMillis(startT);

            long endT = System.currentTimeMillis();
            Calendar endC = Calendar.getInstance();
            endC.setTimeInMillis(endT);
            if (startC.get(Calendar.MONTH) != endC.get(Calendar.MONTH)) {
                // 分表是按月来的 避免跨表时 第二张表 部分消息查不到
                endC.set(Calendar.DAY_OF_MONTH, 0);
                endC.set(Calendar.HOUR_OF_DAY, 0);
                endC.set(Calendar.MINUTE, 0);
                endC.set(Calendar.SECOND, 0);
                endC.set(Calendar.MILLISECOND, 0);
                endT = endC.getTimeInMillis();
            }
            endMid = MessageShardingUtil.getMsgIdFromTimestamp(endT);
        }

        try {
            connection = DBUtil.getConnection();
            String sql = "SELECT _data from " + tableName + " where _type = 1 and _content_type in (2,3,5,6) and _mid >= " + startMid + " and _mid < " + endMid;
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();

            while (rs.next()) {
                int index = 1;
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
                    FileUtils.moveFile(sourceFile, targetFile);
                }
            }
            backupGroupFileStartMid = endMid;
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
        executorService.shutdown();
    }
}
