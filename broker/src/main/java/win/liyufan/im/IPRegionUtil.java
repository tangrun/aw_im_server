package win.liyufan.im;

import org.lionsoul.ip2region.xdb.Searcher;

/**
 * @author Rain
 * @date 2023/9/11 13:11
 */
public class IPRegionUtil {
    private static Searcher searcher;


    public static void init(String dbPath){
        // 1、从 dbPath 加载整个 xdb 到内存。
        byte[] cBuff;
        try {
            cBuff = Searcher.loadContentFromFile(dbPath);
        } catch (Exception e) {
            throw new RuntimeException("IPRegion加载xdb失败，文件位置："+dbPath,e);
        }

        // 2、使用上述的 cBuff 创建一个完全基于内存的查询对象。
        try {
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            System.out.printf("failed to create content cached searcher: %s\n", e);
            throw new RuntimeException("IPRegion初始化失败",e);
        }
    }

    /**
     * 国家|区域|省份|城市|ISP
     * {ip: 151.203.207.240, region: 美国|0|马萨诸塞|波士顿|威瑞森, ioCount: 0, took: 2 μs}
     * {ip: 41.165.213.82, region: 南非|0|0|0|0, ioCount: 0, took: 2 μs}
     * {ip: 73.116.148.212, region: 美国|0|加利福尼亚|萨克拉门托|康卡斯特, ioCount: 0, took: 2 μs}
     * {ip: 106.83.155.137, region: 中国|0|重庆|重庆市|电信, ioCount: 0, took: 2 μs}
     * {ip: 39.223.123.135, region: 印度尼西亚|0|大雅加达|雅加达|Telin, ioCount: 0, took: 3 μs}
     * @param ip
     * @return
     */
    public static String search(String ip){
        try {
            return searcher.search(ip);
        } catch (Exception e) {
        }
        return null;
    }
}
