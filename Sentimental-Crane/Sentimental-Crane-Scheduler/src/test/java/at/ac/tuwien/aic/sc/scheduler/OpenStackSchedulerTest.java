package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import at.ac.tuwien.aic.sc.scheduler.support.SimpleComputationLoadPredictor;
import org.apache.commons.lang3.reflect.FieldUtils;
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
	SimpleComputationLoadPredictor loadPredictor;

	@Before
	public void before() throws Exception {
		scheduler  = new OpenStackScheduler();
		scheduler.clusterManager = new ClusterManagerMock();

		loadPredictor = new SimpleComputationLoadPredictor();
		scheduler.loadPredictor = loadPredictor;

		dataBeginDate = (Date) FieldUtils.readField(loadPredictor, "dataBeginDate", true);
		dataEndDate = (Date) FieldUtils.readField(loadPredictor, "dataEndDate", true);
	}

	@Test
	public void testOneDay() {
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(0, scheduler.clusterManager.getNumberOfRunningNodes());

		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", new Date(0), new Date(0));
		scheduler.newAnalysis(startEvent);
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testHundredDays() {
		Date start = new Date(0);
		Date end = DateUtils.addDays(start, 50);

		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());

		end = DateUtils.addDays(start, 100);

		startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(2, loadPredictor.calculateRequiredNodeCount());
		assertEquals(2, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}

	@Test
	public void testAnalyseSpecialBegin() {
		Date start = DateUtils.addDays(dataBeginDate, -1);
		Date end = DateUtils.addDays(dataBeginDate, +1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialEnd() {
		Date start = DateUtils.addDays(dataEndDate, -1);
		Date end = DateUtils.addDays(dataEndDate, +1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialBetween() {
		Date start = DateUtils.addDays(dataBeginDate, +1);
		Date end = DateUtils.addDays(dataEndDate, -1);

		analyseSpecial(start, end);
	}

	@Test
	public void testAnalyseSpecialAll() {
		Date start = DateUtils.addDays(dataBeginDate, -1);
		Date end = DateUtils.addDays(dataEndDate, +1);

		analyseSpecial(start, end);
	}

	private void analyseSpecial(Date start, Date end) {
		AnalysisStartEvent startEvent = new AnalysisStartEvent("Google", start, end);
		scheduler.newAnalysis(startEvent);
		assertEquals(loadPredictor.maxNumberOfClusterNodes, loadPredictor.calculateRequiredNodeCount());
		assertEquals(loadPredictor.maxNumberOfClusterNodes, scheduler.clusterManager.getNumberOfRunningNodes());

		scheduler.analysisEnded(new AnalysisEndEvent(startEvent.getEventId()));
		assertEquals(1, loadPredictor.calculateRequiredNodeCount());
		assertEquals(1, scheduler.clusterManager.getNumberOfRunningNodes());
	}
}
