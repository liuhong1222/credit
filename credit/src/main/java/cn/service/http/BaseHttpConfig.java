package cn.service.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class BaseHttpConfig {

    /**
     * follow Ssl Redirects
     */
    @Value("${http.base.followSslRedirects}")
    private boolean followSslRedirects = true;

    /**
     * follow Redirects
     */
    @Value("${http.base.followRedirects}")
    private boolean followRedirects = true;

    /**
     * retryOnConnectionFailure
     */
    @Value("${http.base.retryOnConnectionFailure}")
    private boolean retryOnConnectionFailure = false;

    /**
     * connect Timeout
     */
    @Value("${http.base.connectTimeout}")
    private int connectTimeout = 3000;

    /**
     * read Timeout
     */
    @Value("${http.base.readTimeout}")
    private int readTimeout = 30000;

    /**
     * write Timeout
     */
    @Value("${http.base.writeTimeout}")
    private int writeTimeout = 5000;

    /**
     * pingInterval
     */
    @Value("${http.base.pingInterval}")
    private int pingInterval = 10 * 1000;

    /**
     * pool max Idle Connections
     */
    @Value("${http.base.maxIdleConnections}")
    private int maxIdleConnections = 50;

    /**
     * pool keep Alive Duration
     */
    @Value("${http.base.keepAliveDuration}")
    private int keepAliveDuration = 10 * 60 * 1000;


    /**
     * @return the followSslRedirects
     */
    public boolean isFollowSslRedirects() {
        return followSslRedirects;
    }

    /**
     * @param followSslRedirects the followSslRedirects to set
     */
    public void setFollowSslRedirects(boolean followSslRedirects) {
        this.followSslRedirects = followSslRedirects;
    }

    /**
     * @return the followRedirects
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * @param followRedirects the followRedirects to set
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * @return the retryOnConnectionFailure
     */
    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * @param retryOnConnectionFailure the retryOnConnectionFailure to set
     */
    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
    }

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param connectTimeout the connectTimeout to set
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * @return the writeTimeout
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * @param writeTimeout the writeTimeout to set
     */
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * @return the pingInterval
     */
    public int getPingInterval() {
        return pingInterval;
    }

    /**
     * @param pingInterval the pingInterval to set
     */
    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * @return the maxIdleConnections
     */
    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    /**
     * @param maxIdleConnections the maxIdleConnections to set
     */
    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    /**
     * @return the keepAliveDuration
     */
    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * @param keepAliveDuration the keepAliveDuration to set
     */
    public void setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }



}
