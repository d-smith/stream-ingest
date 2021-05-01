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

See ss1.png

Then...

java.lang.RuntimeException: Future for message id 307470 not found as potentially it was a duplicate message or was timed out in Java layer.


At some point the KPL process starts crapping out...

Exception in thread "kpl-callback-pool-1-thread-3164" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached

At this time HTTP errors returned to client 

CLient receives ok response even when KPL error callbacks invokved, then respone time spikes, 
then when KPL dead lots of error responses with quick response time

HTTP Request	500000	40	0	4140	61.726748288381785	3.5E-4	1223.2595463174994	199.19792113920937	2170.568941073141	166.750116
TOTAL	500000	40	0	4140	61.726748288381785	3.5E-4	1223.2595463174994	199.19792113920937	2170.568941073141	166.750116

## Exp 2 - Back pressure

AApply back pressure after 5000 buffered

HTTP Request	500000	23	0	1254	23.39731375714092	0.0	2107.2683901312407	343.06905597537025	3739.1666649106096	166.71
TOTAL	500000	23	0	1254	23.39731375714092	0.0	2107.2683901312407	343.06905597537025	3739.1666649106096	166.71

## Exp 3

AApply back pressure after 500 buffered

HTTP Request	500000	19	0	231	15.17188176679478	0.0	2469.221158262261	401.9959563417007	4381.420746643094	166.71
TOTAL	500000	19	0	231	15.17188176679478	0.0	2469.221158262261	401.9959563417007	4381.420746643094	166.71

