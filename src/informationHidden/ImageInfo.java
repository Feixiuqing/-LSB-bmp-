package informationHidden;

import java.util.Arrays;

public class ImageInfo {
    /**
     * 默认偏移量
     */
    static final int OFF_BITS_24 = 54;
    static final int  OFF_BITS_8 = 1078;

    private int height = -1;
    private int width = -1;

    byte[] fileHeader = new byte[14];
    byte[] informationHeader = new byte[40];

    int[][] red, green, blue;
    int[][] color;

    /**
     * 判断位图
     *
     * @return true/false
     */
    boolean isBmpType() {
            return (fileHeader[0] == 0X42 && fileHeader[1] == 0X4D);
    }

    boolean is24BitCount() {
        return informationHeader[15] == 0 && informationHeader[14] == 24;
    }

    boolean is8BitCount(){ return informationHeader[15] == 0 && informationHeader[14] == 8; }
    /**
     * 初始化位图数据矩阵
     * @return true:成功;false:失败
     */
    boolean initBitmapDataArr() {
        int imageWidth = getImageWidth();
        int imageHeight = getImageHeight();
        boolean flag = imageHeight != 0 && imageWidth != 0;
        if (flag) {
            if(is24BitCount()) {
                red = new int[imageHeight][imageWidth];
                green = new int[imageHeight][imageWidth];
                blue = new int[imageHeight][imageWidth];
            }
            else if(is8BitCount()){
                color = new int[imageHeight][imageWidth];
            }
        }

        return flag;
    }


    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    int getBfOffBits() {
        return bytesToInt(Arrays.copyOfRange(fileHeader, 10, 14));
    }

    /**
     * 设置偏移量
     *
     * @param offset 偏移量
     */
    void setBfOffBits(int offset) {
        if(is24BitCount()){
            if (offset <= OFF_BITS_24) {
                return;
            }
        }else if(is8BitCount()){
            if (offset <= OFF_BITS_8) {
                return;
            }
        }

        byte[] bs = intToBytes(offset);
        int index = 10, bi = 0;
        while (index < fileHeader.length) {
            fileHeader[index] = bs[bi++];
            index++;
        }
    }


    int getImageWidth() {
        if (width == -1) {
            width = bytesToInt(Arrays.copyOfRange(informationHeader, 4, 8));
        }

        return width;
    }

    int getImageHeight() {
        if (height == -1) {
            height = bytesToInt(Arrays.copyOfRange(informationHeader, 8, 12));
        }

        return height;
    }

    private int bytesToInt(byte[] bytes) {
        if (null == bytes || bytes.length <= 0) {
            return 0;
        }

        return (bytes[3] & 0xff << 24) | (bytes[2] & 0xff) << 16 | (bytes[1] & 0xff) << 8 | bytes[0] & 0xff;
    }

    private byte[] intToBytes(int integer) {
        byte[] bytes = new byte[4];

        bytes[3] = (byte) (integer >> 24);
        bytes[2] = (byte) (integer >> 16);
        bytes[1] = (byte) (integer >> 8);
        bytes[0] = (byte) (integer);
        return bytes;
    }
}