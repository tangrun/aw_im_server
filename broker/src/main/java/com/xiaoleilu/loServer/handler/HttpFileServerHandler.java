package com.xiaoleilu.loServer.handler;

import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.*;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.hazelcast.util.StringUtil;
import com.xiaoleilu.hutool.util.*;
import com.xiaoleilu.loServer.ServerSetting;
import com.xiaoleilu.loServer.action.UploadFileAction;
import io.moquette.server.config.MediaServerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import win.liyufan.im.GsonUtil;

import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.xiaoleilu.loServer.handler.HttpResponseHelper.getFileExt;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpFileServerHandler.class);

    String HTTP_DATE_GMT_TIMEZONE = "GMT";
    int HTTP_CACHE_SECONDS = 60;

    //    MimetypesFileTypeMap mimeTypesMap  = new MimetypesFileTypeMap();
    Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    SimpleDateFormat dateFormatter = new SimpleDateFormat(DateUtil.HTTP_DATETIME_PATTERN, Locale.US);

    {
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
    }

    private static final HttpDataFactory factory =
        new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed


    private HttpPostRequestDecoder decoder;

    private FullHttpRequest request;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        this.request = request;
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, request, BAD_REQUEST);
            return;
        }
        if (request.method() == HttpMethod.GET) {
            downloadFile(ctx, request);
        } else if (request.method() == HttpMethod.POST) {
            uploadFile(ctx, request);
            if (decoder != null) {
                decoder.destroy();
                decoder = null;
            }
        } else {
            sendError(ctx, request, METHOD_NOT_ALLOWED);
        }
        this.request = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, request, INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    private void downloadFile(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {

        if (!ServerSetting.isRootAvailable()) {
            sendError(ctx, request, SERVICE_UNAVAILABLE);
            return;
        }

        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        final String uri = request.uri();

        File file = getFileByPath(uri);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            sendError(ctx, request, FORBIDDEN);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifModifiedSinceDate = null;
            try {
                ifModifiedSinceDate = DateUtil.parse(ifModifiedSince, dateFormatter);
            } catch (Exception e) {
                logger.warn("If-Modified-Since header parse error: {}", e.getMessage());
            }
            if (ifModifiedSinceDate != null) {
                // 只对比到秒一级别
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    logger.debug("File {} not modified.", file.getPath());
                    sendNotModified(ctx, request);
                    return;
                }
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, request, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);

        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                    ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    logger.debug(future.channel() + " Transfer progress: " + progress);
                } else {
                    logger.debug(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                logger.debug(future.channel() + " Transfer complete.");
            }
        });

        // Decide whether to close the connection or not.
        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private void uploadFile(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        try {
            decoder = new HttpPostRequestDecoder(factory, request);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
            e1.printStackTrace();
            sendError(ctx, request, BAD_REQUEST);
            return;
        }
        try {
            decoder.offer(request);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
            e1.printStackTrace();
            writeResponseError(ctx.channel(), BAD_REQUEST, true);
            return;
        }

        try {
            String token = null;
            FileUpload fileUpload = null;
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        if ("token".equals(attribute.getName()))
                            token = attribute.getValue();
                    } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        fileUpload = (FileUpload) data;
                    }
                    if (token != null && fileUpload != null) {
                        break;
                    }
                }
            }
            if (token == null) {
                token = request.headers().get("token");
            }
            if (token == null || fileUpload == null) {
                logger.warn("token or file is null");
                writeResponseError(ctx.channel(), BAD_REQUEST, false);
                return;
            }
            int bucket = -1;
            try {
                bucket = UploadFileAction.validateToken(token);
            } catch (UploadFileAction.InvalidateTokenExecption e) {
                logger.warn("无效的token!", e);
                writeResponseError(ctx.channel(), UNAUTHORIZED, false);
                return;
            }
            if (bucket == -1) {
                logger.warn("bucket == -1");
                writeResponseError(ctx.channel(), BAD_REQUEST, false);
                return;
            }

            if (!fileUpload.isCompleted()) {
                logger.warn("file not completed");
                writeResponseError(ctx.channel(), BAD_REQUEST, false);
                return;
            }

            String remoteFileName = fileUpload.getFilename();
            long remoteFileSize = fileUpload.length();

            if (remoteFileSize > 200 * 1024 * 1024) {
                logger.warn("file over limite!(" + remoteFileSize + ")");
                writeResponseError(ctx.channel(), REQUEST_ENTITY_TOO_LARGE, false);
                return;
            }

            String remoteFileExt = "";
            if (remoteFileName.lastIndexOf(".") == -1) {
                remoteFileExt = "octetstream";
                remoteFileName = remoteFileName + "." + remoteFileExt;

            } else {
                remoteFileExt = getFileExt(remoteFileName);
            }

            if (StringUtil.isNullOrEmpty(remoteFileExt) || remoteFileExt.equals("ing")) {
                logger.warn("Invalid file extention name");
                writeResponseError(ctx.channel(), BAD_REQUEST, false);
                return;
            }


            Date nowTime = new Date();
            SimpleDateFormat time = new SimpleDateFormat("yyyy/MM/dd/HH");

            String relativeDir = "fs/" + bucket + "/" + time.format(nowTime);

            File dirFile = new File(MediaServerConfig.FileServerLocalDir + "/" + relativeDir);
            boolean bFile = dirFile.exists();

            if (!bFile) {
                bFile = dirFile.mkdirs();
                if (!bFile) {
                    writeResponseError(ctx.channel(), INTERNAL_SERVER_ERROR, false);
                    return;
                }
            }

            String requestId = UUID.randomUUID().toString().replace("-", "");

            File tmpFile = new File(dirFile, (StringUtil.isNullOrEmpty(remoteFileName) ? requestId : remoteFileName));
            String relativePath = relativeDir + "/" + tmpFile.getName();
            logger.info("the file path is " + tmpFile.getAbsolutePath());

            fileUpload.renameTo(tmpFile);
            decoder.removeHttpDataFromClean(fileUpload);

            if (bucket == 5) {
                // 头像违规检测
                try {
                    AliyunGreenImage.Level level = AliyunGreenImage.invokeFunction(tmpFile);
                    if (level != AliyunGreenImage.Level.none) {
                        FileUtil.copy(tmpFile, new File(MediaServerConfig.FileServerLocalDir + "/fs/weiguitouxiang/" + level.toString() + "/" + tmpFile.getName()), true);
                        String weiguitouxiang = "/fs/static/avatar.png";
                        writeResponseSuccess(ctx.channel(), "{\"key\":\"" + weiguitouxiang + "\"}");
                        return;
                    }
                } catch (Exception e) {
                    logger.error("头像监测失败", e);
                }
            }

            writeResponseSuccess(ctx.channel(), "{\"key\":\"" + relativePath + "\"}");

        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end
            writeResponseError(ctx.channel(), BAD_REQUEST, false);
        }

    }

    private void writeResponseSuccess(Channel channel, String msg) {
        writeResponse(channel, OK, msg, false);
    }

    private void writeResponseError(Channel channel, HttpResponseStatus status, boolean forceClose) {
        writeResponse(channel, status, "Failure: " + status + "\r\n", forceClose);
    }

    private void writeResponse(Channel channel, HttpResponseStatus status, String msg, boolean forceClose) {
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);

        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request) && !forceClose;

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        Set<io.netty.handler.codec.http.cookie.Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendNotModified(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
        setDateHeader(response);


        sendAndCleanupConnection(ctx, request, response);
    }

    private void sendError(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, request, response);
    }

    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 通过URL中的path获得文件的绝对路径
     *
     * @param httpPath Http请求的Path
     * @return 文件绝对路径
     */
    public File getFileByPath(String httpPath) {
        // Decode the path.
        try {
            httpPath = URLDecoder.decode(httpPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (httpPath.isEmpty() || httpPath.charAt(0) != '/') {
            return null;
        }

        // 路径安全检查
        String path = httpPath.substring(0, httpPath.lastIndexOf("/"));
        if (path.contains("/.") || path.contains("./") || ReUtil.isMatch(INSECURE_URI, path)) {
            return null;
        }

        // 转换为绝对路径
        File file = FileUtil.file(ServerSetting.getRoot(), httpPath);
        if (!file.exists()) {
            file = FileUtil.file(ServerSetting.getRoot(), "/backup/" + httpPath);
        }
        return file;
    }


    /**
     * Sets the Date header for the HTTP response
     *
     * @param response HTTP response
     */
    private void setDateHeader(FullHttpResponse response) {
//        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
//        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }


    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response    HTTP response
     * @param fileToCache file to extract content type
     */
    private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
//        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
//        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
            HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
    private void setContentTypeHeader(HttpResponse response, File file) {
//        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String contentType = com.xiaoleilu.hutool.http.HttpUtil.getMimeType(file.getName());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType == null ? "application/octet-stream" : contentType);
    }


    public static class AliyunGreenImage {

        //服务是否部署在vpc上
        public static boolean isVPC = false;

        //文件上传token endpoint->token
        public static Map<String, Pair<DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData, OSS>> tokenMap;

        public static Map<String, Client> clientMap;
        public static String accessKeyId = "LTAI5tNVgo5QpHQEE8MPW13p";
        public static String accessKeySecret = "3P6f7AdFMe1wDbWL4mUbbWHstx1KOr";

        /**
         * 创建请求客户端
         *
         * @param endpoint
         * @return
         * @throws Exception
         */
        private static Client getClient(String endpoint) throws Exception {
            if (clientMap == null) {
                clientMap = new HashMap<>();
            }
            Client client = clientMap.get(endpoint);
            if (client == null) {
                Config config = new Config();
                config.setAccessKeyId(accessKeyId);
                config.setAccessKeySecret(accessKeySecret);
                // 设置http代理。
                //config.setHttpProxy("http://10.10.xx.xx:xxxx");
                // 设置https代理。
                //config.setHttpsProxy("https://10.10.xx.xx:xxxx");
                // 接入区域和地址请根据实际情况修改
                // 接入地址列表：https://help.aliyun.com/document_detail/467828.html?#section-uib-qkw-0c8
                config.setEndpoint(endpoint);
                client = new Client(config);
                clientMap.put(endpoint, client);
            }
            return client;
        }

        private static Pair<DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData, OSS> getOssTokenAndClient(String endpoint) throws Exception {
            if (tokenMap == null) {
                tokenMap = new HashMap<>();
            }
            Pair<DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData, OSS> pair = tokenMap.get(endpoint);
            if (pair == null || pair.getKey().expiration <= System.currentTimeMillis() / 1000) {
                Client client = getClient(endpoint);
                DescribeUploadTokenResponse tokenResponse = client.describeUploadToken();
                DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData tokenData = tokenResponse.getBody().getData();
                OSS oss = null;
                if (isVPC) {
                    oss = new OSSClientBuilder().build(tokenData.ossInternalEndPoint, tokenData.getAccessKeyId(), tokenData.getAccessKeySecret(), tokenData.getSecurityToken());
                } else {
                    oss = new OSSClientBuilder().build(tokenData.ossInternetEndPoint, tokenData.getAccessKeyId(), tokenData.getAccessKeySecret(), tokenData.getSecurityToken());
                }
                pair = new Pair<>(tokenData, oss);
                tokenMap.put(endpoint, pair);
            }
            return pair;
        }

        public static ImageModerationResponse invokeFunction(String endpoint,
                                                             String imgUrl,
                                                             File imgFile) throws Exception {
            Client client = getClient(endpoint);

            // 创建RuntimeObject实例并设置运行参数
            RuntimeOptions runtime = new RuntimeOptions();
            // 检测参数构造。
            Map<String, String> serviceParameters = new HashMap<>();
            // url方式
            if (StrUtil.isNotBlank(imgUrl)) {
                //公网可访问的URL。
                serviceParameters.put("imageUrl", imgUrl);
                //待检测数据唯一标识
                serviceParameters.put("dataId", UUID.randomUUID().toString());
            }
            //本地文件模式
            if (imgFile != null) {
                //上传文件
                //获取文件上传token
                Pair<DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData, OSS> pair = getOssTokenAndClient(endpoint);
                DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData tokenData = pair.getKey();

                String objectName = uploadFile(tokenData, pair.getValue(), imgFile);
                serviceParameters.put("ossBucketName", tokenData.getBucketName());
                serviceParameters.put("ossObjectName", objectName);
                serviceParameters.put("dataId", UUID.randomUUID().toString());
            }

            ImageModerationRequest request = new ImageModerationRequest();
            // 图片检测service：内容安全控制台图片增强版规则配置的serviceCode，示例：baselineCheck
            // 支持service请参考：https://help.aliyun.com/document_detail/467826.html?0#p-23b-o19-gff
            request.setService("profilePhotoCheck");
            request.setServiceParameters(GsonUtil.gson.toJson(serviceParameters));

            ImageModerationResponse response = null;
            try {
                response = client.imageModerationWithOptions(request, runtime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }


        /**
         * 上传文件
         *
         * @param file
         * @param tokenData
         * @return
         * @throws Exception
         */
        private static String uploadFile(DescribeUploadTokenResponseBody.DescribeUploadTokenResponseBodyData tokenData, OSS ossClient, File file) {
            String[] split = file.getAbsolutePath().split("\\.");
            String objectName;
            if (split.length > 1) {
                objectName = tokenData.getFileNamePrefix() + UUID.randomUUID() + "." + split[split.length - 1];
            } else {
                objectName = tokenData.getFileNamePrefix() + UUID.randomUUID();
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(tokenData.getBucketName(), objectName, file);
            ossClient.putObject(putObjectRequest);
            return objectName;
        }


        public static Level invokeFunction(File image) throws Exception {
            /**
             * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
             * 常见获取环境变量方式：
             * 方式一：
             *     获取RAM用户AccessKey ID：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
             *     获取RAM用户AccessKey Secret：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
             * 方式二：
             *     获取RAM用户AccessKey ID：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID");
             *     获取RAM用户AccessKey Secret：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
             */
            // 接入区域和地址请根据实际情况修改。
            ImageModerationResponse response = invokeFunction("green-cip.cn-chengdu.aliyuncs.com", null, image);
            try {
                // 自动路由。
                if (response != null) {
                    //区域切换到cn-beijing。
                    if (500 == response.getStatusCode() || (response.getBody() != null && 500 == (response.getBody().getCode()))) {
                        // 接入区域和地址请根据实际情况修改。
                        response = invokeFunction("green-cip.cn-beijing.aliyuncs.com", null, image);
                    }
                }
                // 打印检测结果。
                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        ImageModerationResponseBody body = response.getBody();
                        if (body.getCode() == 200) {
                            ImageModerationResponseBody.ImageModerationResponseBodyData data = body.getData();
                            logger.info("头像检测 data: {}", GsonUtil.gson.toJson(data));
                            if (StrUtil.equals(data.getRiskLevel(), "high")) {
                                return Level.high;
                            }
                            if (StrUtil.equals(data.getRiskLevel(), "medium")) {
                                return Level.medium;
                            }
                            if (StrUtil.equals(data.getRiskLevel(), "low")) {
                                return Level.low;
                            }
                        } else {
                            logger.warn("头像检测 image moderation not success. code: {}", body.getCode());
                        }
                    } else {
                        logger.warn("头像检测 response not success. status: {}", response.getStatusCode());
                    }
                }
            } catch (Exception e) {
                logger.error("头像检测 error.", e);
            }
            return Level.none;
        }

        public enum Level {
            high, medium, low, none
        }
    }

    public static class Pair<K, V> implements Serializable {

        /**
         * Key of this <code>Pair</code>.
         */
        private K key;

        /**
         * Gets the key for this pair.
         *
         * @return key for this pair
         */
        public K getKey() {
            return key;
        }

        /**
         * Value of this this <code>Pair</code>.
         */
        private V value;

        /**
         * Gets the value for this pair.
         *
         * @return value for this pair
         */
        public V getValue() {
            return value;
        }

        /**
         * Creates a new pair
         *
         * @param key   The key for this pair
         * @param value The value to use for this pair
         */
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * <p><code>String</code> representation of this
         * <code>Pair</code>.</p>
         *
         * <p>The default name/value delimiter '=' is always used.</p>
         *
         * @return <code>String</code> representation of this <code>Pair</code>
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }

        /**
         * <p>Generate a hash code for this <code>Pair</code>.</p>
         *
         * <p>The hash code is calculated using both the name and
         * the value of the <code>Pair</code>.</p>
         *
         * @return hash code for this <code>Pair</code>
         */
        @Override
        public int hashCode() {
            // name's hashCode is multiplied by an arbitrary prime number (13)
            // in order to make sure there is a difference in the hashCode between
            // these two parameters:
            //  name: a  value: aa
            //  name: aa value: a
            return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
        }

        /**
         * <p>Test this <code>Pair</code> for equality with another
         * <code>Object</code>.</p>
         *
         * <p>If the <code>Object</code> to be tested is not a
         * <code>Pair</code> or is <code>null</code>, then this method
         * returns <code>false</code>.</p>
         *
         * <p>Two <code>Pair</code>s are considered equal if and only if
         * both the names and values are equal.</p>
         *
         * @param o the <code>Object</code> to test for
         *          equality with this <code>Pair</code>
         * @return <code>true</code> if the given <code>Object</code> is
         * equal to this <code>Pair</code> else <code>false</code>
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Pair) {
                Pair pair = (Pair) o;
                if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
                if (value != null ? !value.equals(pair.value) : pair.value != null) return false;
                return true;
            }
            return false;
        }
    }
}
