package com.lmeng.yupao.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入 Excel
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ImportExcel {
    public static void main(String[] args) {
        // todo 文件路径记得改为自己的测试文件
        //将 Excel 文件的路径传入 readByListener 或 synchronousRead 方法进行数据读取。
        String fileName = "D:\\Coding workspace\\UserCenterProject\\yupao-backend-main\\src\\main\\resources\\testExcel.xlsx";
        readByListener(fileName);
        //synchronousRead(fileName);
    }

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        //使用监听器的方式读取数据。通过 EasyExcel.read() 方法传入 Excel 文件路径、数据类型、监听器实例来读取数据。
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读取
     *
     * @param fileName
     */
    public static void synchronousRead(String fileName) {
        //传入 Excel 文件路径，并通过 head() 方法指定数据类型，然后通过 doReadSync() 方法同步读取数据。
        //遍历输出每一行的数据信息。这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}
