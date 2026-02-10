package com.old.silence.mq.center.task;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.stats.Stats;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.command.stats.StatsAllSubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.old.silence.mq.center.domain.service.DashboardCollectService;
import com.old.silence.mq.center.domain.service.facade.RocketMQClientFacade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CollectTaskRunnable implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(CollectTaskRunnable.class);
    private final String topic;

    private final RocketMQClientFacade mqFacade;

    private final DashboardCollectService dashboardCollectService;

    public CollectTaskRunnable(String topic, RocketMQClientFacade mqFacade,
                               DashboardCollectService dashboardCollectService) {
        this.topic = topic;
        this.mqFacade = mqFacade;
        this.dashboardCollectService = dashboardCollectService;
    }

    @Override
    public void run() {
        Date date = new Date();
        try {
            TopicRouteData topicRouteData = mqFacade.getTopicRoute(topic);
            GroupList groupList = mqFacade.queryTopicConsumers(topic);
            double inTPS = 0;
            long inMsgCntToday = 0;
            double outTPS = 0;
            long outMsgCntToday = 0;
            for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                String masterAddr = bd.getBrokerAddrs().get(MixAll.MASTER_ID);
                if (masterAddr != null) {
                    try {
                        BrokerStatsData bsd = mqFacade.viewBrokerStatsData(masterAddr, Stats.TOPIC_PUT_NUMS, topic);
                        inTPS += bsd.getStatsMinute().getTps();
                        inMsgCntToday += StatsAllSubCommand.compute24HourSum(bsd);
                    } catch (Exception e) {
                        log.warn("Exception caught: viewBrokerStatsData TOPIC_PUT_NUMS failed, topic: [{}]", topic, e.getMessage());
                    }
                }
            }
            if (groupList != null && !groupList.getGroupList().isEmpty()) {
                for (String group : groupList.getGroupList()) {
                    for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                        String masterAddr = bd.getBrokerAddrs().get(MixAll.MASTER_ID);
                        if (masterAddr != null) {
                            try {
                                String statsKey = String.format("%s@%s", topic, group);
                                BrokerStatsData bsd = mqFacade.viewBrokerStatsData(masterAddr, Stats.GROUP_GET_NUMS, statsKey);
                                outTPS += bsd.getStatsMinute().getTps();
                                outMsgCntToday += StatsAllSubCommand.compute24HourSum(bsd);
                            } catch (Exception e) {
                                log.warn("Exception caught: viewBrokerStatsData GROUP_GET_NUMS failed, topic: [{}], group [{}]", topic, group, e.getMessage());
                            }
                        }
                    }
                }
            }

            List<String> list;
            try {
                list = dashboardCollectService.getTopicMap().get(topic);
            } catch (ExecutionException e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
            if (null == list) {
                list = new ArrayList<>();
            }

            list.add(date.getTime() + "," + new BigDecimal(inTPS).setScale(5, RoundingMode.HALF_UP) + "," + inMsgCntToday + "," + new BigDecimal(outTPS).setScale(5, RoundingMode.HALF_UP) + "," + outMsgCntToday);
            dashboardCollectService.getTopicMap().put(topic, list);
        } catch (Exception e) {
            log.error("Failed to collect topic: {} data", topic, e);
        }
    }
}
