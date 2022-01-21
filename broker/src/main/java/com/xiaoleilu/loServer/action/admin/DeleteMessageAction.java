package com.xiaoleilu.loServer.action.admin;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.DeleteMessageData;
import cn.wildfirechat.pojos.InputDestoryChatroom;
import com.google.gson.Gson;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;
import io.moquette.spi.impl.Utils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.StringUtil;

@Route(APIPath.Msg_Delete)
@HttpMethod("POST")
public class DeleteMessageAction extends AdminAction {
    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest)request.getNettyRequest();
            DeleteMessageData deleteMessageData = getRequestBody(fullHttpRequest, DeleteMessageData.class);
            if (deleteMessageData != null) {
                messagesStore.deleteMessage(deleteMessageData.getMessageUid());
                setResponseContent(RestResult.ok(), response);
            } else {
                setResponseContent(RestResult.resultOf(ErrorCode.INVALID_PARAMETER), response);
            }
        }
        return true;
    }
}
