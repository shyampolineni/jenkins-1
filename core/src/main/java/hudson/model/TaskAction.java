package hudson.model;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.lang.ref.WeakReference;
import java.io.IOException;

import hudson.security.Permission;

/**
 * Partial {@link Action} implementation for those who kick some
 * processing asynchronously (such as SCM tagging.)
 *
 * <p>
 * The class offers the basic set of functionality to do it.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.191
 * @see TaskThread
 */
public abstract class TaskAction extends AbstractModelObject implements Action {
    /**
     * If non-null, that means either the activitiy is in progress
     * asynchronously, or it failed unexpectedly and the thread is dead.
     */
    protected transient volatile TaskThread workerThread;

    /**
     * Hold the log of the tagging operation.
     */
    protected transient WeakReference<LargeText> log;

    protected abstract Permission getPermission();

    protected abstract AbstractBuild getBuild();

    public WeakReference<LargeText> getLog() {
        return log;
    }

    @Override
    public String getSearchUrl() {
        return getUrlName();
    }

    public TaskThread getWorkerThread() {
        return workerThread;
    }

    /**
     * Handles incremental log output.
     */
    public void doProgressiveLog( StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (log != null) {
            LargeText text = log.get();
            if(text!=null) {
                text.doProgressText(req,rsp);
                return;
            }
        }
        rsp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Clears the error status.
     */
    public synchronized void doClearError(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        getBuild().checkPermission(getPermission());

        if(workerThread!=null && !workerThread.isAlive())
            workerThread = null;
        rsp.sendRedirect(".");
    }
}

