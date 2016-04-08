package com.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimedRuleExectionOption;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.RuleRuntime;
import org.kie.internal.logger.KnowledgeRuntimeLogger;
import org.kie.internal.logger.KnowledgeRuntimeLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

	public void ss() {

	}
	static KieSession kSession = null;
	static Map<String,FactHandle>  handleMap =new ConcurrentHashMap<>();

	public static final void main(String[] args) {
		
		Logger log = LoggerFactory.getLogger(DroolsTest.class);
		log.info("3333333333333333");
		
		
		try {
			// load up the knowledge base

			KieServices ks = KieServices.Factory.get();

			// KieSessionConfiguration kieSessionConfiguration =
			// ks.newKieSessionConfiguration();
			// kieSessionConfiguration.setOption(ClockTypeOption.get("pseudo"));

			KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
			ksconf.setOption(TimedRuleExectionOption.YES);

			// kieSession = kieBase.newKieSession(ksconf,null);

			// new Date().toLocaleString()
			// ks.newKieModuleModel();

			KieContainer kContainer = ks.getKieClasspathContainer();

			kSession = kContainer.newKieSession("ksession-rules-me", ksconf);

			
			
//			KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger();
			
			// go !
			Message message = new Message();
			message.setMessage("Hello World");
			message.setStatus(Message.HELLO);

			message.setType("english");
			message.setColor("red9");

			MessageFile file = new MessageFile();
			file.setFileName("111.xls");
			message.setFile(file);

			// message.setStatus(Message.GOODBYE);
			//kSession.fireAllRules();
			
			
			/**
			 * 
调度队列里的match 就像一个剪影一样记录了每一次的change
不会有 这一次的把上一次覆盖的情况 每一个都在那里

每次有fact的增加删除或者改变 都会形成一个match 或者叫active 这个不管你调不调session。fire 都会有   这些match会被存到一个调度队列里  每次的change 都会对应一个或者一组match  它们已经生成了 不会受到影响的
drools 群的  12:30:34
[自动回复]您好，我现在有事不在，一会再和您联系。不再提醒
┠无泪  12:31:53
这里如果形成的那些match的时候 对象数据会不会都形成个镜像 （包括哪些？是否包括global）等等？
drools 群的  12:41:51
会的一切与then end 运行相关的上下文都会有一个剪影被保存
drools 群的  12:42:41
这样就保证了 chenge太频繁 被覆盖的情况

			 */
			//如果是fireAllRules之后的update 就不会直接触发 
			//session.fireUntilHalt();就会
			//fireAllRules 是释放全部 当前调度队列里的match 之后就返回了
			//fireUntilHalt 除了上述功能 还会以阻塞的形式 继续等着新的match到来
			new Thread(new Runnable() {
				public void run() {
					System.out.println("======kSession.fireUntilHalt(); start...");
					kSession.fireUntilHalt();
					System.out.println("======kSession.fireUntilHalt(); end...");
				}
			}).start();
			
			kSession.insert(message);
			

			Trigger trigger = new Trigger(" simple ");
			
			kSession.insert(trigger);
			
			
			kSession.getObjects().forEach((obj)->{

				FactHandle handle=kSession.getFactHandle(obj);

				handleMap.put(getEntityKey(obj),handle);

			});
			
			List<MatchResult> results = doQuery("getMatch");
			results = doQuery("getMatch");
			for (MatchResult t : results) {
				System.out.println(t);
			}
			
			System.out.println(  new java.util.Date().toLocaleString() + " ================================== ");
			
			message.setMessage(" 第二次 " + message.getMessage() );
			
			FactHandle handler=handleMap.computeIfAbsent(getEntityKey(message),(k)-> kSession.insert(message));

			kSession.update(handler, message);
			
			trigger.setEnable(false);
			handler=handleMap.computeIfAbsent(getEntityKey(trigger),(k)-> kSession.insert(trigger));

			kSession.update(handler, trigger);
			
			
//			kSession.fireAllRules();
//			
			
			results = doQuery("getMatch");
			for (MatchResult t : results) {
				System.out.println(t);
			}
			
			
//			
//			Timer timer = new Timer();
//			timer.schedule(new TimerTask() {
//				
//				@Override
//				public void run() {
//					System.out.println(  new java.util.Date().toLocaleString() + " kSession.fireAllRules() ");
//					
//					message.setMessage(message.getMessage() + " 第二次 ");
//					
////					kSession.insert(message);
//					kSession.fireAllRules();
//				}
////			}, 1, 1000);
//			}, 1000);
//			
			/*
			 * // ======================================== Thread.sleep(1000*5);
			 * 
			 * kSession = kContainer.newKieSession("ksession-rules-me", ksconf);
			 * 
			 * // go ! message = new Message(); message.setMessage("Hello World"
			 * ); message.setStatus(Message.HELLO);
			 * 
			 * message.setType("english"); message.setColor("red9");
			 * 
			 * 
			 * // message.setStatus(Message.GOODBYE); kSession.insert(message);
			 * kSession.fireAllRules();
			 * 
			 * // ========================================
			 */

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	
	

	public static <T> List<T> doQuery(String queryName,Object... params){



		QueryResults results = getSession().getQueryResults( queryName,params );

		List<T>  list=new ArrayList<>();

		for ( QueryResultsRow row : results ) {
			T result = ( T ) row.get( "results" );
			list.add(result);
		}

		return list;
	}

	
	
	private static RuleRuntime getSession() {
		
		return kSession;
	}





	public static void addOrUpdateData(Object entity){


		
	}

	
	private static String getEntityKey(Object entity) {
		return entity.getClass().getName()+entity.hashCode();
	}

	
	public static class MatchResult {

		private Object match;

		public MatchResult(Object match) {
			super();
			this.match = match;
		}

		public Object getMatch() {
			return match;
		}

		public void setMatch(Object match) {
			this.match = match;
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString() + " match : " + this.match;
		}
		
	}
			
			
	/**
	 * @author carlosyang
	 *
	 */
	public static class Trigger {
		private boolean enable = true;
		
		public boolean isEnable() {
			return enable;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

		public Trigger(String triggerName) {
			super();
			this.triggerName = triggerName;
		}

		private String triggerName;

		public String getTriggerName() {
			return triggerName;
		}

		public void setTriggerName(String triggerName) {
			this.triggerName = triggerName;
		}
		
	}
	
	public static class MessageFile {
		private String fileName;
		private String type;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static class Message {

		public static final int HELLO = 0;
		public static final int GOODBYE = 1;

		private Map<String, Object> values = new HashMap<>();

		public Message() {
			this.values.put("brightness", "5000");
		}

		public Map<String, Object> getValues() {
			return values;
		}

		public void setValues(Map<String, Object> values) {
			this.values = values;
		}

		private String message;
		private String type;
		private MessageFile file;

		public MessageFile getFile() {
			return file;
		}

		public void setFile(MessageFile file) {
			this.file = file;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		private String color;

		private int status;

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public int getStatus() {
			return this.status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

	}

}
