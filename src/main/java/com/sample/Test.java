package com.sample;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		try {
//			Log4jConfigurer.initLogging("WebContent/WEB-INF/config/log4j.properties");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		Logger log = LoggerFactory.getLogger(Test.class);
		log.info("3333333333333333");
		
	}

}
