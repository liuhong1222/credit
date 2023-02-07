package cn.thread;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务队列
 * 
 * @author rain
 *
 */
public class SynchronizationTasks {
	private ThreadExecutorService threadExecutorService;

	public SynchronizationTasks(ThreadExecutorService threadExecutorService) {
		super();
		this.threadExecutorService = threadExecutorService;
	}

	/** 任务队列长度 */
	private List<Task> queues = new ArrayList<Task>();

	public void addTask(Task task) {
		queues.add(task);
	}

	public void clearTasks() {
		queues.clear();
	}

	/** 开始执行任务 */
	public void startTask() {
		if (queues.size() < 1) {
			return;
		}
		threadExecutorService.execute(new TasksRunnable(queues));
	}
}
