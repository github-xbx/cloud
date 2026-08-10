package com.xbx.study.dubbo.rpc;

import com.xbx.study.dubbo.common.apis.DubboDemoService;
import com.xbx.study.dubbo.service.DubboDemoDelegate;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;


@Component
@DubboService
public class DubboDemoServiceImpl implements DubboDemoService {


    private final DubboDemoDelegate delegate;

    public DubboDemoServiceImpl(DubboDemoDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String hello(String name) {

        return delegate.hello(name);
    }
}
