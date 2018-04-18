package com.winning.mars_consumer.monitor.uploader.network;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.winning.mars_consumer.MarsConsumer;
import com.winning.mars_generator.Mars;
import com.winning.mars_generator.core.modules.network.Network;
import com.winning.mars_generator.core.modules.network.NetworkBean;
import com.winning.mars_generator.utils.LogUtil;

import org.apache.http.conn.ConnectTimeoutException;
import org.reactivestreams.Publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import retrofit2.HttpException;

import static com.winning.mars_consumer.utils.Constants.HttpCode.HTTP_NETWORK_ERROR;
import static com.winning.mars_consumer.utils.Constants.HttpCode.HTTP_SERVER_ERROR;
import static com.winning.mars_consumer.utils.Constants.HttpCode.HTTP_UNAUTHORIZED;
import static com.winning.mars_consumer.utils.Constants.HttpCode.HTTP_UNKNOWN_ERROR;

public class ResponseErrorProxy implements InvocationHandler {
    public static final String TAG = ResponseErrorProxy.class.getSimpleName();

    private Object mProxyObject;
    private String url;

    public ResponseErrorProxy(Object proxyObject, String url) {
        mProxyObject = proxyObject;
        this.url = url;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) {
           return Flowable.just("")
                   .flatMap((Function<String, Publisher<?>>) s -> {
                       final long startTime = System.currentTimeMillis();
                       Flowable<?> flowable =  (Flowable<?>) method.invoke(mProxyObject, args);
                       flowable.map((Function<Object, Object>) o -> {
                           try {
                               int respBodySizeByte = sizeOfObject(o);
                               long endTime = System.currentTimeMillis();
                               Mars.getInstance(MarsConsumer.mContext).getModule(Network.class).generate(new NetworkBean(startTime,endTime,respBodySizeByte,url,args));
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           return o;
                       }).subscribe(o -> LogUtil.d(this.getClass().getSimpleName(),"网络请求数据记录完毕"));
                       return flowable;
                   })
                    .retryWhen(throwableFlowable -> throwableFlowable.flatMap((Function<Throwable, Publisher<?>>) throwable -> {
                        ResponseError error = null;
                        if (throwable instanceof ConnectTimeoutException
                                || throwable instanceof SocketTimeoutException
                                || throwable instanceof UnknownHostException
                                || throwable instanceof ConnectException) {
                            error = new ResponseError(HTTP_NETWORK_ERROR, "当前网络环境较差，请稍后重试!");
                        } else if (throwable instanceof HttpException) {
                            HttpException exception = (HttpException) throwable;
                            try {
                                error = new Gson().fromJson(exception.response().errorBody().string(), ResponseError.class);
                            } catch (Exception e) {
                                if (e instanceof JsonParseException) {
                                    error = new ResponseError(HTTP_SERVER_ERROR, "抱歉！服务器出错了!");
                                } else {
                                    error = new ResponseError(HTTP_UNKNOWN_ERROR, "抱歉！系统出现未知错误!");
                                }
                            }
                        } else if (throwable instanceof JsonParseException) {
                            error = new ResponseError(HTTP_SERVER_ERROR, "抱歉！服务器出错了!");
                        } else {
                            error = new ResponseError(HTTP_UNKNOWN_ERROR, "抱歉！系统出现未知错误!");
                        }

                        if (error.getStatus() == HTTP_UNAUTHORIZED) {
                            return refreshTokenWhenTokenInvalid();
                        } else {
                            return Flowable.error(error);
                        }
                    }));
    }

    /**
     * calculate the size of object
     * @param o object to calculate
     * @return int size
     * */
    private  int sizeOfObject(Object o) throws IOException{
        if (null == o){
            return 0;
        }
        ByteArrayOutputStream buff = new ByteArrayOutputStream(4094);
        ObjectOutputStream outputStream = new ObjectOutputStream(buff);
        outputStream.writeObject(o);
        outputStream.flush();
        outputStream.close();
        return buff.size();
    }

    private Flowable<?> refreshTokenWhenTokenInvalid() {
        synchronized (ResponseErrorProxy.class) {
            return Flowable.error(new ResponseError(HTTP_SERVER_ERROR, "抱歉！服务器出错了!"));
        }
    }
}
