/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("nls")
public class EclipseJobManagerMXBeanImpl implements EclipseJobManagerMXBean {

    private static final String JOB_CLASS = "org.eclipse.core.runtime.jobs.Job";

    private static final String INTERNAL_JOB_CLASS = "org.eclipse.core.internal.jobs.InternalJob";

    private static final String JOB_MANAGER_CLASS = "org.eclipse.core.internal.jobs.JobManager";

    private static final String LOCK_MANAGER_CLASS = "org.eclipse.core.internal.jobs.LockManager";

    private static final String DEADLOCK_DETECTOR_CLASS = "org.eclipse.core.internal.jobs.DeadlockDetector";

    private static final String RUNTIME_LOG_CLASS = "org.eclipse.core.internal.runtime.RuntimeLog";

    private static final String ISTATUS_CLASS = "org.eclipse.core.runtime.IStatus";

    private static final String STATUS_CLASS = "org.eclipse.core.runtime.Status";

    private static final String IPROGRESS_MONITOR_CLASS = "org.eclipse.core.runtime.IProgressMonitor";

    /** The field name for DeadlockDetector.graph */
    private static final String GRAPH_FIELD = "graph";

    /** The field name for LockManager.locks and DeadlockDetector.locks*/
    private static final String LOCKS_FIELD = "locks";

    /** The field name for DeadlockDetector.lockThreads */
    private static final String LOCK_THREADS_FIELD = "lockThreads";

    /** The method name for Job.getJobManager() */
    private static final String GET_JOB_MANAGER_METHOD = "getJobManager";

    /** The method name for JobManager.getLockManager() */
    private static final String GET_LOCK_MANAGER_METHOD = "getLockManager";

    /** The method name for JobManager.find(Object) */
    private static final String FIND_METHOD = "find";

    /** The method name for Job.getName() " */
    private static final String GET_NAME_METHOD = "getName";

    /** The method name for Job.getThread() */
    private static final String GET_THREAD_METHOD = "getThread";

    /** The method name for Job.getState() */
    private static final String GET_STATE_METHOD = "getState";

    /** The method name for Job.getRule() */
    private static final String GET_RULE_METHOD = "getRule";

    /** The method name for InternalJob.getProgressMonitor() */
    private static final String GET_PROGRESS_MONITOR_METHOD = "getProgressMonitor";

    /** The field name for InternalJob.jobNumber */
    private static final String JOB_NUMBER_FIELD = "jobNumber";

    /** The method name for IProgressMonitor.isCanceled() */
    private static final String IS_CANCELED_METHOD = "isCanceled";

    /** The method name for RuntimeLog.log(IStatus status) */
    private static final String LOG_METHOD = "log";

    private final Instrumentation inst;

    private Object deadlockDetectorObject;

    private Class<?> jobClass;

    private Class<?> internalJobClass;

    private Class<?> jobManagerClass;

    private Class<?> lockManagerClass;

    private Class<?> deadlockDetectorClass;

    private Class<?> runtimeLogClass;

    private Class<?> iStatusClass;

    private Class<?> statusClass;

    private Class<?> iProgressMonitorClass;

    /**
     * The constructor.
     *
     * @param inst the instrumentation
     */
    public EclipseJobManagerMXBeanImpl(Instrumentation inst) {
        this.inst = inst;
        initialize();
    }

