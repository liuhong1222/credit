package cn.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("threadExecutorService")
public class ThreadExecutorService {
	private Logger logger = LoggerFactory.getLogger(ThreadExecutorService.class);

	@Value("${threedPoolSize}")
	private String threedPoolSize = "100";

	private static ExecutorService fixedThreadPool = null;

	@PostConstruct
	public synchronized void initial() {
		logger.info("==========initial ThreadExecutorService============");
		fixedThreadPool = Executors.newFixedThreadPool(Integer.parseInt(threedPoolSize));
	}

	@PreDestroy
	public synchronized void shutdown() {
		logger.info("==========shutdown ThreadExecutorService============");
		if (fixedThreadPool != null) {
			fixedThreadPool.shutdown();
		}
	}

	public Future<?> execute(Runnable command) {
		try {
			logger.info("==========start execute ThreadExecutorService============");
			return fixedThreadPool.submit(command);
		} catch (Exception e) {
			logger.error("==========end Throwable ThreadExecutorService============");
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
