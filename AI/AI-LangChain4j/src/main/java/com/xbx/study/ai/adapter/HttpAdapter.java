package com.xbx.study.ai.adapter;

import com.xbx.study.ai.service.BaseAiChat;
import org.springframework.stereotype.Component;

/**
 * http 适配器 直接返回不用
 */

public class HttpAdapter extends Target{


    public HttpAdapter(BaseAiChat baseAiChat) {
        super(baseAiChat);
    }
}
