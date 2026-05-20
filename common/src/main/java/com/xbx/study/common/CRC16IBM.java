package com.xbx.study.common;

/**
 * CRC16-IBM 循环冗余校验算法实现类
 * <p>
 * 该类实现了 CRC16-IBM（也称为 CRC16/ARC、CRC16/LHA）算法，
 * 用于计算数据的 16 位循环冗余校验值。
 * </p>
 * 
 * <p>
 * 算法参数：
 * <ul>
 *   <li>多项式：0xA001（反转形式）</li>
 *   <li>初始值：0x0000</li>
 *   <li>输入反转：是</li>
 *   <li>输出反转：是</li>
 * </ul>
 * </p>
 *
 * @author xbx
 * @version 1.0
 */
public class CRC16IBM {
    /** CRC 校验值的内部状态 */
    protected int wCRCin;

    /**
     * 获取当前 CRC 校验值
     *
     * @return CRC 校验值（long 类型）
     */
    public long getValue() {
        return (long)this.wCRCin;
    }

    /**
     * 使用指定的字节数组更新 CRC 校验值
     * <p>
     * 该方法对给定字节数组的指定范围进行逐字节处理，
     * 通过异或和位移运算计算 CRC16 校验值。
     * </p>
     *
     * @param data 要计算的字节数组
     * @param off  起始偏移量
     * @param len  处理的字节长度
     */
    public void update(byte[] data, int off, int len){
        for(int i = off; i < off + len; i++){
            int b = data[i];
            this.wCRCin ^= b & 255;

            for(int j = 0; j < 8; ++j) {
                if ((this.wCRCin & 1) != 0) {
                    this.wCRCin >>= 1;
                    this.wCRCin ^= 40961;
                } else {
                    this.wCRCin >>= 1;
                }
            }
        }
    }


}
