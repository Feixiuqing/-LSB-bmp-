package informationHidden;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Handle {

    static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private static final String END = "0000000011111111";
    private static final char VALUE_0 = '0';

    static boolean hideInfoToBmp(ImageInfo imageInfo, String text) {
        imageInfo.setBfOffBits(imageInfo.getBfOffBits() + 1);
        String binary = toBinary(text).concat(END);
        char[] chars = binary.toCharArray();
        int  cnt = 0, len = chars.length;
        for (int i = 0; i < imageInfo.getImageHeight(); i++) {
            for (int j = 0; j < imageInfo.getImageWidth(); j++) {
                if(imageInfo.is24BitCount()) {
                    imageInfo.blue[i][j] = getModifyBitData(imageInfo.blue[i][j], chars[cnt++]);
                    if (cnt >= len) {
                        return true;
                    }

                    imageInfo.green[i][j] = getModifyBitData(imageInfo.green[i][j], chars[cnt++]);
                    if (cnt >= len) {
                        return true;
                    }

                    imageInfo.red[i][j] = getModifyBitData(imageInfo.red[i][j], chars[cnt++]);
                    if (cnt >= len) {
                        return true;
                    }
                }
                else if(imageInfo.is8BitCount()){
                        imageInfo.color[i][j] = getModifyBitData(imageInfo.color[i][j], chars[cnt++]);
                        if (cnt >= len) {
                            return true;
                        }
                    }
                }
            }


        if(imageInfo.is24BitCount())
            imageInfo.setBfOffBits(ImageInfo.OFF_BITS_24);
        else if (imageInfo.is8BitCount())
            imageInfo.setBfOffBits(ImageInfo.OFF_BITS_8);
        return false;
    }

    private static int getModifyBitData(int oldValue, char c) {
        // 0 ASCLL 48
        if (c == VALUE_0) {
            return oldValue & 0XE;
        }

        return oldValue | 0X1;
    }

    static String analysisHideInfo(ImageInfo imageInfo) {
        StringBuilder binStrBuilder;
        StringBuilder wordBin = new StringBuilder(16);
        if(imageInfo.is24BitCount()) {
            if (imageInfo.getBfOffBits() == ImageInfo.OFF_BITS_24)
                return "";
            binStrBuilder = new StringBuilder(imageInfo.blue[0].length << 3);
            int cnt = 0;
            loop:
            for (int i = 0; i < imageInfo.getImageHeight(); i++) {
                for (int j = 0; j < imageInfo.getImageWidth(); j++) {
                    wordBin.append(imageInfo.blue[i][j] & 0X1);

                        if (++cnt >= 16) {
                            if (isEndWithAppend(binStrBuilder, wordBin)) {
                                break loop;
                            }

                            cnt = 0;
                        }

                        wordBin.append(imageInfo.green[i][j] & 0X1);
                        if (++cnt >= 16) {
                            if (isEndWithAppend(binStrBuilder, wordBin)) {
                                break loop;
                            }

                            cnt = 0;
                        }

                        wordBin.append(imageInfo.red[i][j] & 0X1);
                        if (++cnt >= 16) {
                            if (isEndWithAppend(binStrBuilder, wordBin)) {
                                break loop;
                            }

                            cnt = 0;
                        }
                    }
                }
            return toString(binStrBuilder.toString());
            }

        else if(imageInfo.is8BitCount()){
            if(imageInfo.getBfOffBits()==ImageInfo.OFF_BITS_8)
                return "";
            binStrBuilder = new StringBuilder(imageInfo.color[0].length << 3);
            int cnt = 0;
            loop:
            for (int i = 0; i < imageInfo.getImageHeight(); i++) {
                for (int j = 0; j < imageInfo.getImageWidth(); j++) {
                    wordBin.append(imageInfo.color[i][j] & 0X1);


                    if (++cnt >= 16) {
                        if (isEndWithAppend(binStrBuilder, wordBin)) {
                            break loop;
                        }

                        cnt = 0;
                    }

                }
            }
            return toString(binStrBuilder.toString());
        }

        return null;
    }

    /**
     * 添加内容,并判断是否结束
     *
     * @param binStrBuilder 二进制串
     * @param wordBin       一个字的二进制串
     * @return true:结束;false:否
     */
    private static boolean isEndWithAppend(StringBuilder binStrBuilder, StringBuilder wordBin) {
        if (END.equals(wordBin.toString())) {
            return true;
        }

        binStrBuilder.append(wordBin);
        wordBin.delete(0, 16);
        return false;
    }

    static void addNoise(ImageInfo imageInfo,File file){
        if (null == imageInfo || null == file) {
            JOptionPane.showMessageDialog(null,"请重新选择文件");
            return;
        }
        if(imageInfo.is24BitCount()) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(imageInfo.fileHeader);
                bos.write(imageInfo.informationHeader);
                int bfOffBits = imageInfo.getBfOffBits();
                // 写入多余的偏移量

                if (bfOffBits > ImageInfo.OFF_BITS_24) {
                    bos.write(new byte[bfOffBits - ImageInfo.OFF_BITS_24]);
                }


                int height = imageInfo.getImageHeight();
                int width = imageInfo.getImageWidth();


                for (int h = height - 1; h >= 0; h--) {
                    for (int w = 0; w < width; w++) {

                        int blue = imageInfo.blue[h][w]*((int)(Math.random()*2));
                        int green = imageInfo.green[h][w]*((int)(Math.random()*2));
                        int red = imageInfo.red[h][w]*((int)(Math.random()*2));
                        bos.write(blue);
                        bos.write(green);
                        bos.write(red);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(imageInfo.is8BitCount()){
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(imageInfo.fileHeader);
                bos.write(imageInfo.informationHeader);
                int bfOffBits = imageInfo.getBfOffBits();
                // 写入多余的偏移量

                if (bfOffBits > ImageInfo.OFF_BITS_8) {
                    bos.write(new byte[bfOffBits - ImageInfo.OFF_BITS_8]);
                }


                int height = imageInfo.getImageHeight();
                int width = imageInfo.getImageWidth();


                for (int h = height - 1; h >= 0; h--) {
                    for (int w = 0; w < width; w++) {
                        int color = imageInfo.color[h][w]*((int)(Math.random()*2));
                        bos.write(color);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 解析bmp图片
     */
    static ImageInfo analysisBmpImage(File file) {
        ImageInfo imageInfo = new ImageInfo();


        if (null == file) {
            return imageInfo;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            bis.read(imageInfo.fileHeader);

            if (!imageInfo.isBmpType()) {
                JOptionPane.showMessageDialog(null,"非bmp类型文件");
                return imageInfo;
            }

            bis.read(imageInfo.informationHeader);

            if(imageInfo.is24BitCount()) {
                if (imageInfo.getBfOffBits() > ImageInfo.OFF_BITS_24) {
                    JOptionPane.showMessageDialog(null,"图中有隐藏信息","提示",JOptionPane.INFORMATION_MESSAGE);
                    bis.skip(imageInfo.getBfOffBits() - ImageInfo.OFF_BITS_24);
                }
            }
            else if(imageInfo.is8BitCount()){
                if(imageInfo.getBfOffBits()>ImageInfo.OFF_BITS_8){
                    JOptionPane.showMessageDialog(null,"图中有隐藏信息","提示",JOptionPane.INFORMATION_MESSAGE);
                    bis.skip(imageInfo.getBfOffBits() - ImageInfo.OFF_BITS_8);
                }
            }

            if (!imageInfo.initBitmapDataArr()) {
                System.out.println("初始化位图数据失败");
                return imageInfo;
            }

            int imageWidth = imageInfo.getImageWidth();

            int imageHeight = imageInfo.getImageHeight();

            for (int i = imageHeight - 1; i >= 0; i--) {
                for (int j = 0; j < imageWidth; j++) {
                    // 内存中是BGR顺序。
                    if(imageInfo.is24BitCount()) {
                        imageInfo.blue[i][j] = bis.read();
                        imageInfo.green[i][j] = bis.read();
                        imageInfo.red[i][j] = bis.read();
                    }
                    else if(imageInfo.is8BitCount()){
                        imageInfo.color[i][j] = bis.read();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageInfo;
    }


    /**
     *  保存文件
     */
    static void saveBmpImage(ImageInfo imageInfo, File file) {
        if (null == imageInfo || null == file) {
            JOptionPane.showMessageDialog(null,"请重新选择文件");
            return;
        }
        if(imageInfo.is24BitCount()) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(imageInfo.fileHeader);
                bos.write(imageInfo.informationHeader);
                int bfOffBits = imageInfo.getBfOffBits();
                // 写入多余的偏移量

                if (bfOffBits > ImageInfo.OFF_BITS_24) {
                    bos.write(new byte[bfOffBits - ImageInfo.OFF_BITS_24]);
                }


                int height = imageInfo.getImageHeight();
                int width = imageInfo.getImageWidth();

                for (int h = height - 1; h >= 0; h--) {
                    for (int w = 0; w < width; w++) {

                        int blue = imageInfo.blue[h][w];
                        int green = imageInfo.green[h][w];
                        int red = imageInfo.red[h][w];
                        bos.write(blue);
                        bos.write(green);
                        bos.write(red);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(imageInfo.is8BitCount()){
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(imageInfo.fileHeader);
                bos.write(imageInfo.informationHeader);
                int bfOffBits = imageInfo.getBfOffBits();
                // 写入多余的偏移量

                if (bfOffBits > ImageInfo.OFF_BITS_8) {
                    bos.write(new byte[bfOffBits - ImageInfo.OFF_BITS_8]);
                }


                int height = imageInfo.getImageHeight();
                int width = imageInfo.getImageWidth();

                for (int h = height - 1; h >= 0; h--) {
                    for (int w = 0; w < width; w++) {
                        int color = imageInfo.color[h][w];
                        bos.write(color);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private static String[] PREFIX_POOL = {"0", "00", "000", "0000", "00000", "000000", "0000000", "00000000",
            "000000000", "0000000000", "00000000000", "000000000000",
            "0000000000000", "00000000000000", "000000000000000"
    };

    /**
     * 字符串转换成二进制流
     *
     * @param str 字符串
     * @return 二进制流
     */
    private static String toBinary(String str) {
        if (null == str || str.trim().length() == 0) {
            return null;
        }

        char[] strChar = str.toCharArray();
        StringBuilder result = new StringBuilder(strChar.length << 4);
        for (int i = 0; i < strChar.length; i++) {
            // 二进制ASCII码
            String s = Integer.toBinaryString(strChar[i]);
            // 补0
            if (s.length() < 16) {
                s = PREFIX_POOL[15 - s.length()].concat(s);
            }

            result.append(s);
        }

        return result.toString();
    }

    /**
     * 二进制流转换成字符串
     *
     * @param binaryString 二进制流
     * @return 字符串
     */
    private static String toString(String binaryString) {
        if (null == binaryString || binaryString.trim().length() == 0) {
            return null;
        }

        int len = binaryString.length();
        StringBuilder sb = new StringBuilder(len >> 4);
        int step = 16;
        for (int i = 0; i < len; i += step) {
            // 将ascll码强转成字符
            sb.append((char) (int) Integer.parseInt(binaryString.substring(i, i + step), 2));
        }

        return sb.toString();
    }

}
