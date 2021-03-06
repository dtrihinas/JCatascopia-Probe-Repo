/*******************************************************************************
 * Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
 * 
 * For any information relevant to JCatascopia Monitoring System,
 * please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
import java.io.IOException;
import java.util.HashMap;

import eu.celarcloud.jcatascopia.probes.Probe;
import eu.celarcloud.jcatascopia.probes.ProbeMetric;
import eu.celarcloud.jcatascopia.probes.ProbePropertyType;

/**
 * 
 * @author Chris Smowton
 *
 */
public class WindowsCPUProbe extends Probe{
	
	//private static String cpuTotalCommand = "(Get-Counter -Counter @(\\\"\\Processor(_Total)\\% Processor Time\\\", \\\"\\Processor(_Total)\\% User Time\\\", \\\"\\Processor(_Total)\\% Privileged Time\\\", \\\"\\Processor(_Total)\\% Idle Time\\\")).CounterSamples | Select-Object -ExpandProperty CookedValue | ForEach-Object -process {$_.ToString((New-Object Globalization.CultureInfo \\\"\\\"))}";

	private static String cpuTotalCommand = PowershellHelper.makePowershellStatsCommand(
	  new String[]{
			  "\\Processor(_Total)\\% Processor Time",
			  "\\Processor(_Total)\\% User Time",
			  "\\Processor(_Total)\\% Privileged Time",
			  "\\Processor(_Total)\\% Idle Time"
	  });

	private static ProbePropertyType[] probeTypes = new ProbePropertyType[] {
		ProbePropertyType.DOUBLE,
		ProbePropertyType.DOUBLE,
		ProbePropertyType.DOUBLE,
		ProbePropertyType.DOUBLE
	};
	
	public WindowsCPUProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"cpuTotal",ProbePropertyType.DOUBLE,"%","Total system CPU usage");
		this.addProbeProperty(1,"cpuUser",ProbePropertyType.DOUBLE,"%","system USER usage");
		this.addProbeProperty(2,"cpuSystem",ProbePropertyType.DOUBLE,"%","system SYSTEM usage");
		this.addProbeProperty(3,"cpuIdle",ProbePropertyType.DOUBLE,"%","system IDLE Usage");
	}
	
	public WindowsCPUProbe(){
		this("Windows CPUProbe",10);
	}
		
	@Override
	public String getDescription() {
		return "Windows CPUProbe collects CPU usage stats";
	}

	@Override
	public ProbeMetric collect() {
		try {
			return collectOrThrow();
		}
		catch(Exception e) {
			System.err.println("Windows CPU probe failed: " + e.toString());
			return new ProbeMetric(new HashMap<Integer, Object>());
		}
	}

	public ProbeMetric collectOrThrow() throws IOException {
		HashMap<Integer, Object> values = PowershellHelper.powershellToStats(cpuTotalCommand, probeTypes);
		return new ProbeMetric(values);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WindowsCPUProbe cpuprobe = new WindowsCPUProbe();
		cpuprobe.activate();
	}
}