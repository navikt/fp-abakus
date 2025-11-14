# Taskmanager#Polling

### SQL Error: 0, SQLState: 40001

Forekommer ved polling da en rad ligger i ScrollableResults, men har blitt flyttet til en annen partisjon(partisjon per status på taskene) av en annen transaksjon(en annen pod som har prosessert tasken). 

```
org.hibernate.exception.LockAcquisitionException: could not advance using next()
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:120)
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:42)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:113)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:99)
	at org.hibernate.internal.ScrollableResultsImpl.convert(ScrollableResultsImpl.java:70)
	at org.hibernate.internal.ScrollableResultsImpl.next(ScrollableResultsImpl.java:105)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManagerRepositoryImpl.pollNesteScrollingUpdate(TaskManagerRepositoryImpl.java:204)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.jboss.weld.bean.proxy.AbstractBeanInstance.invoke(AbstractBeanInstance.java:38)
	at org.jboss.weld.bean.proxy.ProxyMethodHandler.invoke(ProxyMethodHandler.java:106)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManagerRepositoryImpl$Proxy$_$$_WeldClientProxy.pollNesteScrollingUpdate(Unknown Source)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager.pollTasksFunksjon(TaskManager.java:237)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManagerGenerateRunnableTasks.execute(TaskManagerGenerateRunnableTasks.java:45)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager.pollForAvailableTasks(TaskManager.java:196)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks$PollInNewTransaction.doWork(TaskManager.java:265)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks$PollInNewTransaction.doWork(TaskManager.java:251)
	at no.nav.vedtak.felles.jpa.TransactionHandler.apply(TransactionHandler.java:22)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks$PollInNewTransaction.doWork(TaskManager.java:257)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks.doPollingWithEntityManager(TaskManager.java:288)
	at no.nav.vedtak.felles.prosesstask.impl.RequestContextHandler.doWithRequestContext(RequestContextHandler.java:28)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks.call(TaskManager.java:280)
	at no.nav.vedtak.felles.prosesstask.impl.TaskManager$PollAvailableTasks.run(TaskManager.java:321)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
	at java.base/java.util.concurrent.FutureTask.runAndReset(FutureTask.java:305)
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:305)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: org.postgresql.util.PSQLException: ERROR: tuple to be locked was already moved to another partition due to concurrent update
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2497)
	at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2233)
	at org.postgresql.core.v3.QueryExecutorImpl.fetch(QueryExecutorImpl.java:2425)
	at org.postgresql.jdbc.PgResultSet.next(PgResultSet.java:1832)
	at com.zaxxer.hikari.pool.HikariProxyResultSet.next(HikariProxyResultSet.java)
	at org.hibernate.internal.ScrollableResultsImpl.next(ScrollableResultsImpl.java:100)
	... 25 common frames omitted
```
