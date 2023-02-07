package cn.thread;

import java.util.List;

public class TasksRunnable  implements  Runnable {
	private List<Task> tasks;
	public TasksRunnable(List<Task> tasks) {
		super();
		this.tasks = tasks;
	}
	@Override
	public void run() {
		if(tasks.size()<1){
			return;
		}
		for (Task task : tasks) {
			task.startTask();
		}
	}
}
