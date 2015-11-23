package OnlineSlicing;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

public class Monitor {
	private static OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static double lastCPU;
    private static final AtomicLong Ecounter = new AtomicLong();
    private static final AtomicLong Tcounter = new AtomicLong();
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
    
    public static void EMemory(){
    	//lastMemory = Runtime.getRuntime().totalMemory();
    	Ecounter.incrementAndGet();
    }
    
    public static void TMemory(){
    	//long dMemory = Runtime.getRuntime().totalMemory() - lastMemory;
    	Tcounter.incrementAndGet();
    }
    
    public static void printMemory(){
    	System.out.println(Ecounter.get()+" Events + "+Tcounter.get()+" Tokens");
    }
}

