# experiments

## Exp 1 - No back pressure

Lots of warnings...

16:16:25.209 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-04-30 16:16:25.177912] [0x00004aa3][0x000070000c351000] [info] [processing_statistics_logger.cc:114] Stage 2 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 0, timed: 225, KinesisRecords: 469, PutRecords: 225 }
16:16:25.209 [kpl-daemon-0003] WARN  c.a.services.kinesis.producer.LogInputStreamReader - [2021-04-30 16:16:25.177942] [0x00004aa3][0x000070000c351000] [warning] [processing_statistics_logger.cc:126] PutRecords processing time is taking longer than 15000 ms to complete.  You may need to adjust your configuration to reduce the processing time.

Then tons of errors

16:16:55.297 [pool-1-thread-7] WARN  ds.streamingest.service.StreamWriter - callback error count is 49160

With extended logging:

18:59:23.297 [pool-1-thread-29] ERROR ds.streamingest.service.StreamWriter - Record failed to put, partitionKey=1, payload=[B@21123ae, attempts:
Delay after prev attempt: 29986 ms, Duration: 0 ms, Code: Expired, Message: Expiration reached while waiting in limiter
Delay after prev attempt: 23 ms, Duration: 0 ms, Code: Expired, Message: Expiration reached while waiting in limiter
Delay after prev attempt: 0 ms, Duration: 0 ms, Code: Expired, Message: Record has reached expiration


com.amazonaws.services.kinesis.producer.UserRecordFailedException

See rt-no-bp-ramp-up.png.

The trend is steady response time, then a latency spike while messages are being discarded, a temporary resumption of 
Kinesis writes, then the thrashing below.

Then...

java.lang.RuntimeException: Future for message id 307470 not found as potentially it was a duplicate message or was timed out in Java layer.


At some point the KPL process starts crapping out...

Exception in thread "kpl-callback-pool-1-thread-3164" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached

At this time HTTP errors returned to client 

CLient receives ok response even when KPL error callbacks invokved, then respone time spikes, 
then when KPL dead lots of error responses with quick response time

Saw a crash of the app once during experiments

```
08:04:15.646 [http-nio-8080-exec-7] ERROR o.a.c.c.C.[.[localhost].[/].[dispatcherServlet] - Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is com.amazonaws.services.kinesis.producer.DaemonException: The child process has been shutdown and can no longer accept messages.] with root cause
com.amazonaws.services.kinesis.producer.DaemonException: The child process has been shutdown and can no longer accept messages.
        at com.amazonaws.services.kinesis.producer.Daemon.add(Daemon.java:173)
        at com.amazonaws.services.kinesis.producer.KinesisProducer.addUserRecord(KinesisProducer.java:625)
        at com.amazonaws.services.kinesis.producer.KinesisProducer.addUserRecord(KinesisProducer.java:535)
        at com.amazonaws.services.kinesis.producer.KinesisProducer.addUserRecord(KinesisProducer.java:411)
        at ds.streamingest.service.StreamWriter.writeToStream(StreamWriter.java:60)
        at ds.streamingest.controller.MappedIngestController.processBodyExtraction(MappedIngestController.java:88)
        at ds.streamingest.controller.MappedIngestController.ingest(MappedIngestController.java:44)
        at jdk.internal.reflect.GeneratedMethodAccessor39.invoke(Unknown Source)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197)
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141)
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:894)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1060)
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:962)
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)
        at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:652)
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:733)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:93)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542)
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143)
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:357)
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374)
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:893)
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1707)
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
        at java.base/java.lang.Thread.run(Thread.java:834)

```


## Ramp Ups

### No BP

50 threads, 750s ramp up period (add thread every 15 seconds), 25000 loop count

No back pressure - rt-no-bp-ramp-up.png

HTTP Request	1250000	3	0	2936	25.38501650212771	1.232E-4	1419.4061204791915	231.1022320604809	2518.6141805768466	166.723732
TOTAL	1250000	3	0	2936	25.38501650212771	1.232E-4	1419.4061204791915	231.1022320604809	2518.6141805768466	166.723732

### BP at 500

25 threads, 750s ramp up period (add thread every 15 seconds), 25000 loop count

HTTP Request	443887	57	0	3121	221.35761692052992	0.0	297.821791478384	48.48637055019642	528.4591749181872	166.71057949433077
TOTAL	443887	57	0	3121	221.35761692052992	0.0	297.821791478384	48.48637055019642	528.4591749181872	166.71057949433077

Response time grows a more load introduced - peak throughput looks 'throttled' at 300 TPS.

See rt-bp-500

Typical stats

09:14:58.837 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:14:58.836902] [0x00009eee][0x000070000585a000] [info] [processing_statistics_logger.cc:111] Stage 1 Triggers: { stream: 's2', manual: 0, count: 0, size: 156, matches: 0, timed: 0, UserRecords: 4836, KinesisRecords: 156 }
09:14:58.837 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:14:58.836971] [0x00009eee][0x000070000585a000] [info] [processing_statistics_logger.cc:114] Stage 2 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 26, timed: 0, KinesisRecords: 156, PutRecords: 26 }
09:14:58.837 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:14:58.837004] [0x00009eee][0x000070000585a000] [info] [processing_statistics_logger.cc:129] (s2) Average Processing Time: 1159.4615 ms

