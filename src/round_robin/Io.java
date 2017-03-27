package round_robin;

import java.util.LinkedList;

/**
 * This class implements functionality associated with
 * the I/O device of the simulated system.
 */
public class Io {
    private Process activeProcess = null;
    private LinkedList<Process> ioQueue;
    private long avgIoTime;
    private Statistics statistics;
    private boolean idle;

    /**
     * Creates a new I/O device with the given parameters.
     * @param ioQueue		The I/O queue to be used.
     * @param avgIoTime		The average duration of an I/O operation.
     * @param statistics	A reference to the statistics collector.
     */
    public Io(LinkedList<Process> ioQueue, long avgIoTime, Statistics statistics) {
        this.ioQueue = ioQueue;
        this.avgIoTime = avgIoTime;
        this.statistics = statistics;
        this.idle = true;
    }

    /**
     * Adds a process to the I/O queue, and initiates an I/O operation
     * if the device is free.
     * @param requestingProcess	The process to be added to the I/O queue.
     * @param clock				The time of the request.
     * @return					The event ending the I/O operation, or null
     *							if no operation was initiated.
     */
    public Event addIoRequest(Process requestingProcess, long clock) {

        ioQueue.add(requestingProcess);
        requestingProcess.ioQueued();

        if (idle) {
            activeProcess = ioQueue.pop();
            idle = false;
            activeProcess.leftIoQueue(clock);
            return requestingProcess.ioActive(avgIoTime, clock);
        }

        return null;
    }

    /**
     * Starts a new I/O operation if the I/O device is free and there are
     * processes waiting to perform I/O.
     * @param clock		The global time.
     * @return			An event describing the end of the I/O operation that was started,
     *					or null	if no operation was initiated.
     */
    public Event startIoOperation(long clock) {
        if ((activeProcess == null) && (!ioQueue.isEmpty()))  {
            activeProcess = ioQueue.pop();
            activeProcess.leftIoQueue(clock);
            return activeProcess.ioActive(avgIoTime, clock);
        }
        idle = true;
        return null;
    }

    /**
     * This method is called when a discrete amount of time has passed.
     * @param timePassed	The amount of time that has passed since the last call to this method.
     */
    public void timePassed(long timePassed) {
        statistics.ioQueueLengthTime += ioQueue.size()*timePassed;
        if (ioQueue.size() > statistics.ioQueueLargestLength) {
            statistics.ioQueueLargestLength = ioQueue.size();
        }
    }

    /**
     * Removes the process currently doing I/O from the I/O device.
     * @return	The process that was doing I/O, or null if no process was doing I/O.
     */
    public Process removeActiveProcess() {
        if (activeProcess != null) {
            Process p = activeProcess;
            activeProcess = null;
            statistics.nofProcessedIoOperations++;
            return p;
        }
        return null;
    }

    public Process getActiveProcess() {
        return activeProcess;
    }
}
