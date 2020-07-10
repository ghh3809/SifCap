package com.kotoumi.sifcap;

import lombok.extern.slf4j.Slf4j;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacketHandler;
import com.kotoumi.sifcap.utils.PacketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序的入口，执行抓包命令
 * @author guohaohao
 */
@Slf4j
public class Capture {

    public static void main(String[] args) {

        List<PcapIf> alldevs = new ArrayList<>();
        StringBuilder errbuf = new StringBuilder();

        /***************************************************************************
         * First get a list of devices on this system
         **************************************************************************/
        Pcap.findAllDevs(alldevs, errbuf);
        if (alldevs.isEmpty()) {
            System.err.printf("Can’t read list of devices, error is %s", errbuf.toString());
            return;
        }

        System.out.println("Network devices found:");

        int i = 0;
        for (PcapIf device : alldevs) {
            String description = (device.getDescription() != null) ? device.getDescription()
                    : "No description available";
            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
        }

        PcapIf device = null;
        for (PcapIf pcapIf : alldevs) {
            if ("eth0".equals(pcapIf.getName())) {
                log.info("Found eth0 in all devices, address: {}", pcapIf.getAddresses().get(0).getAddr());
                device = pcapIf;
                break;
            }
        }

        if (device == null) {
            log.error("Could not found device eth0, exit!");
            return;
        }

        /***************************************************************************
         * Second we open up the selected device
         **************************************************************************/
        // 打开设备

        // openlive方法：这个方法打开一个和指定网络设备有关的，活跃的捕获器

        // 参数：snaplen指定的是可以捕获的最大的byte数，
        // 如果 snaplen的值比我们捕获的包的大小要小的话，
        // 那么只有snaplen大小的数据会被捕获并以packet data的形式提供。
        // IP协议用16位来表示IP的数据包长度，所有最大长度是65535的长度
        // 这个长度对于大多数的网络是足够捕获全部的数据包的

        // 参数：flags promisc指定了接口是promisc模式的，也就是混杂模式，
        // 混杂模式是网卡几种工作模式之一，比较于直接模式：
        // 直接模式只接收mac地址是自己的帧，
        // 但是混杂模式是让网卡接收所有的，流过网卡的帧，达到了网络信息监视捕捉的目的

        // 参数：timeout 这个参数使得捕获报后等待一定的时间，来捕获更多的数据包，
        // 然后一次操作读多个包，不过不是所有的平台都支持，不支持的会自动忽略这个参数

        // 参数：errbuf pcap_open_live()失败返回NULL的错误信息，或者成功时候的警告信息

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

        if (pcap == null) {
            System.err.print("Error while opening device for capture: " + errbuf.toString());
            return;
        }

        /*
         * Compiling and appplying a filter to network interface
         */
        PcapBpfProgram filter = new PcapBpfProgram();
        String expression = "tcp port 80";
        int optimize = 0;
        int netmask = 0;
        int m = pcap.compile(filter, expression, optimize, netmask);
        if (m != Pcap.OK) {
            System.out.println("Filter error: " + pcap.getErr());
        }
        pcap.setFilter(filter);

        /***************************************************************************
         * Third we create a packet handler which will receive packets from the
         * libpcap loop.
         **************************************************************************/

        //PacketHandler处理
        PcapPacketHandler<String> pcapPacketHandler = new PacketHandler<>();

        /***************************************************************************
         * Fourth we enter the loop and tell it to capture 10 packets. The loop
         * method does a mapping of pcap.datalink() DLT value to JProtocol ID,
         * which is needed by JScanner. The scanner scans the packet buffer and
         * decodes the headers. The mapping is done automatically, although a
         * variation on the loop method exists that allows the programmer to
         * sepecify exactly which protocol ID to use as the data link type for
         * this pcap interface.
         **************************************************************************/
        pcap.loop(-1, pcapPacketHandler, "jNetPcap rocks!");

        /***************************************************************************
         * Last thing to do is close the pcap handle
         **************************************************************************/
        pcap.close();
    }
}
