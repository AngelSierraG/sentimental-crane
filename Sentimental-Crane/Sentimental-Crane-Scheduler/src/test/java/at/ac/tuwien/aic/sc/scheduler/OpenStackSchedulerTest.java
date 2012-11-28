package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class OpenStackSchedulerTest {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private Date dataBeginDate;
	private Date dataEndDate;
	private OpenStackScheduler scheduler;

	@Before
	public void before() throws Exception {
		scheduler = new OpenStackScheduler();
		scheduler.clusterManager = new ClusterManagerMock();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		dataEndDate = format.parse("2011-08-02");
		dataBeginDate = format.parse("2011-07-29");
	}


	@Test
	public void testNewAnalysis() {
		scheduler.newAnalysis(new AnalysisStartEvent("Google", DateUtils.addDays(dataBeginDate, -1), DateUtils.addDays(dataBeginDate, +1)));
		assertEquals(8, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testShutdownSchedule() {
		scheduler.clusterManager.startClusterNodes(8);
		scheduler.lastAnalysisDate = new Date();
		scheduler.idletime = -1;
		scheduler.run();
		assertEquals(7, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testMultipleShutdown(){
		scheduler.clusterManager.startClusterNodes(8);
		scheduler.lastAnalysisDate = new Date();
		scheduler.idletime = -1;
		scheduler.run();
		scheduler.run();
		scheduler.run();
		assertEquals(5, scheduler.clusterManager.getNumberOfRunningNodes());
	}
}
