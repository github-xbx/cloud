package com.xbx.study.ai.service;

import com.xbx.study.ai.event.AGUIEvent;
import reactor.core.publisher.Flux;

/**
 * AG-UI Protocol java 实现
 */
public class AGUIProtocol {




    // 工具类

    public static <T> Flux<T> withEnvelope(Flux<T> source, T header, T footer) {
        return Flux.concat(Flux.just(header), source, Flux.just(footer));
    }







    public Flux<AGUIEvent> chat(){
        return null;
    }



}
