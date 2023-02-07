package cn.service.http;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * @since 2018/5/7
 */
public abstract class BaseOkHttpService {

    /***
     * ok http client
     */
    protected OkHttpClient client;

    /**
     * init OKHttpClient object
     */
    public void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(getBaseConfig().getConnectTimeout(), TimeUnit.MILLISECONDS).writeTimeout(getBaseConfig().getWriteTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(getBaseConfig().getReadTimeout(), TimeUnit.MILLISECONDS).followRedirects(getBaseConfig().isFollowRedirects())
                .followSslRedirects(getBaseConfig().isFollowSslRedirects())
                .connectionPool(new ConnectionPool(getBaseConfig().getMaxIdleConnections(), getBaseConfig().getKeepAliveDuration(), TimeUnit.MILLISECONDS))
                .pingInterval(getBaseConfig().getPingInterval(), TimeUnit.MILLISECONDS).retryOnConnectionFailure(getBaseConfig().isRetryOnConnectionFailure());
        this.client = builder.build();
    }


    /**
     * get baseHttpconfig
     *
     * @return
     */
    public abstract BaseHttpConfig getBaseConfig();

    @Override
    public void finalize() {
        this.client.connectionPool().evictAll();
    }
}
