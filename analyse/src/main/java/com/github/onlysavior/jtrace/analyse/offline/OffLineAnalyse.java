package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.analyse.AnalyseException;
import com.github.onlysavior.jtrace.store.HdfsStoreProvider;
import com.github.onlysavior.jtrace.store.LifeCycleSupport;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public class OffLineAnalyse extends LifeCycleSupport {
    private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "JTRACE_JOBGROUP_NAME";
    private static String TRIGGER_GROUP_NAME = "JTRACE_TRIGGERGROUP_NAME";

    private Scheduler sched = null;
    @Override
    public void start() {
        super.start();
        try {
            sched = gSchedulerFactory.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob(MRJob.class).
                    withIdentity("jtraceanalyse", JOB_GROUP_NAME)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("jtraceTrigger", TRIGGER_GROUP_NAME)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInHours(1).repeatForever())
                    .startAt(DateUtils.addHours(Calendar.getInstance().getTime(), 1))
                    .build();
            sched.start();
            sched.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new AnalyseException(e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            sched.shutdown(true);
        } catch (SchedulerException e) {
            throw new AnalyseException(e);
        }
    }

    public static class MRJob implements Job {
        private String time = null;

        @Override
        public void execute(JobExecutionContext jobExecutionContext)
                throws JobExecutionException {
            Configuration configuration = new Configuration();
            JobConf conf = new JobConf(configuration);
            conf.setJobName("mr1");

            conf.setOutputKeyClass(Text.class);
            conf.setOutputValueClass(LongWritable.class);
            conf.setMapperClass(Mapper.class);
            conf.setReducerClass(Reducer.class);
            conf.setJarByClass(Mapper.class);
            conf.set("mapred.job.tracker", "master:54311");
            conf.set("fs.default.name", "hdfs://master:9000");

            conf.setInputFormat(TextInputFormat.class);
            conf.setOutputFormat(TextOutputFormat.class);

            FileInputFormat.setInputPaths(conf, new Path(input()));
            FileOutputFormat.setOutputPath(conf, new Path(output1()));

            try {
                RunningJob job1 = JobClient.runJob(conf);
                job1.waitForCompletion();

                JobConf conf2 = new JobConf(configuration);
                conf2.setJobName("mr2");
                conf2.setOutputKeyClass(Text.class);
                conf2.setOutputValueClass(LongWritable.class);

                conf2.setMapperClass(Map2.class);
                conf2.setReducerClass(Reducer2.class);
                conf2.setJarByClass(Map2.class);
                conf2.set("mapred.job.tracker", "master:54311");
                conf2.set("fs.default.name", "hdfs://master:9000");

                conf2.setInputFormat(TextInputFormat.class);
                conf2.setOutputFormat(TextOutputFormat.class);

                FileInputFormat.setInputPaths(conf, new Path(output1()));
                FileOutputFormat.setOutputPath(conf, new Path(output2()));
                RunningJob job2 = JobClient.runJob(conf2);
                job2.waitForCompletion();
            } catch (IOException e) {
                throw new JobExecutionException(e);
            }
        }

        private String input() {
            time =  ""+DateUtils.addHours(Calendar.getInstance().getTime(), -1)
                    .getTime();
            String subPath = "input/" + time;
            return HdfsStoreProvider.URI_SPACE + subPath + HdfsStoreProvider.FILE_NAME;
        }

        private String output1() {
            return HdfsStoreProvider.URI_SPACE + "output/" + time + HdfsStoreProvider.OUTPUT1;
        }

        private String output2() {
            return HdfsStoreProvider.URI_SPACE + "output/" + time + HdfsStoreProvider.OUTPUT2;
        }
    }
}
