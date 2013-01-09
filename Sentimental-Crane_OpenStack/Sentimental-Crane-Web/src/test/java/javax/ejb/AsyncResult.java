package javax.ejb;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AsyncResult<T> implements Future<T> {

	public AsyncResult(T obj) {
	}

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
		return false;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return null;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return null;
	}
}
