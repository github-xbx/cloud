package com.xbx.study.netty.collect_client.domain;

public record PaveRecord(
        Long id,
        Integer locationStatus,
        Integer temperatureStatus,
        Integer waveStatus,
        Double longitude,
        Double latitude,
        Double speed,
        Double direction,
        Double temperature,
        Double waveAX,
        Double waveAY,
        Double waveAZ) {



//    /** 时间戳 */
//    private Long id;
//
//    /** 定位传感器状态 */
//    private Integer locationStatus;
//
//    /** 温度传感器状态 */
//    private Integer temperatureStatus;
//
//    /** 振动传感器状态 */
//    private Integer waveStatus;
//
//    /** 经度 */
//    private Double longitude;
//
//    /** 纬度 */
//    private Double latitude;
//
//    /** 对地速度 */
//    private Double speed;
//
//    /** 对地方向 */
//    private Double direction;
//
//    /** 温度 */
//    private Double temperature;
//
//    /** 振动-加速度X(AX) */
//    private Double waveAX;
//
//    /** 振动-加速度Y(AY) */
//    private Double waveAY;
//
//    /** 振动-加速度Z(AZ) */
//    private Double waveAZ;

}