    @Override
    public EclipseSchedulingRuleCompositeData getSchedulingRule() {
        try {
            int[][] graph = getGraphField();
            List<String> locks = getLocksField();
            List<String> lockThreads = getLockThreadsField();

            if (graph.length != 0 && graph.length == lockThreads.size() && graph[0].length == locks.size()) {
                return new EclipseSchedulingRuleCompositeData(graph, locks, lockThreads);
            }

        } catch (Throwable t) {
            Agent.logError(t, Messages.CANNOT_GET_ECLIPSE_SCHEDULING_RULES);
        }
        return new EclipseSchedulingRuleCompositeData(new int[0][0], Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public EclipseJobCompositeData[] getJobs() {
        try {
            Object[] jobs = findJobs();

            EclipseJobCompositeData[] composites = new EclipseJobCompositeData[jobs.length];
            for (int i = 0; i < composites.length; i++) {
                String name = getJobName(jobs[i]);
                String className = jobs[i].getClass().getName();
                String state = getJobState(jobs[i]);
                boolean isCanceled = isCanceled(jobs[i]);
                String thread = getThreadNameFromJob(jobs[i]);
                String schedulingRule = getSchedulingRule(jobs[i]);

                composites[i] = new EclipseJobCompositeData(name, className, state, isCanceled, thread, schedulingRule);
            }
            return composites;
        } catch (Throwable t) {
            Agent.logError(t, Messages.CANNOT_GET_ECLIPSE_JOBS);
        }
        return new EclipseJobCompositeData[0];
    }

    @Override
    public void log() {
        try {
            EclipseSchedulingRuleCompositeData compositeData = getSchedulingRule();
            String logMessage = getSchedulingRuleLogMessage(compositeData);
            log(logMessage);

            EclipseJobCompositeData[] compositeDataArray = getJobs();
            logMessage = getJobsLogMessage(compositeDataArray);
            log(logMessage);
        } catch (Throwable t) {
            Agent.logError(t, Messages.CANNOT_LOG_ECLIPSE_JOB_MANAGER_DATA);
        }
    }

    /**
     * Gets the state indicating if accessing eclipse job manager is supported.
     *
     * @return <tt>true</tt> if accessing eclipse job manager is supported
     */
    public boolean isSuppoted() {
        return jobClass != null && jobManagerClass != null && lockManagerClass != null && deadlockDetectorClass != null
                && runtimeLogClass != null && iStatusClass != null && statusClass != null;
    }

    private void initialize() {
        for (@SuppressWarnings("rawtypes")
        Class clazz : inst.getAllLoadedClasses()) {
            String className = clazz.getName();
            if (JOB_CLASS.equals(className)) {
                jobClass = clazz;
            } else if (INTERNAL_JOB_CLASS.equals(className)) {
                internalJobClass = clazz;
            } else if (JOB_MANAGER_CLASS.equals(className)) {
                jobManagerClass = clazz;
            } else if (LOCK_MANAGER_CLASS.equals(className)) {
                lockManagerClass = clazz;
            } else if (DEADLOCK_DETECTOR_CLASS.equals(className)) {
                deadlockDetectorClass = clazz;
            } else if (RUNTIME_LOG_CLASS.equals(className)) {
                runtimeLogClass = clazz;
            } else if (ISTATUS_CLASS.equals(className)) {
                iStatusClass = clazz;
            } else if (STATUS_CLASS.equals(className)) {
                statusClass = clazz;
            } else if (IPROGRESS_MONITOR_CLASS.equals(className)) {
                iProgressMonitorClass = clazz;
            }
        }
    }

    private Object getDeadlockDetectorObject() throws ReflectiveOperationException {
        if (deadlockDetectorObject == null) {

            // call Job.getJobManager()
            Object jobManagerObject = getJobManager();

            // call JobManager.getLockManager()
            Method method = jobManagerClass.getDeclaredMethod(GET_LOCK_MANAGER_METHOD);
            Object lockManagerObject = method.invoke(jobManagerObject);
            if (lockManagerObject == null) {
                return null;
            }

            // access LockManager.locks
            Field field = lockManagerClass.getDeclaredField(LOCKS_FIELD);
            field.setAccessible(true);
            deadlockDetectorObject = field.get(lockManagerObject);

        }
        return deadlockDetectorObject;
    }

    private Object getJobManager() throws ReflectiveOperationException {
        Method method = jobClass.getDeclaredMethod(GET_JOB_MANAGER_METHOD);
        return method.invoke(null);
    }

    private int[][] getGraphField() throws ReflectiveOperationException {
        Field field = deadlockDetectorClass.getDeclaredField(GRAPH_FIELD);
        field.setAccessible(true);
        return (int[][]) field.get(getDeadlockDetectorObject());
    }

    private List<String> getLocksField() throws ReflectiveOperationException {
        Field field = deadlockDetectorClass.getDeclaredField(LOCKS_FIELD);
        field.setAccessible(true);
        List<?> locks = (ArrayList<?>) field.get(getDeadlockDetectorObject());
        return locks.stream().map((lock) -> lock.toString()).collect(Collectors.toList());
    }

    private List<String> getLockThreadsField() throws ReflectiveOperationException {
        Field field = deadlockDetectorClass.getDeclaredField(LOCK_THREADS_FIELD);
        field.setAccessible(true);
        List<?> lockThreads = (ArrayList<?>) field.get(getDeadlockDetectorObject());
        return lockThreads.stream().map((thread) -> getThreadName(thread)).collect(Collectors.toList());
    }

    private String getThreadName(Object thread) {
        if (thread instanceof Thread) {
            return ((Thread) thread).getName();
        }
        throw new IllegalStateException();
    }

    private Object[] findJobs() throws ReflectiveOperationException {

        // call Job.getJobManager()
        Object jobManagerObject = getJobManager();

        // call JobManager.find(null)
        Method method = jobManagerClass.getDeclaredMethod(FIND_METHOD, Object.class);
        Object family = null;
        return (Object[]) method.invoke(jobManagerObject, family);
    }

    private String getJobName(Object jobObject) throws ReflectiveOperationException {

        // call Job.getName()
        Method method = jobClass.getDeclaredMethod(GET_NAME_METHOD);
        String jobName = (String) method.invoke(jobObject);

        // access InternalJob.jobNumber
        Field field = internalJobClass.getDeclaredField(JOB_NUMBER_FIELD);
        field.setAccessible(true);
        Integer jobNumber = (Integer)field.get(jobObject);

        // create unique name with "<job name> (<job number>)"
        StringBuilder builder = new StringBuilder();
        builder.append(jobName).append(" (").append(jobNumber).append(")");

        return builder.toString();
    }

    private String getJobState(Object jobObject) throws ReflectiveOperationException {

        // call Job.getState()
        Method method = jobClass.getDeclaredMethod(GET_STATE_METHOD);
        Integer state = (Integer) method.invoke(jobObject);

        switch (state.intValue()) {
        case 1:
            return "SLEEPING";
        case 2:
            return "WAITING";
        case 4:
            return "RUNNING";
        default:
            return "NONE";
        }
    }

    private boolean isCanceled(Object jobObject) throws ReflectiveOperationException {

        // call InternalJob.getProgressMonitor()
        Method method = internalJobClass.getDeclaredMethod(GET_PROGRESS_MONITOR_METHOD);
        method.setAccessible(true);
        Object progressMonitorObject = method.invoke(jobObject);
        if (progressMonitorObject == null) {
            return false;
        }

        method = iProgressMonitorClass.getDeclaredMethod(IS_CANCELED_METHOD);
        Object isCanceled = method.invoke(progressMonitorObject);
        if (!(isCanceled instanceof Boolean)) {
            return false;
        }

        return ((Boolean)isCanceled).booleanValue();
    }

    private String getThreadNameFromJob(Object jobObject) throws ReflectiveOperationException {

        // call Job.getThread()
        Method method = jobClass.getDeclaredMethod(GET_THREAD_METHOD);
        Object threadObject = method.invoke(jobObject);
        if (threadObject == null) {
            return "";
        }

        return getThreadName(threadObject);
    }

    private String getSchedulingRule(Object jobObject) throws ReflectiveOperationException {

        // call Job.getRule()
        Method method = jobClass.getDeclaredMethod(GET_RULE_METHOD);
        Object schedulingRuleObject = method.invoke(jobObject);
        if (schedulingRuleObject == null) {
            return "";
        }
        return schedulingRuleObject.toString();
    }

    private String getSchedulingRuleLogMessage(EclipseSchedulingRuleCompositeData compositeData) {

        int[][] graph = compositeData.getGraph();
        List<String> locks = compositeData.getLocks();
        List<String> lockThreads = compositeData.getLockThreads();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < lockThreads.size(); i++) {
            String waitedSchedulingRule = "";
            List<String> heldSchedulingRules = new ArrayList<>();

            for (int j = 0; j < graph[i].length; j++) {
                int state = graph[i][j];
                String lock = locks.get(j);
                if (state == -1) {
                    waitedSchedulingRule = lock;
                } else if (state > 0) {
                    heldSchedulingRules.add(lock);
                }
            }

            String heldSchedulingRulesSeparatedByComma;
            if (heldSchedulingRules.isEmpty()) {
                heldSchedulingRulesSeparatedByComma = "";
            } else {
                heldSchedulingRulesSeparatedByComma = heldSchedulingRules.stream().collect(Collectors.joining(", "));
            }

            String message = String.format(Messages.ECLIPSE_SCHEDULING_RULE_LOG_MESSAGE, lockThreads.get(i),
                    heldSchedulingRulesSeparatedByComma, waitedSchedulingRule);
            builder.append(message);
        }

        if (builder.length() == 0) {
            builder.append(Messages.NO_THREAD_USNIG_ECLIPSE_SCHEDULING_RULE);
        }

        return builder.toString();
    }

    private String getJobsLogMessage(EclipseJobCompositeData[] composites) {
        StringBuilder builder = new StringBuilder();

        for (EclipseJobCompositeData composite : composites) {
            String name = composite.getName();
            String className = composite.getClassName();
            String state = composite.getState();
            boolean isCanceled = composite.isCanceled();
            String thread = composite.getThread();
            String schedulingRule = composite.getSchedulingRule();

            String message = String.format(Messages.ECLIPSE_JOB_LOG_MESSAGE, name, className, state, thread,
                    schedulingRule, Boolean.valueOf(isCanceled));
            builder.append(message);
        }

        if (builder.length() == 0) {
            builder.append(Messages.NO_ECLIPSE_JOB_RUNNING_WAITING_OR_SLEEPING);
        }

        return builder.toString();
    }

    private void log(String message) throws ReflectiveOperationException {

        // call Status(int severity, String pluginId, String message)
        Constructor<?> constructor = statusClass.getConstructor(int.class, String.class, String.class);
        Integer severity = Integer.valueOf(1); // info
        Object statusObject = constructor.newInstance(severity, "org.jvmmonitor.agent", message);

        // call RuntimeLog.log(IStatus status)
        Method method = runtimeLogClass.getDeclaredMethod(LOG_METHOD, iStatusClass);
        method.invoke(null, statusObject);
    }
}
