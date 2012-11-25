package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class OpenStackSchedulerTest {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private OpenStackScheduler scheduler;

	@Before
	public void before() throws ParseException {
		scheduler  = new OpenStackScheduler();
		scheduler.init();
		scheduler.clusterManager = new ClusterManagerMock();
	}

	@Test
	public void testOneDay() {
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(0, scheduler.clusterManager.getNumberOfRunningNodes());

		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", new Date(0), new Date(0));
		scheduler.newAnalysis(startEvent);
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testThousandDays() {
		Date start = new Date(0);
		Date end = DateUtils.addDays(start, 1000);

		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		end = DateUtils.addDays(start, 2000);

		startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(2, scheduler.calculateRequiredNodeCount());
		assertEquals(2, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testAnalyseSpecialBegin() {
		Date start = DateUtils.addDays(scheduler.dataBeginDate, -1);
		Date end = DateUtils.addDays(scheduler.dataBeginDate, +1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialEnd() {
		Date start = DateUtils.addDays(scheduler.dataEndDate, -1);
		Date end = DateUtils.addDays(scheduler.dataEndDate, +1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialBetween() {
		Date start = DateUtils.addDays(scheduler.dataBeginDate, +1);
		Date end = DateUtils.addDays(scheduler.dataEndDate, -1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialAll() {
		Date start = DateUtils.addDays(scheduler.dataBeginDate, -1);
		Date end = DateUtils.addDays(scheduler.dataEndDate, +1);

		analyseSpecial(start, end);
	}

	private void analyseSpecial(Date start, Date end) {
		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(scheduler.maxNumberOfClusterNodes, scheduler.calculateRequiredNodeCount());
		assertEquals(scheduler.maxNumberOfClusterNodes, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, scheduler.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}
}
