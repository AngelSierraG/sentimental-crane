package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
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
	@Ignore
	public void testNewAnalysis() throws Exception {
		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", DateUtils.addDays(dataBeginDate, -1), DateUtils.addDays(dataBeginDate, +1));
		scheduler.newAnalysis(startEvent);
		Thread.sleep(17000);
		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		scheduler.run();		
	}
}
