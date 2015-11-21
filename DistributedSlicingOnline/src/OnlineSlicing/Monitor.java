package OnlineSlicing;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

public class Monitor {
	private static OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static double lastCPU;
    private static long lastMemory;
	private static final AtomicLong counter = new AtomicLong();
	
	public static void incrementCount() {
		counter.incrementAndGet();
	}
	
	public static void printMsgCount(){
		System.out.println(counter.get());
	}
	
    public static void initCpuUsage(){
    	lastCPU = osbean.getSystemLoadAverage();
    	System.out.println(lastCPU);
    }
    
    public static double getCpuUsage(){
    	double dCPU = osbean.getSystemLoadAverage() - lastCPU;
    	System.out.println(dCPU);
    	lastCPU = osbean.getSystemLoadAverage();
    	return dCPU;
    }    
    
    public static void initMemory(){
    	lastMemory = Runtime.getRuntime().totalMemory();
    	System.out.println(lastMemory);
    }
    
    public static long getMemory(){
    	long dMemory = Runtime.getRuntime().totalMemory() - lastMemory;
    	System.out.println(dMemory);
    	lastMemory = Runtime.getRuntime().totalMemory();
    	return dMemory;
    }
}