Wide range of attempts to buffer - anywhere from 1 to 750 attempts, a few outliers above 750

### BP at 5000

25 threads, 750s ramp up period (add thread every 15 seconds), 25000 loop count

Saw the occasional 'PutRecords processing time is taking longer than 15000 ms to complete.  You may need to adjust your configuration to reduce the processing time.'

HTTP Request	491170	48	0	37300	342.9153769842055	0.0	330.45735346160416	53.79952650594516	586.3681750388035	166.71051367143758
TOTAL	491170	48	0	37300	342.9153769842055	0.0	330.45735346160416	53.79952650594516	586.3681750388035	166.71051367143758

See rt-bp-5000

09:46:32.926 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:46:32.926186] [0x0000a0fa][0x0000700002342000] [info] [processing_statistics_logger.cc:111] Stage 1 Triggers: { stream: 's2', manual: 0, count: 0, size: 156, matches: 0, timed: 0, UserRecords: 4836, KinesisRecords: 156 }
09:46:32.926 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:46:32.926328] [0x0000a0fa][0x0000700002342000] [info] [processing_statistics_logger.cc:114] Stage 2 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 26, timed: 0, KinesisRecords: 156, PutRecords: 26 }
09:46:32.926 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:46:32.926385] [0x0000a0fa][0x0000700002342000] [info] [processing_statistics_logger.cc:129] (s2) Average Processing Time: 13977.731 ms
0

Throttle delays up to 1500

### BP at 10000

25 threads, 750s ramp up period (add thread every 15 seconds), 25000 loop count

Removed attempts logging

HTTP Request	513256	46	0	3086	208.61769724251153	0.0	336.34582958983	54.75819817504621	596.8167698874229	166.71054015929673
TOTAL	513256	46	0	3086	208.61769724251153	0.0	336.34582958983	54.75819817504621	596.8167698874229	166.71054015929673

10:17:58.146 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 10:17:58.145830] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:129] (s2) Average Processing Time: 29010 ms
10:18:13.151 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 10:18:13.150918] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:111] Stage 1 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 0, timed: 0, UserRecords: 0, KinesisRecords: 0 }
10:18:13.151 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 10:18:13.151030] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:114] Stage 2 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 0, timed: 0, KinesisRecords: 0, PutRecords: 0 }
10:18:13.151 [kpl-daemon-0003] WARN  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 10:18:13.151265] [0x0000a312][0x000070000f9ec000] [warning] [processing_statistics_logger.cc:126] PutRecords processing time is taking longer than 15000 ms to complete.  You may need to adjust your configuration to reduce the processing time.


See rt-bp-10000-25t

But... what happens at 50 threads, 750s ramp up period (add thread every 15 seconds), 25000 loop count?



09:54:57.728 [kpl-daemon-0003] WARN  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:54:57.727637] [0x0000a312][0x000070000f9ec000] [warning] [processing_statistics_logger.cc:126] PutRecords processing time is taking longer than 15000 ms to complete.  You may need to adjust your configuration to reduce the processing time.
09:54:57.728 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:54:57.727662] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:129] (s2) Average Processing Time: 30719.615 ms
09:55:12.735 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:55:12.734660] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:111] Stage 1 Triggers: { stream: 's2', manual: 0, count: 0, size: 168, matches: 0, timed: 0, UserRecords: 5208, KinesisRecords: 168 }
09:55:12.735 [kpl-daemon-0003] INFO  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:55:12.734926] [0x0000a312][0x000070000f9ec000] [info] [processing_statistics_logger.cc:114] Stage 2 Triggers: { stream: 's2', manual: 0, count: 0, size: 0, matches: 28, timed: 0, KinesisRecords: 168, PutRecords: 28 }
09:55:12.735 [kpl-daemon-0003] WARN  c.a.services.kinesis.producer.LogInputStreamReader - [2021-05-01 09:55:12.734969] [0x0000a312][0x000070000f9ec000] [warning] [processing_statistics_logger.cc:126] PutRecords processing time is taking longer than 15000 ms to complete.  You may need to adjust your configuration to reduce the processing time.


HTTP Request	1250000	123	0	5309	374.0296445928373	0.0	323.92524841827304	52.73591617559599	574.7775159921895	166.71
TOTAL	1250000	123	0	5309	374.0296445928373	0.0	323.92524841827304	52.73591617559599	574.7775159921895	166.71

See rt-bp-10000-50t

### No back pressure, error out on throttle

Updates to see how many records are received, how many error out

