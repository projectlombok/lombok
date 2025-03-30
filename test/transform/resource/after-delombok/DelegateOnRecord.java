// version 14:
record DelegateOnRecord(Runnable runnable) {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void run() {
		this.runnable.run();
	}
}
