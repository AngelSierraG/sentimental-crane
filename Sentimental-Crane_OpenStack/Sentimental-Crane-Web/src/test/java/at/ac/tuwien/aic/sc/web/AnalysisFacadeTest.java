package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.entities.Company;
import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AnalysisFacadeTest {
	private AnalysisFacade facade;

	private AtomicInteger numberOfRequest = new AtomicInteger(0);

	private List<Pair<Date, Date>> scheduled;

	@Before
	public void init() {
		numberOfRequest.set(0);
		scheduled = new ArrayList<Pair<Date, Date>>();
		facade = new AnalysisFacade();
		facade.startBus = new MyEventProcessor<AnalysisStartEvent>();
		facade.endBus = new MyEventProcessor<AnalysisEndEvent>();
		facade.analysisScheduler = new AnalysisScheduler() {
			@Override
			public Future<AnalysisResult> schedule(Company company, final Date from, final Date to) {
				numberOfRequest.incrementAndGet();
				scheduled.add(new Pair<Date, Date>() {
					@Override
					public Date getLeft() {
						return from;
					}

					@Override
					public Date getRight() {
						return to;
					}

					@Override
					public Date setValue(Date value) {
						return null;
					}
				});
				return new Future<AnalysisResult>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						return false;
					}

					@Override
					public boolean isCancelled() {
						return false;
					}

					@Override
					public boolean isDone() {
						return true;
					}

					@Override
					public AnalysisResult get() throws InterruptedException, ExecutionException {
						return new AnalysisResult(0, 0);
					}

					@Override
					public AnalysisResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
						return new AnalysisResult(0, 0);
					}
				};
			}
		};
	}

	@Test
	public void hourTest() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dataEndDate = format.parse("2011-08-02");
		Date dataBeginDate = format.parse("2011-07-29");
		facade.analyse(new Company("Google"), dataBeginDate, dataEndDate);

		assertEquals(8, numberOfRequest.get());
	}

	@Test
	public void testOneDay() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dataEndDate = format.parse("2011-08-02");
		Date dataBeginDate = format.parse("2011-08-02");
		facade.analyse(new Company("Google"), dataBeginDate, dataEndDate);

		assertEquals(8, numberOfRequest.get());
		for (Pair<Date, Date> p : scheduled) {
			System.out.println(p.getLeft() + " - " + p.getRight());
		}
	}

	@Test
	public void testMonthRange() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dataBeginDate = format.parse("2011-08-01");
		Date dataEndDate = format.parse("2011-08-30");
		facade.analyse(new Company("Google"), dataBeginDate, dataEndDate);

		assertEquals(8, numberOfRequest.get());
		for (Pair<Date, Date> p : scheduled) {
			System.out.println(p.getLeft() + " - " + p.getRight());
		}
	}

	static class MyEventProcessor<T> implements Event<T> {
		@Override
		public void fire(T t) {

		}

		@Override
		public Event<T> select(Annotation... annotations) {
			return null;
		}

		@Override
		public <U extends T> Event<U> select(Class<U> uClass, Annotation... annotations) {
			return null;
		}

		@Override
		public <U extends T> Event<U> select(TypeLiteral<U> uTypeLiteral, Annotation... annotations) {
			return null;
		}
	}
}
