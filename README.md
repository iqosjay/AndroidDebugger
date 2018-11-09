# AndroidDebugger
--每次都要插USB数据线？
--使用这个工具可以一键打开

---
1、 需要Root权限
2、 PC需要配置ADB命令
3、 手机电脑需要在同一个局域网
---

由于执行shell命令
---
**su//获取root权限
  setprop service.adb.tcp.port 5555//设置监听的端口，端口可以自定义，如5554，5555是默认的
  stop adbd//关闭adbd
  start adbd//重新启动adbd**
---
所以此工具当然是需要Root权限才可以使用~
执行成功之后会出现一个

--inet addr:***.***.***.***

后面就是你手机的IP地址
电脑端打开cmd终端，输入adb connect <你的ip地址>(如 adb connect 192.168.1.3)
出现
connected to 192.168.1.3:5555
表示连接成功，此时就可以调试了

如果没有Root、那就只有先用USB连接
输入
adb tcpip 5555 //打开端口
adb shell ifconfig wlan0 //查看手机IP地址
adb connect <你的IP>

有问题发送邮件至
iqosjay@gmail.com 
