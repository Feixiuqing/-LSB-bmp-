package informationHidden;


import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class UserInterface extends JFrame {

    public static UserInterface l;
    private static final int CENTER = 0;

    private int displayWidth, displayHeight;

    private UserInterface() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        displayWidth = (int) (screenSize.getWidth() * 0.6);
        displayHeight = (int) (screenSize.getHeight() * 0.6);
        initFrame();
    }

    private void initFrame() {
        JLabel yt = new JLabel("原  图",CENTER);
        yt.setFont(new Font("仿宋",1,72));
        yt.setPreferredSize(new Dimension(displayWidth/2,512));
        yt.setBorder(BorderFactory.createLineBorder(Color.black));


        JLabel qr = new JLabel("嵌 入 后 ",CENTER);
        qr.setFont(new Font("仿宋",1,72));
        qr.setPreferredSize(new Dimension(displayWidth/2,512));
        qr.setBorder(BorderFactory.createLineBorder(Color.black));

        Handle.setUIFont(new FontUIResource("黑体", 0, 20));

        setLayout(new BorderLayout());
        add(yt,BorderLayout.WEST);
        add(qr,BorderLayout.EAST);

        setJMenuBar(initMenuBar());

        setTitle("LSB Hide Information");
        setSize(displayWidth, displayHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    private final String IMAGE = "图片操作";
    private final String READ = "读取";
    private final String SAVE = "保存";
    private final String INFO = "信息操作";
    private final String HIDE = "嵌入信息";
    private final String SHOW = "提取信息";
    private final String NOISE = "添加噪声";
    private final String BMP = "bmp";

    private JMenuBar initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        MenuItemListener listener = new MenuItemListener();
        JMenu menu1 = new JMenu(IMAGE);

        JMenuItem openItem = new JMenuItem(READ);
        openItem.addActionListener(listener);
        JMenuItem saveItem = new JMenuItem(SAVE);
        saveItem.addActionListener(listener);
        menu1.add(openItem);
        menu1.add(saveItem);
        menuBar.add(menu1);

        JMenu menu2 = new JMenu(INFO);
        JMenuItem hideItem = new JMenuItem(HIDE);
        hideItem.addActionListener(listener);
        JMenuItem showItem = new JMenuItem(SHOW);
        showItem.addActionListener(listener);
        JMenuItem noise = new JMenuItem(NOISE);
        noise.addActionListener(listener);
        menu2.add(hideItem);
        menu2.add(showItem);
        menu2.add(noise);
        menuBar.add(menu2);
        return menuBar;
    }

    class MenuItemListener implements ActionListener {

        private final String PATH = "C:/Users/费/Desktop/近期将要完成的任务/信息安全实验 截止12.16/可供选用的图片";
        private JFileChooser fileChooser;
        private ImageInfo imageInfo;

        private static final byte OPEN = 1;
        private static final byte STORE = 2;
        private static final byte ADDNOISE = 3;

        MenuItemListener() {
            fileChooser = new JFileChooser(PATH);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP位图(*.bmp,*.BMP)", BMP));


            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case READ: {
                    openHandler();
                    break;
                }
                case SAVE: {
                    saveHandler();
                    break;
                }
                case HIDE: {
                    hideHandler();
                    break;
                }
                case SHOW: {
                    showHandler();
                    break;
                }
                case NOISE:{
                        noiseHandler();
                    break;
                }
                default: {
                    break;
                }
            }
        }

        private void showHandler() {
            if (null == imageInfo) {
                JOptionPane.showMessageDialog(null, "请选择图片后重试", "警告", JOptionPane.ERROR_MESSAGE);

                return;
            }

            String info = Handle.analysisHideInfo(imageInfo);
            if ("".equals(info.trim())) {
                JOptionPane.showMessageDialog(null, "解析出来的信息为空", "提示", JOptionPane.ERROR_MESSAGE);

                return;
            }

            JOptionPane.showMessageDialog(null, info,"提取信息为",JOptionPane.NO_OPTION);
        }

        private void hideHandler() {
            if ((imageInfo == null )) {

                JOptionPane.showMessageDialog(null, "请打开BMP图片后再试！");
                return;
            }
            if(imageInfo.is24BitCount() && imageInfo.blue == null){

                JOptionPane.showMessageDialog(null, "请打开BMP图片后再试！");
                return;
            }
            if(imageInfo.is8BitCount() && imageInfo.color == null){

                JOptionPane.showMessageDialog(null, "请打开BMP图片后再试！");
                return;
            }
            String str1 = String.valueOf(imageInfo.getImageHeight()*imageInfo.getImageWidth()/8);
            String str2 = String.valueOf(3*imageInfo.getImageHeight()*imageInfo.getImageWidth()/8);
            if(imageInfo.is24BitCount())
                JOptionPane.showMessageDialog(null, str2+"字节","可嵌入最大信息长度为",JOptionPane.PLAIN_MESSAGE);
            else if(imageInfo.is8BitCount())
                JOptionPane.showMessageDialog(null, str1+"字节","可嵌入最大信息长度为",JOptionPane.PLAIN_MESSAGE);
            String text = JOptionPane.showInputDialog("请输入要嵌入的信息：");
            if (null == text || text.trim().length() <= 0) {
                JOptionPane.showMessageDialog(null, "请在文本框中输入需要隐藏的文本信息后重试");
                return;
            }

            if (Handle.hideInfoToBmp(imageInfo, text)) {
                JOptionPane.showMessageDialog(null, "隐藏文本信息成本！","提示",JOptionPane.PLAIN_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(null, "隐藏信息失败","错误",JOptionPane.ERROR_MESSAGE);


        }

        private void openHandler() {

            File file = selectFile(OPEN);
            if (null == file) {
                return;
            }

            l.dispose();
            new UserInterface();

            imageInfo = Handle.analysisBmpImage(file);
            if(!imageInfo.isBmpType()){
                return;
            }
            else {
                if(imageInfo.is24BitCount()){
                    JOptionPane.showMessageDialog(null,"图片为24位真彩图");
                    if (imageInfo.getBfOffBits() > ImageInfo.OFF_BITS_24) {

                        showQBmpImage(imageInfo);
                        JOptionPane.showMessageDialog(null,"打开图片成功！\n(图片为嵌入信息后图片)");

                    }else{

                        showYBmpImage(imageInfo);
                        JOptionPane.showMessageDialog(null,"打开图片成功！");
                    }
                }
                else if(imageInfo.is8BitCount()){
                    JOptionPane.showMessageDialog(null,"图片为256灰度图");
                    if (imageInfo.getBfOffBits() > ImageInfo.OFF_BITS_8) {

                        showQBmpImage(imageInfo);
                        JOptionPane.showMessageDialog(null,"打开图片成功！\n(图片为嵌入信息后图片)");

                    }else{

                        showYBmpImage(imageInfo);
                        JOptionPane.showMessageDialog(null,"打开图片成功！");
                    }
                }
            }


        }

        private void noiseHandler(){
            File file = selectFile(OPEN);
            if (null == file) {
                return;
            }

            l.dispose();
            new UserInterface();
            file = selectFile(ADDNOISE);
            imageInfo = Handle.analysisBmpImage(file);
            Handle.addNoise(imageInfo, file);
            JOptionPane.showMessageDialog(null,"添加噪声成功");


        }

        private void saveHandler() {
            File file = new File(generateSaveFileName());
            fileChooser.setSelectedFile(file);
            file = selectFile(STORE);
            Handle.saveBmpImage(imageInfo, file);
            JOptionPane.showMessageDialog(null,"保存文件成功");
        }

        private String generateSaveFileName() {
            File selectedFile = fileChooser.getSelectedFile();
            String name = selectedFile.getName();
            name = name.substring(name.lastIndexOf('_') + 1);
            String parent = selectedFile.getParent();

            return String.join("", parent, "/", name);

        }

        private File selectFile(byte type) {

            if (type == OPEN && JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(null)) {
                return null;
            }

            if (type == STORE && JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(null)) {
                return null;
            }

            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();

            String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);

            if (!BMP.equalsIgnoreCase(suffixName)) {

                JOptionPane.showMessageDialog(null,"图片不是bmp格式");
                return null;
            }

            return selectedFile;
        }
    }



    private void showYBmpImage(ImageInfo imageInfo) {
        DrawPanel hiding = new DrawPanel(imageInfo);

        JScrollPane scrollPane = new JScrollPane();

        scrollPane.setViewportView(hiding);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane,BorderLayout.WEST);

        setVisible(true);
    }

    /**
     * 显示嵌入后信息的图片
     * @param imageInfo
     */
    private void showQBmpImage(ImageInfo imageInfo) {
        DrawPanel hided = new DrawPanel(imageInfo);

        JScrollPane scrollPane = new JScrollPane();

        scrollPane.setViewportView(hided);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane,BorderLayout.EAST);

        setVisible(true);
    }



    /**
     * 绘图面板
     */
    class DrawPanel extends JPanel {

        ImageInfo bmpImageInfo;

        DrawPanel(ImageInfo imageInfo) {
            bmpImageInfo = imageInfo;
            setPreferredSize(new Dimension(displayWidth/2, imageInfo.getImageHeight()));

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (null == bmpImageInfo) {
                return;
            }

            int height = bmpImageInfo.getImageHeight();
            int width = bmpImageInfo.getImageWidth();
            // 水平居中x 初始坐标
            int initX = 0;

            if (displayWidth > width) {
                initX = (int) (displayWidth/2-width)/2;
            }

           if(bmpImageInfo.is24BitCount()) {

               for (int h = 0; h < height; h++) {
                   for (int w = 0; w < width; w++) {
                       g.setColor(new Color(bmpImageInfo.red[h][w], bmpImageInfo.green[h][w], bmpImageInfo.blue[h][w]));
                       g.fillRect(w + initX, h, 1, 1);
                   }
               }
           }
           else if(bmpImageInfo.is8BitCount()){
               for (int h = 0; h < height; h++) {
                   for (int w = 0; w < width; w++) {
                       g.setColor(new Color(bmpImageInfo.color[h][w],bmpImageInfo.color[h][w],bmpImageInfo.color[h][w]));
                       g.fillRect(w + initX, h, 1, 1);
                   }
               }
           }
        }
    }

    public static void main(String[] args) {

        l =   new UserInterface();
    }

}




