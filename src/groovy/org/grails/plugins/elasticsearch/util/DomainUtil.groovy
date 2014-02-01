package org.grails.plugins.elasticsearch.util

import java.lang.reflect.Method;

import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * Allows to unproxy Hibernate proxies while not making hard dependency on Hibernate.<br/>
 * This is quick and dirty solution, would be better as Spring based bean autowired with plugin/user provided deproxifiers ;)
 * Will suffice for now.
 * Some users may want to use Mongo with Hibernate so multiple resolvers possible.
 * 
 * @author Lukasz Wozniak
 *
 */
class DomainUtil {
	def resolvers = []
	static instance

	private DomainUtil() {
		try{
			def hibernate =  Class.forName("org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil")
			Method method = hibernate.getMethod("unwrapIfProxy", Object)
			resolvers.push({
				method.invoke(null, it)
			})
		} catch (ClassNotFoundException e){
			//no hibernate... no big deal
		}

		//dumb resolver, does nothing
		resolvers.push({it})
	}

	def unProxyIfNecessary(instance){
		for(resolver in resolvers){
			def result = resolver(instance)
			if(result!=null){
				return result
			}
		}
		return instance
	}

	static getInstance(){
		if(instance==null){
			synchronized(DomainUtil){
				if(instance==null){
					instance = new DomainUtil()
				}
			}
		}
		return instance
	}

}
