package com.winning.mars_consumer.monitor;

import android.content.Context;

import com.winning.mars_generator.Mars;
import com.winning.mars_generator.core.modules.account.Account;
import com.winning.mars_generator.core.modules.account.AccountBean;
import com.winning.mars_generator.core.modules.battery.Battery;
import com.winning.mars_generator.core.modules.battery.BatteryBean;
import com.winning.mars_generator.core.modules.cpu.Cpu;
import com.winning.mars_generator.core.modules.cpu.CpuBean;
import com.winning.mars_generator.core.modules.crash.Crash;
import com.winning.mars_generator.core.modules.crash.CrashBean;
import com.winning.mars_generator.core.modules.device.Device;
import com.winning.mars_generator.core.modules.device.DeviceBean;
import com.winning.mars_generator.core.modules.fps.Fps;
import com.winning.mars_generator.core.modules.fps.FpsBean;
import com.winning.mars_generator.core.modules.inflate.Inflate;
import com.winning.mars_generator.core.modules.inflate.InflateBean;
import com.winning.mars_generator.core.modules.leak.Leak;
import com.winning.mars_generator.core.modules.leak.LeakBean;
import com.winning.mars_generator.core.modules.network.Network;
import com.winning.mars_generator.core.modules.network.NetworkBean;
import com.winning.mars_generator.core.modules.sm.Sm;
import com.winning.mars_generator.core.modules.sm.SmBean;
import com.winning.mars_generator.core.modules.startup.Startup;
import com.winning.mars_generator.core.modules.startup.StartupBean;
import com.winning.mars_generator.core.modules.thread.deadlock.DeadLock;
import com.winning.mars_generator.core.modules.traffic.Traffic;
import com.winning.mars_generator.core.modules.traffic.TrafficBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * monitor the data generated by mars-generator module
 * Created by yuzhijun on 2018/4/2.
 */
public class Monitor {
    private Repository mRepos;
    private CompositeDisposable mCompositeDisposable;

    public Monitor(){
        mRepos = Repository.getInstance();
        mCompositeDisposable = new CompositeDisposable();
    }

    /**
     * monitor all data
     * */
    public void startMonitor(Context context){
        Mars mars = Mars.getInstance(context);
        mCompositeDisposable.add(mars.getModule(Battery.class).subject().subscribe(new Consumer<BatteryBean>() {
            @Override
            public void accept(BatteryBean batteryBean) throws Exception {
                mRepos.setBatteryBean(batteryBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Cpu.class).subject().subscribe(new Consumer<CpuBean>() {
            @Override
            public void accept(CpuBean cpuBean) throws Exception {
                mRepos.setCpuBean(cpuBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Crash.class).subject().map(new Function<List<CrashBean>, CrashBean>() {
            @Override
            public CrashBean apply(List<CrashBean> crashBeans) throws Exception {
                if (crashBeans == null || crashBeans.isEmpty()) {
                    return CrashBean.INVALID;
                }
                Collections.sort(crashBeans, new Comparator<CrashBean>() {
                    @Override
                    public int compare(CrashBean o1, CrashBean o2) {
                        if (o1.timestampMillis < o2.timestampMillis) {
                            return 1;
                        }
                        if (o1.timestampMillis > o2.timestampMillis) {
                            return -1;
                        }
                        return 0;
                    }
                });
                return crashBeans.get(0);
            }
        }).subscribe(new Consumer<CrashBean>() {
            @Override
            public void accept(CrashBean crashBean) throws Exception {
                mRepos.setCrashBean(crashBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Device.class).subject().subscribe(new Consumer<DeviceBean>() {
            @Override
            public void accept(DeviceBean deviceBean) throws Exception {
                mRepos.setDeviceBean(deviceBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Fps.class).subject().subscribe(new Consumer<FpsBean>() {
            @Override
            public void accept(FpsBean fpsBean) throws Exception {
                mRepos.setFpsBean(fpsBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Inflate.class).subject().subscribe(new Consumer<InflateBean>() {
            @Override
            public void accept(InflateBean inflateBean) throws Exception {
                mRepos.setInflateBean(inflateBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Leak.class).subject().subscribe(new Consumer<LeakBean.LeakMemoryBean>() {
            @Override
            public void accept(LeakBean.LeakMemoryBean leakMemoryBean) throws Exception {
                mRepos.setLeakMemoryBean(leakMemoryBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Sm.class).subject().subscribe(new Consumer<SmBean>() {
            @Override
            public void accept(SmBean smBean) throws Exception {
                mRepos.setSmBean(smBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(DeadLock.class).subject().subscribe(new Consumer<List<Thread>>() {
            @Override
            public void accept(List<Thread> threads) throws Exception {
                mRepos.setDeadLockThreads(threads);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Traffic.class).subject().subscribe(new Consumer<TrafficBean>() {
            @Override
            public void accept(TrafficBean trafficBean) throws Exception {
                mRepos.setTrafficBean(trafficBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Network.class).subject().subscribe(new Consumer<NetworkBean>() {
            @Override
            public void accept(NetworkBean networkBean) throws Exception {
                mRepos.setNetworkBean(networkBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Startup.class).subject().subscribe(new Consumer<StartupBean>() {
            @Override
            public void accept(StartupBean startupBean) throws Exception {
                mRepos.setStartupBean(startupBean);
            }
        }));

        mCompositeDisposable.add(mars.getModule(Account.class).subject().subscribe(new Consumer<AccountBean>() {
            @Override
            public void accept(AccountBean accountBean) throws Exception {
                mRepos.setAccountBean(accountBean);
            }
        }));
    }

    /**
     * stop monitor
     * */
    public void stopMonitor(){
        mCompositeDisposable.dispose();
    }
}
